package uni.gaben.iscat.utils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.screens.scores.SaveData;

public class SessionManager {
    private static SessionManager instance;

    private SessionUser currentUser;
    private UserSettings currentSettings;
    private final ObjectProperty<SaveData> currentSaveData = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public SessionUser getCurrentUser()        { return currentUser; }
    public void setCurrentUser(SessionUser user) {
        this.currentUser = user;
        username.set(user.username());
    }

    public UserSettings getCurrentSettings()   { return currentSettings; }
    public void setCurrentSettings(UserSettings s) { this.currentSettings = s; }

    public SaveData getCurrentSaveData()               { return currentSaveData.get(); }
    public void setCurrentSaveData(SaveData data)      { currentSaveData.set(data); }
    public ObjectProperty<SaveData> saveDataProperty() { return currentSaveData; }

    public ObservableValue<String> usernameProperty()  { return username; }
}