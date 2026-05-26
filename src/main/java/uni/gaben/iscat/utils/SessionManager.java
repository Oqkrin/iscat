package uni.gaben.iscat.utils;

import uni.gaben.iscat.iscat_screens.login.model.SessionUser;

public class SessionManager {
    private static SessionManager instance;
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    public SessionUser currentUser;
    private SessionManager() {}
}
