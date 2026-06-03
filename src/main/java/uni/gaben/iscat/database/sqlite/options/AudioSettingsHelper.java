package uni.gaben.iscat.database.sqlite.options;

import uni.gaben.iscat.database.IscatDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AudioSettingsHelper {

    public void updateVolume(int userId, String columnName, double volumeValue) {
        if (!isValidVolumeColumn(columnName)) {
            throw new IllegalArgumentException("Nome colonna audio non valido: " + columnName);
        }

        int dbVolumeValue = (int) Math.round(volumeValue * 100);

        String sql = "UPDATE ImpostazioniUtenti SET " + columnName + " = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dbVolumeValue);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'aggiornamento del volume " + columnName + " per utente: " + userId, e);
        }
    }

    private boolean isValidVolumeColumn(String column) {
        return column != null && column.matches("(?i)MasterVolume|BGMVolume|SFXVolume");
    }
}