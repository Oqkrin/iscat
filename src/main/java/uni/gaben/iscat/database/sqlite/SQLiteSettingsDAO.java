package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.user.UserSettings;

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
                    double master = rs.getInt("MasterVolume") / 100.0;
                    double bgm = rs.getInt("BGMVolume") / 100.0;
                    double sfx = rs.getInt("SFXVolume") / 100.0;
                    int showFps = rs.getInt("ShowFPS");
                    int fullscreen = rs.getInt("Fullscreen");

                    int debugMode = rs.getInt("DebugMode");
                    int lightmode = rs.getInt("Lightmode");
                    int rainbowMode = rs.getInt("RainbowMode");
                    String primaryTheme = rs.getString("PrimaryTheme");
                    String secondaryTheme = rs.getString("SecondaryTheme");
                    String tertiaryTheme = rs.getString("TertiaryTheme");
                    String backgroundTheme = rs.getString("BackgroundTheme");
                    double scale = rs.getInt("Scale") / 100.0;

                    return Optional.of(new UserSettings(
                            rs.getInt("UserID"),
                            rs.getString("WalkUp"),
                            rs.getString("WalkDown"),
                            rs.getString("WalkLeft"),
                            rs.getString("WalkRight"),
                            rs.getString("Attack"),
                            rs.getString("Dash1"),
                            rs.getString("Dash2"),
                            rs.getString("PauseGame"),
                            master,
                            bgm,
                            sfx,
                            showFps,
                            fullscreen,
                            debugMode,
                            lightmode,
                            rainbowMode,
                            primaryTheme,
                            secondaryTheme,
                            tertiaryTheme,
                            backgroundTheme,
                            scale
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore loadSettings per userId: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
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

    @Override
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
        (UserID, WalkUp, WalkDown, WalkLeft, WalkRight, 
               Attack, Dash1, Dash2, PauseGame, 
               MasterVolume, BGMVolume, SFXVolume, 
               Scale, ShowFPS, Fullscreen,
               DebugMode, Lightmode, RainbowMode,
               PrimaryTheme, SecondaryTheme, TertiaryTheme, BackgroundTheme) 
        VALUES (?, 'W', 'S', 'A', 'D', 'MOUSEPRIMARY', 'Q', 'E', 'P', 50, 50, 30, 50, 0, 1, 0, 0, 0, '#FFFFFF', '#FFFFFF', '#FFFFFF', '#000000')
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
        return column != null && column.matches("(?i)WalkUp|WalkDown|WalkLeft|WalkRight|Dash1|Dash2|Attack|PauseGame");
    }

    private boolean isValidVolumeColumn(String column) {
        return column != null && column.matches("(?i)MasterVolume|BGMVolume|SFXVolume");
    }
}