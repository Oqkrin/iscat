package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.login.model.UserSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SQLiteSettingsDAO implements SettingsDAO {

    // Helper per ottenere connessione fresca
    private Connection getConnection() throws SQLException {
        return IscatDB.getInstance().getConnection();
    }

    @Override
    public Optional<UserSettings> loadSettings(int userId) {
        String sql = "SELECT * FROM ImpostazioniUtenti WHERE UserID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new UserSettings(
                            rs.getInt("UserID"),
                            rs.getString("WalkUp"),
                            rs.getString("WalkDown"),
                            rs.getString("WalkLeft"),
                            rs.getString("WalkRight"),
                            rs.getString("Dash1"),
                            rs.getString("Dash2"),
                            rs.getString("PauseGame")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore loadSettings per userId: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateControl(int userId, String columnName, String newKey) {
        // Whitelist per evitare SQL Injection
        if (!columnName.matches("(?i)WalkUp|WalkDown|WalkLeft|WalkRight|Dash1|Dash2|PauseGame")) return;

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newKey);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore updateControl per userId: " + userId, e);
        }
    }

    @Override
    public void delete(int userId) {
        String deleteSettingsSql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        String deleteSavesSql = "DELETE FROM Salvataggi WHERE UserID = ?";
        String deleteUserSql = "DELETE FROM Utenti WHERE ID = ?";

        try (Connection conn = getConnection()) {
            // Elimina le impostazioni dei controlli
            try (PreparedStatement stmtSettings = conn.prepareStatement(deleteSettingsSql)) {
                stmtSettings.setInt(1, userId);
                stmtSettings.executeUpdate();
            }

            // Elimina i salvataggi
            try (PreparedStatement stmtSaves = conn.prepareStatement(deleteSavesSql)) {
                stmtSaves.setInt(1, userId);
                stmtSaves.executeUpdate();
            }

            // Elimina l'utente dalla tabella principale
            try (PreparedStatement stmtUser = conn.prepareStatement(deleteUserSql)) {
                stmtUser.setInt(1, userId);
                stmtUser.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Errore delete per userId: " + userId, e);
        }
    }

    @Override
    public void createDefault(int userId) {
        String sql = "INSERT OR IGNORE INTO ImpostazioniUtenti (UserID, WalkUp, WalkDown, WalkLeft, WalkRight, Dash1, Dash2, PauseGame) " +
                "VALUES (?, 'W', 'S', 'A', 'D', 'Q', 'E', 'P')";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore createDefault per userId: " + userId, e);
        }
    }
}