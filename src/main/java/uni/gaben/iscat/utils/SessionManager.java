package uni.gaben.iscat.utils;

import javafx.animation.AnimationTimer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.screens.scores.SaveData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;

    private SessionUser currentUser;
    private UserSettings currentSettings;
    private final ObjectProperty<SaveData> currentSaveData = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();

    public final List<File> carouselImages = new ArrayList<>();
    public int currentIndex = -1;
    public AnimationTimer uiRainbowSyncTimer;
    // Add this field inside your SessionManager class
    public boolean isLightModeSelected = false;
    public ColorPicker activePicker = null;
    public final List<Color> currentPalette = new ArrayList<>();
    public final Map<ColorPicker, StackPane> pickerBoxes = new HashMap<>();


    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public SessionUser getCurrentUser()        { return currentUser; }
    public void setCurrentUser(SessionUser user) {
        this.currentUser = user;
        if (user != null) {
            username.set(user.username());
        } else {
            username.set(""); // Pulisce la property se l'utente viene disconnesso
        }
    }

    public UserSettings getCurrentSettings()   { return currentSettings; }
    public void setCurrentSettings(UserSettings s) { this.currentSettings = s; }

    public SaveData getCurrentSaveData()               { return currentSaveData.get(); }
    public void setCurrentSaveData(SaveData data)      { currentSaveData.set(data); }
    public ObjectProperty<SaveData> saveDataProperty() { return currentSaveData; }

    public ObservableValue<String> usernameProperty()  { return username; }
}