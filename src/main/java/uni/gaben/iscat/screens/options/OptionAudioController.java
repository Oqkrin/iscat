package uni.gaben.iscat.screens.options;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import uni.gaben.iscat.utils.AudioManager;

public class OptionAudioController {
    @FXML private Slider masterSlider;
    @FXML private Slider BGMSlider;
    @FXML private Slider SFXSlider;

    @FXML
    public void initialize() {
        masterSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        BGMSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        SFXSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setSfxVolume(val.doubleValue()));
    }
}