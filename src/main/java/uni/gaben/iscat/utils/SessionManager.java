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

/**
 * Manager di sessione globale e centralizzato per il ciclo di vita dell'applicazione.
 * Gestisce lo stato dell'utente autenticato, le preferenze di configurazione, i dati di salvataggio (punteggi)
 * e le proprietà reattive della skin del giocatore attualmente equipaggiata.
 */
public class SessionManager {

    // === Sistema di Skin (Centralizzato & Data-Driven) ===
    private static final StringProperty playerSkin = new SimpleStringProperty("/uni/gaben/iscat/sprites/players/player1.png");
    private static final StringProperty playerSkinKey = new SimpleStringProperty("player1");

    private static SessionManager instance;

    private SessionUser currentUser;
    private UserSettings currentSettings;
    private final ObjectProperty<ScoreModel> currentSaveData = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();

    // === Stati Temporanei della UI e della Configurazione dei Temi ===
    public final List<File> carouselImages = new ArrayList<>();
    public int currentIndex = -1;
    public AnimationTimer uiRainbowSyncTimer;
    public boolean isLightModeSelected = false;
    public ColorPicker activePicker = null;
    public final List<Color> currentPalette = new ArrayList<>();
    public final Map<ColorPicker, StackPane> pickerBoxes = new HashMap<>();

    private SessionManager() {
        /* Costruttore privato per garantire il pattern Singleton */
    }

    /**
     * Restituisce l'istanza unica del SessionManager.
     * Implementa l'inizializzazione pigra (Lazy Initialization).
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public static StringProperty playerSkinProperty() {
        return playerSkin;
    }

    public static String getPlayerSkin() {
        return playerSkin.getValue();
    }

    public static void setPlayerSkin(String skin) {
        playerSkin.setValue(skin);
    }

    public static String getPlayerSkinKey() {
        return playerSkinKey.getValue();
    }

    public static void setPlayerSkinKey(String key) {
        playerSkinKey.setValue(key);
    }

    public SessionUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Aggiorna l'utente della sessione corrente.
     * Sincronizza automaticamente la proprietà osservabile del nome utente.
     */
    public void setCurrentUser(SessionUser user) {
        this.currentUser = user;
        if (user != null) {
            username.set(user.username());
        } else {
            username.set(""); // Pulisce la property in caso di disconnessione (logout)
        }
    }

    public UserSettings getCurrentSettings() {
        return currentSettings;
    }

    public void setCurrentSettings(UserSettings s) {
        this.currentSettings = s;
    }

    public ScoreModel getCurrentSaveData() {
        return currentSaveData.get();
    }

    public void setCurrentSaveData(ScoreModel data) {
        currentSaveData.set(data);
    }

    public ObjectProperty<ScoreModel> saveDataProperty() {
        return currentSaveData;
    }

    /**
     * Restituisce il valore osservabile del nome utente, utile per effettuare
     * il binding diretto sui nodi testuali dell'interfaccia grafica (es. Label di benvenuto).
     */
    public ObservableValue<String> usernameProperty() {
        return username;
    }
}