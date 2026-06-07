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
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.model.ScoreModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionManager {
    private static SessionManager instance;

    private SessionUser currentUser;
    private UserSettings currentSettings;
    private final ObjectProperty<ScoreModel> currentSaveData = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();

    public final List<File> carouselImages = new ArrayList<>();
    public int currentIndex = -1;
    public AnimationTimer uiRainbowSyncTimer;
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

    public ScoreModel getCurrentSaveData()               { return currentSaveData.get(); }
    public void setCurrentSaveData(ScoreModel data)      { currentSaveData.set(data); }
    public ObjectProperty<ScoreModel> saveDataProperty() { return currentSaveData; }

    public ObservableValue<String> usernameProperty()  { return username; }
}