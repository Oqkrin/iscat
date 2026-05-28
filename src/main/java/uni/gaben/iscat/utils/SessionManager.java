package uni.gaben.iscat.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.screens.login.model.UserSettings;

public class SessionManager {
    private static SessionManager instance;

    // Campi per memorizzare i dati della sessione attiva
    private SessionUser currentUser;
    private UserSettings currentSettings;
    private StringProperty username = new SimpleStringProperty();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public SessionUser getCurrentUser() { return currentUser; }
    public void setCurrentUser(SessionUser currentUser) {
        this.currentUser = currentUser;
        username.set(currentUser.username());
    }

    public UserSettings getCurrentSettings() { return currentSettings; }
    public void setCurrentSettings(UserSettings currentSettings) { this.currentSettings = currentSettings; }

    public ObservableValue<String> usernameProperty() {
        return username;
    }
}