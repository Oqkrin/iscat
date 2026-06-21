package uni.gaben.iscat.controller.components.settings;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import uni.gaben.iscat.utils.audio.AudioManager;

/**
 * Controller per la gestione della schermata delle impostazioni audio.
 * Coordina l'interfaccia grafica basata su {@link Slider} con il sistema di gestione
 * audio globale ({@link AudioManager}), permettendo la regolazione in tempo reale
 * del volume generale, della musica di sottofondo (BGM) e degli effetti sonori (SFX).
 */
public class AudioSettingsController {

    /** Slider per la regolazione del volume master globale. */
    @FXML private Slider masterSlider;

    /** Slider per la regolazione del volume della musica di sottofondo (BGM). */
    @FXML private Slider BGMSlider;

    /** Slider per la regolazione del volume degli effetti sonori (SFX). */
    @FXML private Slider SFXSlider;

    /** * Flag di controllo per prevenire loop di aggiornamento ricorsivi
     * durante la sincronizzazione bidirezionale delle proprietà.
     */
    private boolean isUpdating = false;

    /**
     * Inizializza il componente configurando i limiti degli slider e
     * registrando i listener per catturare le modifiche ai volumi.
     * I valori degli slider (0-100) vengono normalizzati in formato decimale (0.0-1.0)
     * prima di essere passati all'{@link AudioManager}.
     */
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

    /**
     * Sincronizza lo stato visivo degli slider con i valori di volume attuali
     * memorizzati nell'{@link AudioManager}. I valori decimali (0.0-1.0) vengono
     * convertiti in percentuale (0-100). Utilizza internamente il flag {@code isUpdating}
     * per evitare la riattivazione dei listener di modifica durante il processo.
     */
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

    /**
     * Configura i parametri di base di uno slider impostando il range di valori consentito.
     * Imposta il valore minimo a {@code 0.0} e il valore massimo a {@code 100.0}.
     *
     * @param slider Lo slider da configurare, può essere {@code null}.
     */
    private void configuraSlider(Slider slider) {
        if (slider != null) {
            slider.setMin(0.0);
            slider.setMax(100.0);
        }
    }
}