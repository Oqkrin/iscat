package uni.gaben.iscat.controller.components.options;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import uni.gaben.iscat.utils.AudioManager;

public class OptionAudioController {
    @FXML private Slider masterSlider;
    @FXML private Slider BGMSlider;
    @FXML private Slider SFXSlider;

    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        AudioManager am = AudioManager.getInstance();

        configuraSlider(masterSlider);
        configuraSlider(BGMSlider);
        configuraSlider(SFXSlider);

        masterSlider.valueProperty().addListener((obs, old, val) -> {
            if (!isUpdating) {
                am.setMasterVolume(val.doubleValue() / 100.0);
            }
        });

        BGMSlider.valueProperty().addListener((obs, old, val) -> {
            if (!isUpdating) {
                am.setBgmVolume(val.doubleValue() / 100.0);
            }
        });

        SFXSlider.valueProperty().addListener((obs, old, val) -> {
            if (!isUpdating) {
                am.setSfxVolume(val.doubleValue() / 100.0);
            }
        });

        bindAudioProperties();
    }


    public void bindAudioProperties() {
        AudioManager am = AudioManager.getInstance();

        isUpdating = true;
        try {
            if (masterSlider != null) masterSlider.setValue(am.getMasterVolume() * 100.0);
            if (BGMSlider != null)     BGMSlider.setValue(am.getBgmVolume() * 100.0);
            if (SFXSlider != null)     SFXSlider.setValue(am.getSfxVolume() * 100.0);
        } finally {
            isUpdating = false;
        }
    }

    private void configuraSlider(Slider slider) {
        if (slider != null) {
            slider.setMin(0.0);
            slider.setMax(100.0);
        }
    }
}