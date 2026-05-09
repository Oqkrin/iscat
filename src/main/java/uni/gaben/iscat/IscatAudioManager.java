package uni.gaben.iscat;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.game.utils.settings.AudioSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IscatAudioManager {

    private static IscatAudioManager instance;
    private MediaPlayer bgmPlayer;
    private final Map<String, AudioClip> sfxMap = new HashMap<>();

    // Inizializziamo con i valori di GameSettings
    private double bgmVolume = AudioSettings.VOLUME_BGM;
    private double sfxVolume = AudioSettings.VOLUME_SFX;

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
        this.bgmVolume = AudioSettings.VOLUME_BGM;
        this.sfxVolume = AudioSettings.VOLUME_SFX;

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
            var res = getClass().getResource(path);
            if (res == null) {
                System.err.println("FILE NON TROVATO: " + path);
                return;
            }
            String resource = res.toExternalForm();
            AudioClip clip = new AudioClip(resource);
            sfxMap.put(name, clip);
        } catch (Exception e) {
            System.err.println("ERRORE CARICAMENTO (" + path + "): " + e.getMessage());
            e.printStackTrace(); // <--- Questo ti dirà se il problema è il formato del file
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
        AudioSettings.VOLUME_BGM = volume; // Teniamo sincronizzato GameSettings
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(volume);
        }
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;
        AudioSettings.VOLUME_SFX = volume;
    }
}