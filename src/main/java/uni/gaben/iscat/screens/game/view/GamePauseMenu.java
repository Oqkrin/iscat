package uni.gaben.iscat.screens.game.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.AudioSettings;
import uni.gaben.iscat.screens.game.controller.GameController;

import java.util.function.Consumer;

public class GamePauseMenu extends VBox {

    public GamePauseMenu(GameController controller) {
        getStyleClass().addAll("spacing-lg", "game-pause-overlay");
        setAlignment(Pos.CENTER);

        Label title = new Label("GAME PAUSED");
        title.getStyleClass().add("pause-title");

        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setMaxWidth(400);
        settingsBox.getStyleClass().add("pause-settings-box");

        createLabeledSlider(settingsBox, "MUSIC VOLUME", 0, 1, AudioSettings.VOLUME_BGM,
                AudioManager.getInstance()::setBgmVolume);

        createLabeledSlider(settingsBox, "SFX VOLUME", 0, 1, AudioSettings.VOLUME_SFX,
                AudioManager.getInstance()::setSfxVolume);

        CheckBox fpsCheck = new CheckBox("SHOW FPS COUNTER");
        fpsCheck.getStyleClass().add("check-box");
        fpsCheck.setSelected(controller.isFpsOn());
        fpsCheck.selectedProperty().addListener((obs, oldV, newV) -> controller.setShowFps(newV));
        settingsBox.getChildren().add(fpsCheck);

        CheckBox debugMode = new CheckBox("DEBUG MODE");
        debugMode.getStyleClass().add("check-box");
        debugMode.setSelected(controller.isDebugModeOn());
        debugMode.selectedProperty().addListener((obs, oldV, newV) -> controller.setShowDebugMode(newV));
        settingsBox.getChildren().add(debugMode);

        Button resumeBtn = createBigButton("RESUME GAME");
        resumeBtn.setOnAction(e -> controller.togglePause());

        Button menuBtn = createBigButton("QUIT TO MENU");
        menuBtn.setOnAction(e -> controller.quitToMainMenu());

        Button quitBtn = createBigButton("QUIT GAME");
        quitBtn.getStyleClass().add("btn-danger-outline");
        quitBtn.setOnAction(e -> controller.quitGame());

        getChildren().addAll(title, settingsBox, resumeBtn, menuBtn, quitBtn);
    }

    private Button createBigButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("pulsante-menu");
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        return btn;
    }

    private void createLabeledSlider(VBox parent, String labelText, double min, double max, double current, Consumer<Double> onUpdate) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);

        Label l = new Label(labelText);
        l.getStyleClass().addAll("label-small", "testo-dim");

        Slider s = new Slider(min, max, current);
        s.getStyleClass().add("slider");
        s.valueProperty().addListener((obs, oldV, newV) -> onUpdate.accept(newV.doubleValue()));

        box.getChildren().addAll(l, s);
        parent.getChildren().add(box);
    }
}