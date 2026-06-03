package uni.gaben.iscat.database.sqlite.options;

import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.user.UserSettings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class SQLiteSettingsDAO implements SettingsDAO {

    private final AudioSettingsHelper audioHelper;
    private final DisplaySettingsHelper displayHelper;
    private final ControlSettingsHelper controlHelper;
    private final ThemeSettingsHelper themeHelper;
    private final UserSettingsMapper settingsMapper;

    public SQLiteSettingsDAO() {
        this.audioHelper = new AudioSettingsHelper();
        this.displayHelper = new DisplaySettingsHelper();
        this.controlHelper = new ControlSettingsHelper();
        this.themeHelper = new ThemeSettingsHelper();
        this.settingsMapper = new UserSettingsMapper();
    }

    @Override
    public Optional<UserSettings> loadSettings(int userId) {
        String sql = "SELECT * FROM ImpostazioniUtenti WHERE UserID = ?";
        try (PreparedStatement stmt = IscatDB.getInstance().getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(settingsMapper.mapResultSetToUserSettings(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore loadSettings per userId: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public void updateVolume(int userId, String columnName, double volumeValue) {
        audioHelper.updateVolume(userId, columnName, volumeValue);
    }

    @Override
    public void updateDisplaySetting(int userId, String columnName, int value) {
        displayHelper.updateDisplaySetting(userId, columnName, value);
    }

    @Override
    public void updateControl(int userId, String columnName, String newKey) {
        controlHelper.updateControl(userId, columnName, newKey);
    }

    public void updateThemeSetting(int userId, String columnName, String hexColor) {
        themeHelper.updateThemeSetting(userId, columnName, hexColor);
    }

    @Override
    public void delete(int userId) {
        String deleteSettingsSql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        String deleteSavesSql = "DELETE FROM Salvataggi WHERE UserID = ?";
        String deleteBestiarySql = "DELETE FROM BestiarioUtente WHERE UserID = ?";
        String deleteUserSql = "DELETE FROM Utenti WHERE ID = ?";

        try (Connection conn = IscatDB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
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
}