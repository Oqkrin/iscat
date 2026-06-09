package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.BestiaryDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class SQLiteBestiaryDAO implements BestiaryDAO {

    @Override
    public void incrementKill(int userId, String enemyKey, int count) {
        if (enemyKey == null || count <= 0) return;

        String sql = """
            INSERT INTO Bestiario (UserID, EnemyKEY, KilledTimes)
            VALUES (?, ?, ?)
            ON CONFLICT(UserID, EnemyKEY)
            DO UPDATE SET KilledTimes = KilledTimes + EXCLUDED.KilledTimes;
        """;

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, enemyKey.toLowerCase().trim());
            pstmt.setInt(3, count);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[SQLiteBestiaryDAO] Errore nell'incremento kill per: " + enemyKey);
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, Integer> getKillsForUser(int userId) {
        Map<String, Integer> killsMap = new LinkedHashMap<>();
        String sql = "SELECT EnemyKEY, KilledTimes FROM Bestiario WHERE UserID = ?;";

        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String key = rs.getString("EnemyKEY").toLowerCase().trim();
                    int times = rs.getInt("KilledTimes");
                    killsMap.put(key, times);
                }
            }

        } catch (SQLException e) {
            System.err.println("[SQLiteBestiaryDAO] Errore nel caricamento del bestiario per utente: " + userId);
            e.printStackTrace();
        }

        return killsMap;
    }
}