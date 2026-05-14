package uni.gaben.iscat.gamenex.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.utils.settings.AudioSettings;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.function.Consumer;

/**
 * Full-screen pause overlay for Gamenex.
 * Handles game state and system settings.
 */
public class GamenexPauseMenu extends VBox {

    public GamenexPauseMenu(GamenexController controller) {
        getStyleClass().addAll("spacing-lg");
        setAlignment(Pos.CENTER);
        
        // Full screen dark overlay
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("GAME PAUSED");
        title.setStyle(TipografiaAurea.HEADLINE[TipografiaAurea.LARGE] + "-fx-text-fill: white; -fx-letter-spacing: 8;");

        // SETTINGS BOX
        VBox settingsBox = new VBox(20);
        settingsBox.setAlignment(Pos.CENTER);
        settingsBox.setMaxWidth(400);
        settingsBox.setStyle("-fx-padding: 40; -fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // BGM SLIDER
        createLabeledSlider(settingsBox, "MUSIC VOLUME", 0, 1, AudioSettings.VOLUME_BGM, 
            v -> IscatAudioManager.getInstance().setBgmVolume(v));

        // SFX SLIDER
        createLabeledSlider(settingsBox, "SFX VOLUME", 0, 1, AudioSettings.VOLUME_SFX, 
            v -> IscatAudioManager.getInstance().setSfxVolume(v));

        // FPS COUNTER
        CheckBox fpsCheck = new CheckBox("SHOW FPS COUNTER");
        fpsCheck.getStyleClass().add("check-box");
        fpsCheck.setSelected(controller.isShowFps());
        fpsCheck.selectedProperty().addListener((obs, oldV, newV) -> controller.setShowFps(newV));
        settingsBox.getChildren().add(fpsCheck);

        // BUTTONS
        Button resumeBtn = createBigButton("RESUME GAME");
        resumeBtn.setOnAction(e -> controller.togglePause());

        Button menuBtn = createBigButton("QUIT TO MENU");
        menuBtn.setOnAction(e -> {
            controller.setPaused(false); // Unpausa il menu
            javafx.scene.Parent actualRoot = this.getScene().getRoot();
            IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, (StackPane) actualRoot);
        });

        Button quitBtn = createBigButton("QUIT GAME");
        quitBtn.setStyle("-fx-border-color: rgba(255, 50, 50, 0.3);");
        quitBtn.setOnAction(e -> System.exit(0));

        getChildren().addAll(title, settingsBox, resumeBtn, menuBtn, quitBtn);
    }
        
        // Bind visibility to controller's pause state (via model)
        // Note: In a real app we'd bind this in the scene, but we can do it here if we have the model
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
