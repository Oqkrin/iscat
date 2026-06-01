package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEnemyDAO implements EnemyDAO {

    // Helper per ottenere connessione fresca
    private Connection getConnection() {
        return IscatDB.getInstance().getConnection();
    }

    @Override
    public void incrementKill(int userId, String entityKey) {
        if (entityKey == null) return;
        String normalizedKey = entityKey.toLowerCase().trim();

        String sql = """
            INSERT INTO BestiarioUtente (UserID, EnemyID, KillCount)
            VALUES (
                ?, 
                (SELECT ID FROM Entita WHERE LOWER(EntityKey) = ?), 
                1
            )
            ON CONFLICT(UserID, EnemyID) DO UPDATE SET 
                KillCount = KillCount + 1;
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, normalizedKey);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Errore incrementKill per userId: " + userId + ", key: " + normalizedKey, e);
        }
    }

    @Override
    public List<EnemyDAO.BestiarioEntry> getBestiarioForUser(int userId) {
        List<EnemyDAO.BestiarioEntry> list = new ArrayList<>();

        String sql = """
            SELECT e.Name, e.Description, e.SpritePath, COALESCE(b.KillCount, 0) AS Kills
            FROM Entita e
            LEFT JOIN BestiarioUtente b ON e.ID = b.EnemyID AND b.UserID = ?
            ORDER BY e.ID ASC;
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int kills = rs.getInt("Kills");
                    boolean unlocked = kills > 0;

                    String name = unlocked ? rs.getString("Name") : "???";
                    String desc = unlocked ? rs.getString("Description") : "Sconfiggi questo nemico nell'universo per sbloccare i suoi dati.";
                    String sprite = rs.getString("SpritePath");

                    list.add(new EnemyDAO.BestiarioEntry(name, desc, sprite, kills, unlocked));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore getBestiarioForUser per userId: " + userId, e);
        }
        return list;
    }

    @Override
    public Optional<GenericEntitySettings> findByKey(String entityKey) {
        if (entityKey == null) return Optional.empty();
        String normalizedKey = entityKey.toLowerCase().trim();

        String sql = "SELECT * FROM Entita WHERE LOWER(TRIM(EntityKey)) = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, normalizedKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore findByKey per entityKey: " + normalizedKey, e);
        }
        return Optional.empty();
    }

    @Override
    public List<GenericEntitySettings> findAll() {
        String sql = "SELECT * FROM Entita";
        List<GenericEntitySettings> results = new ArrayList<>();

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore findAll", e);
        }
        return results;
    }

    /**
     * Converte la riga corrente di un ResultSet nell'oggetto di configurazione tipizzato.
     */
    private GenericEntitySettings mapRow(ResultSet rs) throws SQLException {
        GenericEntitySettings s = new GenericEntitySettings();

        s.entityKey   = rs.getString("EntityKey");
        s.name        = rs.getString("Name");
        s.description = rs.getString("Description");

        s.spritePath  = rs.getString("SpritePath");
        s.frameW      = rs.getInt("FrameW");
        s.frameH      = rs.getInt("FrameH");

        s.initLife        = rs.getDouble("InitLife");
        s.dimSprite       = rs.getInt("FrameW");
        s.scale           = rs.getDouble("Scale");
        s.dampingLineare  = rs.getDouble("LinearDamping");
        s.maxVelocity     = rs.getDouble("MaxVelocity");
        s.force           = rs.getDouble("Force");
        s.rotationSpeed   = rs.getDouble("RotationSpeed");
        s.xpReward        = rs.getInt("XPReward");
        s.detectionRange  = rs.getDouble("DetectionRange");
        s.combatRange     = rs.getDouble("CombatRange");
        s.preferredRange  = rs.getDouble("PreferredRange");
        s.fireCooldownS   = rs.getDouble("FireCooldownS");
        s.customParam1    = rs.getDouble("CustomParam1");
        s.customParam2    = rs.getDouble("CustomParam2");

        s.shapeType    = GenericEntitySettings.ShapeType.fromString(rs.getString("ShapeType"));
        s.behaviorType = GenericEntitySettings.BehaviorType.fromString(rs.getString("BehaviorType"));

        return s;
    }
}