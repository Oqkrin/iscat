package uni.gaben.iscat.controller;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.BestiaryModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.universe.entity.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.model.user.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller per la schermata dei punteggi e delle statistiche globali del giocatore.
 * Gestisce l'esposizione delle metriche di gioco (punteggio massimo, uccisioni totali, tempo, danni)
 * ricavate dal sistema di salvataggio e dal database. Integra un sistema decorativo agli angoli
 * della UI che renderizza i mostri di gioco animati tramite canvas dedicati.
 */
public class ScoreMenuController implements IscatMenuController {

    private StackPane contentRoot;
    private Runnable customBackAction = null;

    @FXML private Label lblBestScore;
    @FXML private Label lblTotalEnemies;
    @FXML private Label lblBestTime;
    @FXML private Label lblDamageTaken;
    @FXML private Label lblDamageCaused;
    @FXML private Label lblBoosts;
    @FXML private Label lblTimesPlayed;
    @FXML private Label lblLongestTime;

    @FXML private Label valBestScore;
    @FXML private Label valTotalEnemies;
    @FXML private Label valBestTime;
    @FXML private Label valDamageTaken;
    @FXML private Label valDamageCaused;
    @FXML private Label valBoosts;
    @FXML private Label valTimesPlayed;
    @FXML private Label valLongestTime;

    @FXML private BorderPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Button exitBtn;

    @FXML private StackPane previewNW;
    @FXML private StackPane previewNE;
    @FXML private StackPane previewSW;
    @FXML private StackPane previewSE;

    /** Lista dei canvas animati attivi negli angoli della schermata, tracciati per la corretta disallocazione. */
    private final List<AnimatedCanvas> activeCanvases = new ArrayList<>();

    private BestiaryModel bestiaryModel;

    private ScoreModel currentScore;

    /**
     * Inizializza la schermata configurando i vincoli di binding del titolo con lo username dell'utente,
     * applicando i glifi iconici ai relativi componenti grafici e avviando l'ascolto delle variazioni
     * sui dati di salvataggio per l'aggiornamento in tempo reale delle metriche.
     */
    @FXML
    public void initialize() {
        registerEscHandler();

        bestiaryModel = new BestiaryModel();

        // Data-binding dinamico del titolo sul nome utente della sessione corrente
        titleLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    String user = SessionManager.getInstance().usernameProperty().getValue();
                    if (user == null || user.isBlank()) {
                        return "PLAYER SCORE";
                    }
                    return user.toUpperCase() + " SCORE";
                }, SessionManager.getInstance().usernameProperty())
        );

        // Applicazione dei font iconici (FontAwesome) a etichette e pulsanti
        ComponentsUtils.applyIconButton(exitBtn,         "fas-sign-out-alt");
        ComponentsUtils.applyIconLabel(lblBestScore,     "fas-trophy");
        ComponentsUtils.applyIconLabel(lblTotalEnemies,  "fas-skull");
        ComponentsUtils.applyIconLabel(lblBestTime,      "fas-stopwatch");
        ComponentsUtils.applyIconLabel(lblDamageTaken,   "fas-heart-broken");
        ComponentsUtils.applyIconLabel(lblDamageCaused,  "fas-crosshairs");
        ComponentsUtils.applyIconLabel(lblBoosts,        "fas-bolt");
        applyIconLabel(lblTimesPlayed,   "fas-clock");
        applyIconLabel(lblLongestTime,   "fas-hourglass-half");

        setupCornerMobs();
        loadInitialData();
    }

    /**
     * Carica i dati iniziali dal database
     */
    private void loadInitialData() {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();
            Optional<ScoreModel> scoreOpt = scoreDAO.load(user.id());

            if (scoreOpt.isPresent()) {
                currentScore = scoreOpt.get();
                SessionManager.getInstance().setCurrentSaveData(currentScore);
                refresh(currentScore);
            } else {
                // Crea nuovo record se non esiste
                scoreDAO.createIfNotExists(user.id());
                currentScore = new ScoreModel(user.id());
                SessionManager.getInstance().setCurrentSaveData(currentScore);
                refresh(currentScore);
            }
        } catch (Exception e) {
            System.err.println("[ScoreMenuController] Errore caricamento score: " + e.getMessage());
        }
    }

    /**
     * Recupera le definizioni dal Bestiario caricate dal database e istanzia dei canvas animati
     * distribuiti nei contenitori posizionati ai quattro angoli della schermata dei punteggi.
     */
    private void setupCornerMobs() {
        try {
            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user == null) return;

            Map<String, GenericEntitySettings> enemiesMap = bestiaryModel.loadEnemies(user.id());

            if (enemiesMap == null || enemiesMap.isEmpty()) return;

            List<GenericEntitySettings> enemyList = new ArrayList<>(enemiesMap.values());
            StackPane[] containers = { previewNW, previewNE, previewSW, previewSE };

            for (int i = 0; i < containers.length; i++) {
                if (containers[i] == null) continue;

                GenericEntitySettings enemy = enemyList.get(i % enemyList.size());

                AnimatedCanvas canvas = new AnimatedCanvas(140.0);
                canvas.setFrameDuration(0.20);
                canvas.loadSkin(enemy.spritePath, enemy.frameW, enemy.frameH);

                containers[i].getChildren().add(canvas);
                activeCanvases.add(canvas);
            }
        } catch (Exception e) {
            System.err.println("[ScoreMenuController] Impossibile caricare i mob d'angolo: " + e.getMessage());
        }
    }

    /**
     * Sincronizza i testi delle etichette della UI con i valori numerici presenti nel DTO di salvataggio.
     *
     * @param data Oggetto contenente i dati e i record statistici aggregati dell'utente.
     */
    private void refresh(ScoreModel data) {
        if (data == null) return;

        valBestScore.setText(formatNumber(data.score()));
        valBestTime.setText(formatTime(data.bestTime()));
        valDamageTaken.setText(formatNumber(data.totalDamageReceived()));
        valDamageCaused.setText(formatNumber(data.totalDamageDealt()));
        valBoosts.setText(formatNumber(data.boostCollected()));
        valTimesPlayed.setText(formatNumber(data.timesPlayed()));
        valLongestTime.setText(formatTime(data.longestTime()));
        valTotalEnemies.setText(formatNumber(data.totalKills()));
    }

    /**
     * Formatta un numero con separatori delle migliaia
     */
    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

    /**
     * Formatta un valore espresso in secondi nel formato standard MM:SS.
     */
    private String formatTime(int seconds) {
        if (seconds <= 0) return "00:00";
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    public void setCustomBackAction(Runnable customBackAction) {
        this.customBackAction = customBackAction;
    }

    /**
     * Gestisce la disattivazione controllata e l'uscita dalla schermata.
     * Rimuove i vincoli di binding del titolo e interrompe i loop di rendering dei canvas
     * per prevenire memory leak o spreco di CPU in background, prima di rientrare nel menu principale.
     */
    @Override
    public void handleBack() {
        titleLabel.textProperty().unbind();

        // Ferma e pulisci tutti i canvas animati
        activeCanvases.forEach(canvas -> {
            canvas.stop();
            if (canvas.getParent() != null) {
                ((StackPane) canvas.getParent()).getChildren().remove(canvas);
            }
        });
        activeCanvases.clear();

        if (customBackAction != null) {
            customBackAction.run();
        } else {
            IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
        }
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        handleBack();
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}