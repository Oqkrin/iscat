package uni.gaben.iscat.database.sqlite.settings;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Classe di supporto per l'aggiornamento delle impostazioni video e di visualizzazione sul database SQLite.
 */
public class DisplaySettingsHelper {

    /** * Aggiorna un'opzione di visualizzazione (es. Fullscreen, ShowFPS) per l'utente specificato.
     * @throws IllegalArgumentException se il nome della colonna non supera la whitelist di sicurezza.
     */
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