package uni.gaben.iscat.game.hud;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.GameSettings;
import uni.gaben.iscat.game.settings.AudioSettings;
import uni.gaben.iscat.game.settings.EffectsSettings;
import uni.gaben.iscat.game.settings.InputSettings;
import uni.gaben.iscat.game.settings.VisualSettings;

/**
 * Menu opzioni del gioco.
 * Usa CSS classes dal design system aureo.
 */
public class OptionsMenu extends VBox {

    public OptionsMenu(Runnable onBack) {
        // Applica CSS classes invece di inline styles
        getStyleClass().addAll("menu-opzioni", "spacing-md");
        setAlignment(Pos.CENTER);

        // TITOLO
        Label title = new Label("GAME SETTINGS");
        title.getStyleClass().addAll("title-large", "testo-primario");

        // ==========================================================
        // AUDIO
        // ==========================================================

        VBox audioBox = createSection("AUDIO CONTROL");

        // BGM SLIDER
        Slider bgmSlider = createLabeledSlider(audioBox, "MUSIC VOLUME", 0, 1, 
            AudioSettings.VOLUME_BGM);
        bgmSlider.valueProperty().addListener((obs, oldV, newV) -> {
            AudioSettings.VOLUME_BGM = newV.doubleValue();
            IscatAudioManager.getInstance().updateVolumes();
        });

        // SFX SLIDER
        Slider sfxSlider = createLabeledSlider(audioBox, "SFX VOLUME", 0, 1, 
            AudioSettings.VOLUME_SFX);
        sfxSlider.valueProperty().addListener((obs, oldV, newV) ->
            IscatAudioManager.getInstance().setSfxVolume(newV.doubleValue()));

        // ==========================================================
        // GAMEPLAY
        // ==========================================================

        VBox gameplayBox = createSection("GAMEPLAY");

        // MOUSE SENSITIVITY
        Slider sensSlider = createLabeledSlider(gameplayBox, "MOUSE SENSITIVITY", 0.1, 2.0, 
            InputSettings.SENSIBILITA_MOUSE);
        sensSlider.valueProperty().addListener((obs, oldV, newV) ->
            InputSettings.SENSIBILITA_MOUSE = newV.doubleValue());

        // FPS COUNTER
        CheckBox fpsCheck = new CheckBox("SHOW FPS COUNTER");
        fpsCheck.getStyleClass().add("check-box");
        fpsCheck.setSelected(VisualSettings.MOSTRA_FPS);
        fpsCheck.selectedProperty().addListener((obs, oldV, newV) -> 
            VisualSettings.MOSTRA_FPS = newV);

        // SCREENSHAKE
        CheckBox shakeCheck = new CheckBox("ENABLE SCREENSHAKE");
        shakeCheck.getStyleClass().add("check-box");
        shakeCheck.setSelected(EffectsSettings.SCREENSHAKE_ABILITATO);
        shakeCheck.selectedProperty().addListener((obs, oldV, newV) ->
                EffectsSettings.SCREENSHAKE_ABILITATO = newV);

        gameplayBox.getChildren().addAll(fpsCheck, shakeCheck);

        // PULSANTE BACK
        Button backBtn = new Button("SAVE & BACK");
        backBtn.getStyleClass().add("pulsante-menu");
        backBtn.setOnAction(e -> onBack.run());

        // Aggiungi tutto al menu
        getChildren().addAll(title, audioBox, gameplayBox, backBtn);
        setVisible(false);
    }

    /**
     * Crea una sezione con titolo.
     * @param name nome della sezione
     * @return VBox contenitore
     */
    private VBox createSection(String name) {
        VBox box = new VBox();
        box.getStyleClass().addAll("sezione-opzioni", "spacing-sm");
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label l = new Label(name);
        l.getStyleClass().addAll("titolo-sezione", "label-small", "testo-dim");
        box.getChildren().add(l);
        return box;
    }

    /**
     * Crea uno slider con etichetta.
     * @param parent contenitore parent
     * @param labelText testo etichetta
     * @param min valore minimo
     * @param max valore massimo
     * @param current valore corrente
     * @return slider creato
     */
    private Slider createLabeledSlider(VBox parent, String labelText, double min, double max, double current) {
        Label l = new Label(labelText);
        l.getStyleClass().addAll("etichetta-controllo", "label-medium");
        
        Slider s = new Slider(min, max, current);
        s.getStyleClass().add("slider");
        
        parent.getChildren().addAll(l, s);
        return s;
    }
}
