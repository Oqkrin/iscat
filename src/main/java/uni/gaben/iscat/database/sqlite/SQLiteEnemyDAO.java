package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.entity.GenericEntitySettings;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteEnemyDAO implements EnemyDAO {

    public SQLiteEnemyDAO() {
    }

    @Override
    public void incrementKill(int userId, String entityKey) {
        if (entityKey == null) return;
        String normalizedKey = entityKey.toLowerCase().trim();

        String sql = """
            INSERT INTO BestiarioUtente (UserID, EnemyID, KillCount)
            VALUES (
                    ?,
                    (SELECT ID FROM Entity WHERE LOWER(EntityKey) = ?),
                    1
            )
            ON CONFLICT(UserID, EnemyID) DO UPDATE SET
                KillCount = KillCount + 1;
            """;

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, normalizedKey);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante incrementKill per nemico: " + entityKey, e);
        }
    }

    @Override
    public List<BestiarioEntry> getBestiarioForUser(int userId) {
        List<BestiarioEntry> list = new ArrayList<>();
        String sql = """
            SELECT e.Name, e.Description, e.SpritePath, COALESCE(b.KillCount, 0) AS KillCount
            FROM Entity e
            LEFT JOIN BestiarioUtente b ON e.ID = b.EnemyID AND b.UserID = ?
            ORDER BY e.ID ASC
            """;

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("Name");
                    String description = rs.getString("Description");
                    String spritePath = rs.getString("SpritePath");
                    int killCount = rs.getInt("KillCount");
                    boolean isUnlocked = killCount > 0;
                    list.add(new BestiarioEntry(name, description, spritePath, killCount, isUnlocked));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nel caricamento del bestiario per utente: " + userId, e);
        }
        return list;
    }

    @Override
    public Optional<GenericEntitySettings> findByKey(String entityKey) {
        if (entityKey == null) return Optional.empty();
        String sql = "SELECT * FROM Entity WHERE LOWER(EntityKey) = ?";

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setString(1, entityKey.toLowerCase().trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la ricerca del nemico tramite chiave: " + entityKey, e);
        }
        return Optional.empty();
    }

    @Override
    public List<GenericEntitySettings> findAll() {
        List<GenericEntitySettings> list = new ArrayList<>();
        String sql = "SELECT * FROM Entity ORDER BY ID ASC";

        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero di tutte le entità nemici", e);
        }
        return list;
    }

    /**
     * Maps a database row from the Entity table to a GenericEntitySettings object.
     * Follows the standardized schema defined in the Entity table.
     * All field mappings are explicit and match the database column names exactly.
     */
    private GenericEntitySettings mapRow(ResultSet rs) throws SQLException {
        GenericEntitySettings s = new GenericEntitySettings();

        // Identity fields
        s.entityKey = rs.getString("EntityKey");
        s.name = rs.getString("Name");
        s.description = rs.getString("Description");

        // Visual properties
        s.spritePath = rs.getString("SpritePath");
        s.frameW = rs.getInt("FrameW");
        s.frameH = rs.getInt("FrameH");
        
        // Shape type (with safe parsing)
        String shapeTypeStr = rs.getString("ShapeType");
        s.shapeType = GenericEntitySettings.ShapeType.fromString(shapeTypeStr);

        // Physical properties (from PhysicalEntitySettings base)
        s.scale = rs.getDouble("Scale");
        s.initLife = rs.getDouble("InitLife");
        s.linearDamping = rs.getDouble("LinearDamping");
        s.mass = rs.getDouble("mass");

        // Movement properties (from PhysicalEntitySettings base)
        s.maxVelocity = rs.getDouble("MaxVelocity");
        s.maxForce = rs.getDouble("MaxForce");
        s.maxAngularVelocity = rs.getDouble("MaxAngularVelocity");

        // Behavioral properties (from PhysicalEntitySettings base)
        s.detectionRange = rs.getDouble("DetectionRange");
        s.combatRange = rs.getDouble("CombatRange");
        s.preferredRange = rs.getDouble("PreferredRange");
        s.actionCooldownMS = rs.getDouble("actionCooldownS");

        // Rewards
        s.xpReward = rs.getInt("XPReward");

        return s;
    }
}