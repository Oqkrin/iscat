package uni.gaben.iscat.database.sqlite.options;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ControlSettingsHelper {

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

    private boolean isValidControlColumn(String column) {
        return column != null && column.matches("(?i)WalkUp|WalkDown|WalkLeft|WalkRight|Dash1|Dash2|Attack|PauseGame");
    }
}