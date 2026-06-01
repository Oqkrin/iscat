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
                (SELECT ID FROM Entita WHERE LOWER(EntityKey) = ?), 
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
            FROM Entita e
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
        String sql = "SELECT * FROM Entita WHERE LOWER(EntityKey) = ?";

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
        String sql = "SELECT * FROM Entita ORDER BY ID ASC";

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

    private GenericEntitySettings mapRow(ResultSet rs) throws SQLException {
        GenericEntitySettings s = new GenericEntitySettings();

        s.entityKey      = rs.getString("EntityKey");
        s.name           = rs.getString("Name");
        s.description    = rs.getString("Description");
        s.spritePath     = rs.getString("SpritePath");
        s.frameW         = rs.getInt("FrameW");
        s.frameH         = rs.getInt("FrameH");
        s.initLife       = rs.getDouble("InitLife");
        s.dimSprite      = rs.getInt("FrameW");
        s.scale          = rs.getDouble("Scale");
        s.dampingLineare = rs.getDouble("LinearDamping");
        s.maxVelocity    = rs.getDouble("MaxVelocity");
        s.force          = rs.getDouble("Force");
        s.rotationSpeed  = rs.getDouble("RotationSpeed");
        s.xpReward       = rs.getInt("XPReward");
        s.detectionRange = rs.getDouble("DetectionRange");
        s.combatRange    = rs.getDouble("CombatRange");
        s.preferredRange = rs.getDouble("PreferredRange");
        s.fireCooldownS  = rs.getDouble("FireCooldownS");
        s.customParam1   = rs.getDouble("CustomParam1");
        s.customParam2   = rs.getDouble("CustomParam2");

        String shapeTypeStr = rs.getString("ShapeType");
        if (shapeTypeStr != null) {
            try {
                s.shapeType = GenericEntitySettings.ShapeType.valueOf(shapeTypeStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                s.shapeType = GenericEntitySettings.ShapeType.CIRCLE; // Fallback di sicurezza
            }
        }
        return s;
    }
}