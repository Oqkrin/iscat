package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.model.BestiaryModel;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.ScoreModel;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.model.user.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @FXML private VBox rootPane;
    @FXML private Label titleLabel;
    @FXML private Button exitBtn;

    @FXML private StackPane previewNW;
    @FXML private StackPane previewNE;
    @FXML private StackPane previewSW;
    @FXML private StackPane previewSE;

    private final List<AnimatedCanvas> activeCanvases = new ArrayList<>();
    private BestiaryModel bestiaryModel;

    @FXML
    public void initialize() {
        registerEscHandler();

        bestiaryModel = new BestiaryModel();

        titleLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    String user = SessionManager.getInstance().usernameProperty().getValue();
                    if (user == null || user.isBlank()) return "PLAYER SCORE";
                    return user.toUpperCase() + " SCORE";
                }, SessionManager.getInstance().usernameProperty())
        );

        ComponentsUtils.applyIconButton(exitBtn,         "fas-sign-out-alt");
        ComponentsUtils.applyIconLabel(lblBestScore,     "fas-trophy");
        ComponentsUtils.applyIconLabel(lblTotalEnemies,  "fas-skull");
        ComponentsUtils.applyIconLabel(lblBestTime,      "fas-stopwatch");
        ComponentsUtils.applyIconLabel(lblDamageTaken,   "fas-heart-broken");
        ComponentsUtils.applyIconLabel(lblDamageCaused,  "fas-crosshairs");
        ComponentsUtils.applyIconLabel(lblBoosts,        "fas-bolt");
        ComponentsUtils.applyIconLabel(lblTimesPlayed,   "fas-clock");
        ComponentsUtils.applyIconLabel(lblLongestTime,   "fas-hourglass-half");

        SessionManager.getInstance().saveDataProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) refresh(newVal);
        });

        ScoreModel cached = SessionManager.getInstance().getCurrentSaveData();
        if (cached != null) {
            refresh(cached);
        }

        loadFromDB();

        setupCornerMobs();
    }

    /**
     * Carica i dati dal DB in modo asincrono.
     * Quando il dato arriva, aggiorna la sessione → il listener chiama refresh().
     */
    private void loadFromDB() {
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        IscatDB.getInstance().executeAsync(() -> {
            ScoreDAO scoreDAO = IscatDB.getInstance().getScoreDAO();
            scoreDAO.createIfNotExists(user.id());
            scoreDAO.load(user.id()).ifPresent(data ->
                    Platform.runLater(() -> SessionManager.getInstance().setCurrentSaveData(data))
            );
        });
    }
    private void setupCornerMobs() {
        try {
            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user == null) return;

            Map<String, EntityRecord> enemiesMap = bestiaryModel.loadEnemies(user.id());
            if (enemiesMap == null || enemiesMap.isEmpty()) return;

            List<EntityRecord> enemyList = new ArrayList<>(enemiesMap.values());
            StackPane[] containers = { previewNW, previewNE, previewSW, previewSE };

            for (int i = 0; i < containers.length; i++) {
                if (containers[i] == null) continue;
                EntityRecord enemy = enemyList.get(i % enemyList.size());

                AnimatedCanvas canvas = new AnimatedCanvas(128.0);
                canvas.setFrameDuration(.5);
                canvas.loadSkin(enemy.spritePath(), enemy.frameW(), enemy.frameH());
                canvas.resize(128.0, 128.0);

                containers[i].getChildren().add(canvas);
                activeCanvases.add(canvas);
            }
        } catch (Exception e) {
            System.err.println("[ScoreMenuController] Impossibile caricare i mob d'angolo: " + e.getMessage());
        }
    }

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

    private String formatNumber(int number) {
        return String.format("%,d", number);
    }

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

    @Override
    public void handleBack() {
        titleLabel.textProperty().unbind();

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