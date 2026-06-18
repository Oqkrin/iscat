package uni.gaben.iscat.view.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseWaveController;
import uni.gaben.iscat.utils.theme.ThemeManager;

public class GameHudBar extends StackPane {

    private static final String FONT_FAMILY  = "Miracode";
    private static final double FONT_SIZE_LG = 22;
    private static final double FONT_SIZE_MD = 15;
    private static final double FONT_SIZE_SM = 12;
    private static final String ICON_TIMER   = "⏱";
    private static final String ICON_KILLS   = "☠";

    private Label timerLabel;
    private Label enemiesCounterLabel;
    private Label waveLabel;
    private Label threatLabel;

    public GameHudBar(GameController controller) {
        buildNodes(controller.getUniverseWaveController());
        bindToWaveController(controller.getUniverseWaveController());

        controller.getGameModel().timerProperty()
                .addListener((obs, oldV, newV) -> updateTimer(newV.intValue()));
        updateTimer(controller.getGameModel().getTimer());
    }

    private void buildNodes(UniverseWaveController wave) {
        // Riga 1
        timerLabel = styledLabel(ICON_TIMER + "  00:00", FONT_SIZE_LG);
        timerLabel.setMinWidth(170);

        Label killsLabel = styledLabel(ICON_KILLS + "  0", FONT_SIZE_LG);
        killsLabel.setMinWidth(170);
        killsLabel.setAlignment(Pos.CENTER_RIGHT);

        killsLabel.textProperty().bind(
                UniverseWaveController.totalKillsProperty().asString(ICON_KILLS + "  %d"));

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox headerRow = new HBox(timerLabel, topSpacer, killsLabel);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setPadding(new Insets(0, 4, 4, 4));

        // Riga 2
        enemiesCounterLabel = styledLabel("Nemici rimanenti: 0 / 0", FONT_SIZE_MD);
        enemiesCounterLabel.setMaxWidth(Double.MAX_VALUE);
        enemiesCounterLabel.setAlignment(Pos.CENTER);

        // Riga 3 - Wave number
        waveLabel = styledLabel("Wave 1", FONT_SIZE_SM);
        waveLabel.setOpacity(0.55);
        waveLabel.setMaxWidth(Double.MAX_VALUE);
        waveLabel.setAlignment(Pos.CENTER);

        // Riga 4 - Threat level
        threatLabel = styledLabel("", FONT_SIZE_SM);
        threatLabel.setOpacity(0.7);
        threatLabel.setMaxWidth(Double.MAX_VALUE);
        threatLabel.setAlignment(Pos.CENTER);

        // Assemblaggio nel VBox
        VBox content = new VBox(6, headerRow, enemiesCounterLabel, waveLabel, threatLabel);
        content.setPadding(new Insets(12, 20, 10, 20));
        content.setAlignment(Pos.CENTER);
        content.setMaxHeight(VBox.USE_PREF_SIZE);
        content.setFillWidth(true);

        getChildren().add(content);

        setStyle("-fx-background-color: transparent;");
        setMaxHeight(USE_PREF_SIZE);
        setPickOnBounds(false);
        setFocusTraversable(false);
        setMouseTransparent(true);
    }

    private void bindToWaveController(UniverseWaveController wave) {
        // Aggiorna il testo quando cambiano i nemici rimanenti o il totale della wave
        wave.enemiesRemainingProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newV.intValue(), wave.waveTotalProperty().get()));
        wave.waveTotalProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                wave.enemiesRemainingProperty().get(), newV.intValue()));

        // Aggiorna la wave label (SOLO numero)
        wave.currentWaveProperty().addListener((obs, oldV, newV) ->
                waveLabel.setText("Wave " + newV.intValue()));

        // Aggiorna la threat label
        wave.currentWaveProperty().addListener((obs, oldV, newV) ->
                threatLabel.setText(wave.getCurrentThreatLevelDisplay()));

        // Inizializza
        refreshEnemiesLabel(wave.getEnemiesRemaining(), wave.getWaveTotal());
        waveLabel.setText("Wave " + wave.getCurrentWave());
        threatLabel.setText(wave.getCurrentThreatLevelDisplay());
    }

    private void refreshEnemiesLabel(int remaining, int total) {
        enemiesCounterLabel.setText("Nemici rimanenti: " + remaining + " / " + total);
    }

    public void updateTimer(int val) {
        int hours   = val / 10000;
        int minutes = (val % 10000) / 100;
        int seconds = val % 100;
        String time = hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
        timerLabel.setText(ICON_TIMER + "  " + time);
    }

    public void rebindToWaveController(UniverseWaveController newWave) {
        // Aggiorna i listener
        newWave.enemiesRemainingProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newV.intValue(), newWave.waveTotalProperty().get()));
        newWave.waveTotalProperty().addListener((obs, oldV, newV) -> refreshEnemiesLabel(
                newWave.enemiesRemainingProperty().get(), newV.intValue()));

        newWave.currentWaveProperty().addListener((obs, oldV, newV) -> {
            waveLabel.setText("Wave " + newV.intValue());
            threatLabel.setText(newWave.getCurrentThreatLevelDisplay());
        });

        // Sincronizza immediatamente
        refreshEnemiesLabel(newWave.getEnemiesRemaining(), newWave.getWaveTotal());
        waveLabel.setText("Wave " + newWave.getCurrentWave());
        threatLabel.setText(newWave.getCurrentThreatLevelDisplay());
    }

    private Label styledLabel(String text, double size) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, size));
        lbl.setTextFill(ThemeManager.getInstance().getAccentPrimary());
        lbl.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");
        lbl.setFocusTraversable(false);
        lbl.setMouseTransparent(true);
        return lbl;
    }
}