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

    @FXML private Label valBestScore;
    @FXML private Label valTotalEnemies;
    @FXML private Label valBestTime;
    @FXML private Label valDamageTaken;
    @FXML private Label valDamageCaused;
    @FXML private Label valBoosts;

    @FXML private BorderPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Button exitBtn;

    @FXML private StackPane previewNW;
    @FXML private StackPane previewNE;
    @FXML private StackPane previewSW;
    @FXML private StackPane previewSE;

    /** Lista dei canvas animati attivi negli angoli della schermata, tracciati per la corretta disallocazione. */
    private final List<AnimatedCanvas> activeCanvases = new ArrayList<>();

    /**
     * Inizializza la schermata configurando i vincoli di binding del titolo con lo username dell'utente,
     * applicando i glifi iconici ai relativi componenti grafici e avviando l'ascolto delle variazioni
     * sui dati di salvataggio per l'aggiornamento in tempo reale delle metriche.
     */
    @FXML
    public void initialize() {
        registerEscHandler();

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

        setupCornerMobs();

        // Monitoraggio del file di salvataggio per forzare il refresh dei dati a schermo
        SessionManager.getInstance().saveDataProperty().addListener(
                (obs, old, data) -> refresh(data)
        );
        refresh(SessionManager.getInstance().getCurrentSaveData());
    }

    /**
     * Recupera le definizioni dal Bestiario caricate dal database e istanzia dei canvas animati
     * distribuiti nei contenitori posizionati ai quattro angoli della schermata dei punteggi.
     */
    private void setupCornerMobs() {
        try {
            int userId = 0;
            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                userId = user.id();
            }

            BestiaryModel bestiaryModel = new BestiaryModel();
            Map<String, GenericEntitySettings> enemiesMap = bestiaryModel.loadEnemies(userId);

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
     * Interroga la mappa dei contatori del Bestiario per calcolare la somma cumulativa e reale di tutte le uccisioni.
     *
     * @param data Oggetto contenente i dati e i record statistici aggregati dell'utente.
     */
    private void refresh(ScoreModel data) {
        if (data == null) return;

        valBestScore.setText(String.valueOf(data.score()));

        int totalKills = 0;
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            BestiaryModel bestiaryModel = new BestiaryModel();
            bestiaryModel.loadEnemies(user.id());

            // Somma dei contatori di uccisioni estratti dalla mappa di associazione delle tabelle extra
            totalKills = bestiaryModel.getKillCounts().values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        } else {
            // Fallback sul dato locale aggregato se la sessione non è temporaneamente raggiungibile
            totalKills = data.deaths();
        }

        valTotalEnemies.setText(String.valueOf(totalKills));
        valBestTime.setText(formatTime(data.bestTime()));
        valDamageTaken.setText(String.valueOf(data.totalDamageReceived()));
        valDamageCaused.setText(String.valueOf(data.totalDamageDealt()));
    }

    /**
     * Formatta un valore espresso in secondi nel formato standard MM:SS.
     */
    private String formatTime(int seconds) {
        if (seconds <= 0) return "00:00";
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
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
        activeCanvases.forEach(AnimatedCanvas::stop);
        activeCanvases.clear();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    private void handleBackAction(ActionEvent event) { handleBack(); }

    @Override
    public Pane getRootPane() { return rootPane; }
}