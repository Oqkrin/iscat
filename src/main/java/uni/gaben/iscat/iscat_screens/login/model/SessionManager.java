package uni.gaben.iscat.iscat_screens.login.model;

public class SessionManager {
    private static SessionManager instance;
    private SessionUser currentUser;
    private UserSettings currentSettings;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new java.util.concurrent.atomic.AtomicReference<>(new SessionManager()).get();
        }
        return instance;
    }

    public SessionUser getCurrentUser() { return currentUser; }
    public void setCurrentUser(SessionUser user) { this.currentUser = user; }

    public UserSettings getCurrentSettings() { return currentSettings; }
    public void setCurrentSettings(UserSettings settings) { this.currentSettings = settings; }
}
