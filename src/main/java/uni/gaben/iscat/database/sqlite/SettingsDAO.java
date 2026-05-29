package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.login.model.UserSettings;
import java.sql.*;

public class SettingsDAO {
    private SettingsDAO() {
        /* This utility class should not be instantiated */
    }

    public static UserSettings loadSettings(int userId) {
        String sql = "SELECT * FROM ImpostazioniUtenti WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserSettings(
                            rs.getInt("UserID"),
                            rs.getString("WalkUp"),
                            rs.getString("WalkDown"),
                            rs.getString("WalkLeft"),
                            rs.getString("WalkRight"),
                            rs.getString("Dash1"),
                            rs.getString("Dash2"),
                            rs.getString("PauseGame")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateControl(int userId, String columnName, String newKey) {
        // Usiamo una whitelist per evitare SQL Injection dinamica sul nome della colonna
        if (!columnName.matches("(?i)WalkUp|WalkDown|WalkLeft|WalkRight|Dash1|Dash2|PauseGame")) return;

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newKey);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void delete(int userId) {
        String sql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}