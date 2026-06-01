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

    public SQLiteSettingsDAO() {
    }

    @Override
    public Optional<UserSettings> loadSettings(int userId) {
        String sql = "SELECT * FROM ImpostazioniUtenti WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {

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
        if (!isValidControlColumn(columnName)) {
            throw new IllegalArgumentException("Nome colonna non valido o non autorizzato: " + columnName);
        }

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newKey);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del controllo " + columnName + " per utente: " + userId, e);
        }
    }

    @Override
    public void delete(int userId) {
        String deleteSettingsSql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        String deleteSavesSql = "DELETE FROM Salvataggi WHERE UserID = ?";
        String deleteBestiarySql = "DELETE FROM BestiarioUtente WHERE UserID = ?";
        String deleteUserSql = "DELETE FROM Utenti WHERE ID = ?";

        try (Connection conn = IscatDB.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Inizio blocco transazionale atomico
            try {
                try (PreparedStatement stmtSettings = conn.prepareStatement(deleteSettingsSql)) {
                    stmtSettings.setInt(1, userId);
                    stmtSettings.executeUpdate();
                }
                try (PreparedStatement stmtSaves = conn.prepareStatement(deleteSavesSql)) {
                    stmtSaves.setInt(1, userId);
                    stmtSaves.executeUpdate();
                }
                try (PreparedStatement stmtBestiary = conn.prepareStatement(deleteBestiarySql)) {
                    stmtBestiary.setInt(1, userId);
                    stmtBestiary.executeUpdate();
                }
                try (PreparedStatement stmtUser = conn.prepareStatement(deleteUserSql)) {
                    stmtUser.setInt(1, userId);
                    stmtUser.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione a cascata dell'utente: " + userId, e);
        }
    }

    @Override
    public void createDefault(int userId) {
        String sql = """
            INSERT OR IGNORE INTO ImpostazioniUtenti 
            (UserID, WalkUp, WalkDown, WalkLeft, WalkRight, Dash1, Dash2, PauseGame) 
            VALUES (?, 'W', 'S', 'A', 'D', 'Q', 'E', 'P')
            """;
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante la creazione delle impostazioni predefinite per utente: " + userId, e);
        }
    }

    private boolean isValidControlColumn(String column) {
        return column != null && column.matches("(?i)WalkUp|WalkDown|WalkLeft|WalkRight|Dash1|Dash2|PauseGame");
    }
}