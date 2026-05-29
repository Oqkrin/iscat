package uni.gaben.iscat.screens.scores;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.bestiary.BestiaryData;
import uni.gaben.iscat.view.AnimatedCanvas;
import uni.gaben.iscat.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoreMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    // Label Icona
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

    private final List<AnimatedCanvas> activeCanvases = new ArrayList<>();

    @FXML
    public void initialize() {
        titleLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    String user = SessionManager.getInstance().usernameProperty().getValue();
                    if (user == null || user.isBlank()) {
                        return "PLAYER SCORE";
                    }
                    return user.toUpperCase() + " SCORE";
                }, SessionManager.getInstance().usernameProperty())
        );

        applyIconButton(exitBtn,           "fas-sign-out-alt");
        applyIconLabel(lblBestScore,     "fas-trophy");
        applyIconLabel(lblTotalEnemies,  "fas-skull");
        applyIconLabel(lblBestTime,      "fas-stopwatch");
        applyIconLabel(lblDamageTaken,   "fas-heart-broken");
        applyIconLabel(lblDamageCaused,  "fas-crosshairs");
        applyIconLabel(lblBoosts,        "fas-bolt");

        setupCornerMobs();

        SessionManager.getInstance().saveDataProperty().addListener(
                (obs, old, data) -> refresh(data)
        );
        refresh(SessionManager.getInstance().getCurrentSaveData());
    }

    private void setupCornerMobs() {
        try {
            BestiaryData bestiaryData = new BestiaryData();
            Map<String, BestiaryData.Enemy> enemiesMap = bestiaryData.loadEnemies();

            if (enemiesMap == null || enemiesMap.isEmpty()) return;

            List<BestiaryData.Enemy> enemyList = new ArrayList<>(enemiesMap.values());
            StackPane[] containers = { previewNW, previewNE, previewSW, previewSE };

            for (int i = 0; i < containers.length; i++) {
                if (containers[i] == null) continue;

                BestiaryData.Enemy enemy = enemyList.get(i % enemyList.size());

                AnimatedCanvas canvas = new AnimatedCanvas(140.0);
                canvas.setFrameDuration(0.20);
                canvas.loadSkin(enemy.sprite(), enemy.frameW(), enemy.frameH());

                containers[i].getChildren().add(canvas);
                activeCanvases.add(canvas);
            }
        } catch (Exception e) {
            System.err.println("[ScoreMenuController] Impossibile caricare i mob d'angolo: " + e.getMessage());
        }
    }

    private void refresh(SaveData data) {
        if (data == null) return;

        valBestScore.setText(String.valueOf(data.score()));
        valTotalEnemies.setText(String.valueOf(data.deaths()));
        valBestTime.setText(formatTime(data.bestTime()));
        valDamageTaken.setText(String.valueOf(data.totalDamageReceived()));
        valDamageCaused.setText(String.valueOf(data.totalDamageDealt()));
        // valBoosts.setText(String.valueOf(data.boosts()));
    }

    private String formatTime(int seconds) {
        if (seconds <= 0) return "00:00";
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        titleLabel.textProperty().unbind();

        for (AnimatedCanvas canvas : activeCanvases) {
            canvas.stop();
        }
        activeCanvases.clear();

        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }
}