package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data-access object for the Entita table.
 * Follows the same pattern as ScoreDAO: static methods, IscatDB singleton,
 * try-with-resources on every call, no open connections kept.
 * All numeric stats from the DB are written directly into GenericEntitySettings
 * (which extends EntitySettings), so the rest of the engine consumes them
 * exactly as it would a hardcoded Settings object.
 */
public class EnemyDAO {

    private EnemyDAO() {}

    // ── Single lookup ─────────────────────────────────────────────────────────

    /**
     * Loads one enemy by its EntityKey (e.g. "iscat_mob").
     *
     * @param entityKey the primary key string stored in the Entita table
     * @return an Optional containing the settings, or empty if not found
     */
    public static Optional<GenericEntitySettings> findByKey(String entityKey) {
        String sql = "SELECT * FROM Entita WHERE EntityKey = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, entityKey);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // ── Bulk load ─────────────────────────────────────────────────────────────

    /**
     * Loads every row from Entita.
     * Useful for pre-caching all enemy definitions on game start.
     */
    public static List<GenericEntitySettings> findAll() {
        String sql = "SELECT * FROM Entita";
        List<GenericEntitySettings> results = new ArrayList<>();
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // ── Row mapping ───────────────────────────────────────────────────────────

    /**
     * Maps a single ResultSet row → GenericEntitySettings.
     * Column names mirror the Entita table exactly.
     */
    private static GenericEntitySettings mapRow(ResultSet rs) throws SQLException {
        GenericEntitySettings s = new GenericEntitySettings();

        // Identity
        s.entityKey   = rs.getString("EntityKey");
        s.name        = rs.getString("Name");
        s.description = rs.getString("Description");

        // Sprite
        s.spritePath  = rs.getString("SpritePath");
        s.frameW      = rs.getInt("FrameW");
        s.frameH      = rs.getInt("FrameH");

        // Physics & combat (EntitySettings fields)
        s.initLife        = rs.getDouble("InitLife");
        s.dimSprite       = rs.getDouble("FrameW");   // dimSprite = frame width for hitbox calc
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

        // Generic system fields (columns added by migration)
        s.shapeType    = GenericEntitySettings.ShapeType.fromString(rs.getString("ShapeType"));
        s.behaviorType = GenericEntitySettings.BehaviorType.fromString(rs.getString("BehaviorType"));

        return s;
    }
}