package uni.gaben.iscat.database.sqlite.options;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ThemeSettingsHelper {

    public void updateThemeSetting(int userId, String columnName, String hexColor) {
        if (columnName == null || !columnName.matches("(?i)PrimaryTheme|SecondaryTheme|TertiaryTheme|BackgroundTheme")) {
            throw new IllegalArgumentException("Colonna tema non valida: " + columnName);
        }

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, hexColor);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore nell'aggiornare la colonna tema " + columnName, e);
        }
    }
}