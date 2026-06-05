package uni.gaben.iscat.database.sqlite.options;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DisplaySettingsHelper {

    public void updateDisplaySetting(int userId, String columnName, int value) {
        if (columnName == null || !columnName.matches("(?i)ShowFPS|Fullscreen|DebugMode|Lightmode|RainbowMode")) {
            throw new IllegalArgumentException("Colonna display/stato non valida: " + columnName);
        }

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornare la colonna display " + columnName, e);
        }
    }
}