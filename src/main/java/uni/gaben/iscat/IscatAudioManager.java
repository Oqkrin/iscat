package uni.gaben.iscat;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.game.model.GameSettings; // Importa i tuoi settings
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IscatAudioManager {

    private static IscatAudioManager instance;
    private MediaPlayer bgmPlayer;
    private final Map<String, AudioClip> sfxMap = new HashMap<>();

    // Inizializziamo con i valori di GameSettings
    private double bgmVolume = GameSettings.BGM_VOLUME;
    private double sfxVolume = GameSettings.SFX_VOLUME;

    private IscatAudioManager() {}

    public static IscatAudioManager getInstance() {
        if (instance == null) instance = new IscatAudioManager();
        return instance;
    }

    /**
     * Sincronizza i volumi interni con quelli definiti in GameSettings.
     * Utile se i settings vengono cambiati massivamente o caricati da file.
     */
    public void updateVolumes() {
        this.bgmVolume = GameSettings.BGM_VOLUME;
        this.sfxVolume = GameSettings.SFX_VOLUME;

        // Se la musica sta andando, aggiorniamo il volume istantaneamente
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(bgmVolume);
        }
    }

    // --- BGM ---
    public void playBGM(String path, boolean loop) {
        stopBGM();
        try {
            String resource = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
            Media media = new Media(resource);
            bgmPlayer = new MediaPlayer(media);

            bgmPlayer.setVolume(bgmVolume); // Applica il volume salvato
            if (loop) bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento BGM: " + path);
        }
    }

    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
        }
    }

    // --- SFX ---
    public void loadSFX(String name, String path) {
        try {
            String resource = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
            AudioClip clip = new AudioClip(resource);
            sfxMap.put(name, clip);
        } catch (Exception e) {
            System.err.println("Errore SFX: " + path);
        }
    }

    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip != null) {
            clip.play(sfxVolume);
        }
    }

    // --- SETTERS REATTIVI ---

    public void setBgmVolume(double volume) {
        this.bgmVolume = volume;
        GameSettings.BGM_VOLUME = volume; // Teniamo sincronizzato GameSettings
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(volume);
        }
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
        GameSettings.SFX_VOLUME = volume;
    }
}