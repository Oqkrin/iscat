package uni.gaben.iscat.screens.bestiary;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class BestiaryData {

    public record Enemy(
            String entityKey,
            String name,
            String sprite,
            int frameW,
            int frameH,
            String description,

            int initLife,
            int maxVelocity,
            int xpReward,
            int detectionRange,
            int combatRange,
            int fireCooldownS
    ) {}

    public Map<String, Enemy> loadEnemies() {
        Map<String, Enemy> enemies = new LinkedHashMap<>();

        String sql = """
            SELECT ID, EntityKey, Name, SpritePath, FrameW, FrameH, Description,
                   InitLife, MaxVelocity, XPReward, DetectionRange, CombatRange, FireCooldownS
            FROM Entita
            ORDER BY ID ASC
        """;

        try {
            Connection conn = IscatDB.getInstance().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Enemy enemy = new Enemy(
                        rs.getString("EntityKey").trim(),
                        rs.getString("Name"),
                        rs.getString("SpritePath"),
                        rs.getInt("FrameW"),
                        rs.getInt("FrameH"),
                        rs.getString("Description"),
                        rs.getInt("InitLife"),
                        rs.getInt("MaxVelocity"),
                        rs.getInt("XPReward"),
                        rs.getInt("DetectionRange"),
                        rs.getInt("CombatRange"),
                        rs.getInt("FireCooldownS")
                );

                enemies.put(enemy.entityKey(), enemy);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return enemies;
    }
}