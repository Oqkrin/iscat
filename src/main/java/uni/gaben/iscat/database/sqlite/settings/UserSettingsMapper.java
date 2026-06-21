package uni.gaben.iscat.database.sqlite.settings;

import uni.gaben.iscat.model.user.UserSettings;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapper per convertire i record del database SQLite in oggetti di business {@link UserSettings}.
 * Si occupa anche della normalizzazione dei valori (es. conversione dei volumi e della scala da interi a decimali).
 */
public class UserSettingsMapper {

    /** * Mappa la riga corrente del ResultSet in un oggetto UserSettings popolato.
     * I valori percentuali memorizzati come interi nel database vengono convertiti in decimali (0.0 - 1.0).
     */
    public UserSettings mapResultSetToUserSettings(ResultSet rs) throws SQLException {
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

        return new UserSettings(
                rs.getInt("UserID"),
                rs.getString("WalkUp"),
                rs.getString("WalkDown"),
                rs.getString("WalkLeft"),
                rs.getString("WalkRight"),
                rs.getString("Attack"),
                rs.getString("Dash"),
                rs.getString("BulletTime"),
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
        );
    }
}