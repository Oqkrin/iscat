package uni.gaben.iscat.database.sqlite.settings;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Classe di supporto per l'aggiornamento dei temi grafici e delle impostazioni estetiche sul database SQLite.
 */
public class ThemeSettingsHelper {

    /** * Aggiorna una specifica preferenza cromatica o di stile visivo (es. PrimaryTheme, BackgroundTheme) per l'utente.
     * @throws IllegalArgumentException se il nome della colonna non supera la whitelist di sicurezza.
     */
    public void updateThemeSetting(int userId, String columnName, String value) {
        if (columnName == null || !columnName.matches("(?i)PrimaryTheme|SecondaryTheme|TertiaryTheme|BackgroundTheme|RainbowMode|Lightmode")) {
            throw new IllegalArgumentException("Colonna tema non valida: " + columnName);
        }

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornare la colonna tema " + columnName, e);
        }
    }
}