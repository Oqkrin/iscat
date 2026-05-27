package uni.gaben.iscat.utils;

import uni.gaben.iscat.iscat_screens.login.model.SessionUser;
import uni.gaben.iscat.iscat_screens.login.model.UserSettings;

public class SessionManager {
    private static SessionManager instance;

    // Campi per memorizzare i dati della sessione attiva
    private SessionUser currentUser;
    private UserSettings currentSettings;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionUser getCurrentUser() { return currentUser; }
    public void setCurrentUser(SessionUser currentUser) { this.currentUser = currentUser; }

    public UserSettings getCurrentSettings() { return currentSettings; }
    public void setCurrentSettings(UserSettings currentSettings) { this.currentSettings = currentSettings; }
}