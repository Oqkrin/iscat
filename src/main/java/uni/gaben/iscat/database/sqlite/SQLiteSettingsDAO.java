package uni.gaben.iscat.database.sqlite;

import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.sqlite.settings.*;
import uni.gaben.iscat.model.user.UserSettings;

import java.sql.*;
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
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
    public void updatePlayerSkin(int userId, String skinKey) {
        String sql = "UPDATE ImpostazioniUtenti SET PlayerSkinKey = ? WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, skinKey);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore updatePlayerSkin per userId: " + userId, e);
        }
    }

    @Override
    public String loadPlayerSkin(int userId) {
        String sql = "SELECT PlayerSkinKey FROM ImpostazioniUtenti WHERE UserID = ?";
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String key = rs.getString("PlayerSkinKey");
                    return (key != null && !key.isBlank()) ? key : "player1";
                }
            }
        } catch (SQLException e) {
            return "player1";
        }
        return "player1";
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

    @Override
    public void updateThemeSetting(int userId, String choosenTheme, String hexvalue) {
        themeHelper.updateThemeSetting(userId, choosenTheme, hexvalue);
    }

    @Override
    public void delete(int userId) {
        String deleteSettingsSql = "DELETE FROM ImpostazioniUtenti WHERE UserID = ?";
        String deleteSavesSql    = "DELETE FROM UserScore WHERE UserID = ?";
        String deleteBestiarySql = "DELETE FROM Bestiario WHERE UserID = ?";
        String deleteUserSql     = "DELETE FROM Utenti WHERE ID = ?";

        try (Connection conn = IscatDB.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement s = conn.prepareStatement(deleteSettingsSql)) {
                    s.setInt(1, userId); s.executeUpdate();
                }
                try (PreparedStatement s = conn.prepareStatement(deleteSavesSql)) {
                    s.setInt(1, userId); s.executeUpdate();
                }
                try (PreparedStatement s = conn.prepareStatement(deleteBestiarySql)) {
                    s.setInt(1, userId); s.executeUpdate();
                }
                try (PreparedStatement s = conn.prepareStatement(deleteUserSql)) {
                    s.setInt(1, userId); s.executeUpdate();
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore eliminazione utente: " + userId, e);
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
            VALUES (?, 'W', 'S', 'A', 'D', 'MOUSEPRIMARY', 'Q', 'E', 'P',
                    50, 50, 30, 50, 0, 1, 0, 0, 0,
                    '#cbcbcb', '#a9a9a9', '#333333', '#010203')
            """;
        try (Connection conn = IscatDB.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore createDefault per userId: " + userId, e);
        }
    }
}