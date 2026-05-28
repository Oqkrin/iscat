package uni.gaben.iscat.screens.scores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.SessionManager;

public class ScoreMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML private Label bestScore;
    @FXML private Label bestTime;
    @FXML private Label damageCaused;
    @FXML private Label damageTaken;
    @FXML private Label totalEnemies;
    @FXML private BorderPane rootPane;

    @FXML
    public void initialize() {
        SessionManager.getInstance().saveDataProperty().addListener(
                (obs, old, data) -> refresh(data)
        );
        refresh(SessionManager.getInstance().getCurrentSaveData());
    }

    private void refresh(SaveData data) {
        if (data == null) return;
        bestScore.setText("Best Score: " + data.score());
        totalEnemies.setText("Nemici sconfitti: " + data.deaths());
        bestTime.setText("Miglior tempo: " + formatTime(data.bestTime()));
        damageTaken.setText("Danni subiti: " + data.totalDamageReceived());
        damageCaused.setText("Danni causati: " + data.totalDamageDealt());
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }
}