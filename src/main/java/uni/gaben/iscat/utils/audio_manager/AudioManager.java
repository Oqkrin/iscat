package uni.gaben.iscat.utils.audio_manager;

import javafx.scene.media.AudioClip; // Importante per gli SFX
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AudioManager {

    private static AudioManager instance;
    private MediaPlayer bgmPlayer;
    private final Map<String, AudioClip> sfxMap = new HashMap<>();

    private double bgmVolume = 0.5; // Massimo volume musica
    private double sfxVolume = 1.0; // Massimo volume effetti

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // BGM (Background Music)
    public void playBGM(String path, boolean loop) {
        stopBGM();

        try {
            String resource = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
            Media media = new Media(resource);
            bgmPlayer = new MediaPlayer(media);

            // Debug
            bgmPlayer.setOnError(() -> {
                System.err.println("Errore interno MediaPlayer: " + bgmPlayer.getError().getMessage());
            });

            // Debug
            //bgmPlayer.setOnReady(() -> {
            //    System.out.println("L' audio della scena è in riproduzione!");
            //});


            bgmPlayer.setVolume(bgmVolume);
            // se la musica deve loopare allora la facciamo loopare
            if (loop) bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);

            bgmPlayer.play();
        } catch (Exception e) {
            System.err.println("Errore nel caricamento (percorso errato): " + path);
        }
    }

    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose(); // Libera risorse
        }
    }

    // SFX (Sound Effects)
    public void loadSFX(String name, String path) {
        try {
            String resource = Objects.requireNonNull(getClass().getResource(path)).toExternalForm();
            AudioClip clip = new AudioClip(resource);
            sfxMap.put(name, clip);
        } catch (Exception e) {
            System.err.println("Errore caricamento SFX: " + path);
        }
    }

    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip != null) {
            // AudioClip è fatto per essere lanciato "fire and forget"
            clip.play(sfxVolume);
        }
    }
}