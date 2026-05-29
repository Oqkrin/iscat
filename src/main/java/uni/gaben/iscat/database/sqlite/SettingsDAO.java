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

    /**
     * Elimina l'utente distruggendo le sue impostazioni, i suoi salvataggi
     * e il suo profilo di login principale senza bloccare il database.
     * @param userId ID dell'utente da eliminare
     */
    public static void delete(int userId) {
        String deleteSettingsSql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        String deleteSavesSql = "DELETE FROM Salvataggi WHERE UserID = ?";
        String deleteUserSql = "DELETE FROM Utenti WHERE ID = ?";

        try (Connection conn = IscatDB.getInstance().getConnection()) {

            // Elimina le impostazioni dei controlli
            try (PreparedStatement stmtSettings = conn.prepareStatement(deleteSettingsSql)) {
                stmtSettings.setInt(1, userId);
                int rows = stmtSettings.executeUpdate();
                System.out.println("Impostazioni rimosse per utente " + userId + " (Righe: " + rows + ")");
            }

            // Elimina i salvataggi
            try (PreparedStatement stmtSaves = conn.prepareStatement(deleteSavesSql)) {
                stmtSaves.setInt(1, userId);
                int rows = stmtSaves.executeUpdate();
                System.out.println("Salvataggi rimossi per utente " + userId + " (Righe: " + rows + ")");
            }

            // Elimina l'utente dalla tabella principale
            try (PreparedStatement stmtUser = conn.prepareStatement(deleteUserSql)) {
                stmtUser.setInt(1, userId);
                int rows = stmtUser.executeUpdate();
                System.out.println("Utente rimosso da tabella Utenti (Righe: " + rows + ")");
            }

            System.out.println("Procedura di eliminazione completata per l'ID: " + userId);

        } catch (SQLException e) {
            System.err.println("ERRORE CRITICO DURANTE LA DELETE DELL'UTENTE!");
            e.printStackTrace();
        }
    }
}