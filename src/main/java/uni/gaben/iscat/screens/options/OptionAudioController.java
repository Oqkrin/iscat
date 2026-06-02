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
        AudioManager am = AudioManager.getInstance();

        masterSlider.setValue(am.getMasterVolume());
        BGMSlider.setValue(am.getBgmVolume());
        SFXSlider.setValue(am.getSfxVolume());

        masterSlider.valueProperty().addListener((obs, old, val) ->
                am.setMasterVolume(val.doubleValue()));

        BGMSlider.valueProperty().addListener((obs, old, val) ->
                am.setBgmVolume(val.doubleValue()));

        SFXSlider.valueProperty().addListener((obs, old, val) ->
                am.setSfxVolume(val.doubleValue()));
    }
}