package uni.gaben.iscat.game.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.model.GameSettings;

public class OptionsMenu extends VBox {

    public OptionsMenu(Runnable onBack) {
        // --- Setup Layout ---
        setSpacing(15);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(20, 20, 20, 0.95); -fx-padding: 30; -fx-border-color: white; -fx-border-width: 2;");
        setMaxSize(450, 600);

        // --- Titolo ---
        Label title = new Label("GAME SETTINGS");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-family: 'Miracode'; -fx-padding: 0 0 10 0;");

        // --- Sezione Audio ---
        VBox audioBox = createSection("AUDIO CONTROL");

        Slider bgmSlider = createLabeledSlider(audioBox, "MUSIC VOLUME", 0, 1, GameSettings.BGM_VOLUME);
        bgmSlider.valueProperty().addListener((obs, oldV, newV) -> {
            GameSettings.BGM_VOLUME = newV.doubleValue();
            IscatAudioManager.getInstance().updateVolumes(); // Metodo da aggiungere all'AudioManager
        });

        Slider sfxSlider = createLabeledSlider(audioBox, "SFX VOLUME", 0, 1, GameSettings.SFX_VOLUME);
        sfxSlider.valueProperty().addListener((obs, oldV, newV) -> {
            GameSettings.SFX_VOLUME = newV.doubleValue();
        });

        // --- Sezione Gameplay ---
        VBox gameplayBox = createSection("GAMEPLAY");

        Slider sensSlider = createLabeledSlider(gameplayBox, "MOUSE SENSITIVITY", 0.1, 2.0, GameSettings.SENSITIVITY);
        sensSlider.valueProperty().addListener((obs, oldV, newV) -> {
            GameSettings.SENSITIVITY = newV.doubleValue();
        });

        CheckBox fpsCheck = new CheckBox("SHOW FPS COUNTER");
        fpsCheck.setStyle("-fx-text-fill: white; -fx-font-family: 'Miracode';");
        fpsCheck.setSelected(GameSettings.SHOW_FPS);
        fpsCheck.selectedProperty().addListener((obs, oldV, newV) -> GameSettings.SHOW_FPS = newV);

        CheckBox shakeCheck = new CheckBox("ENABLE SCREENSHAKE");
        shakeCheck.setStyle("-fx-text-fill: white; -fx-font-family: 'Miracode';");
        shakeCheck.setSelected(GameSettings.SCREENSHAKE_ENABLED);
        shakeCheck.selectedProperty().addListener((obs, oldV, newV) -> GameSettings.SCREENSHAKE_ENABLED = newV);

        gameplayBox.getChildren().addAll(fpsCheck, shakeCheck);

        // --- Pulsante Indietro ---
        Button backBtn = new Button("SAVE & BACK");
        backBtn.getStyleClass().add("menu-button");
        backBtn.setMinWidth(200);
        backBtn.setOnAction(e -> onBack.run());

        // --- Aggiunta finale di tutti i gruppi ---
        getChildren().addAll(title, audioBox, gameplayBox, backBtn);
        setVisible(false);
    }

    // Helper per creare titoli di sezione
    private VBox createSection(String name) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(name);
        l.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px; -fx-font-weight: bold;");
        box.getChildren().add(l);
        return box;
    }

    // Helper per creare Slider con etichetta
    private Slider createLabeledSlider(VBox parent, String labelText, double min, double max, double current) {
        Label l = new Label(labelText);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        Slider s = new Slider(min, max, current);
        s.setMaxWidth(300);
        parent.getChildren().addAll(l, s);
        return s;
    }
}