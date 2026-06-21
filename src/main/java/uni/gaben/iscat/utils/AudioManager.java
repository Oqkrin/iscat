package uni.gaben.iscat.utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.user.UserSettings;

import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Gestione centralizzata dell'audio (BGM + SFX) per ISCAT.
 * Singleton thread-safe tramite initialization-on-demand holder.
 */
public class AudioManager {

    // --- Singleton ---

    private AudioManager() {}

    public static String getBgmPath(IscatViews scene) {
        return switch (scene) {
            case LOGIN_MENU    -> "/uni/gaben/iscat/audio/BGM/awesomeness.wav";
            case MAIN_MENU,
                 SKIN_MENU,
                 BESTIARY_MENU,
                 LEADERBOARD_MENU,
                 SCORE_MENU,
                 CREDITS,
                 TUTORIAL_MENU,
                 ENTITY_EDITOR,
                 SETTINGS_MENU -> "/uni/gaben/iscat/audio/BGM/TremLoadingloopl.wav";
            case GAME          -> "/uni/gaben/iscat/audio/BGM/SuperHero_original.wav";
        };
    }

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    // --- Stato interno ---

    private MediaPlayer bgmPlayer;
    private String currentBgmPath = "";
    private final Map<String, AudioClip> sfxMap = new HashMap<>();

    // --- Volumi e Canali ---

    /**
     * Sincronizza i volumi interni con quelli correnti in AudioSettings.
     * Da chiamare dopo un caricamento massivo dei settings da file.
     */
    public void syncVolumes() {
        updateBgmPlayerVolume();
    }

    public double getMasterVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeMaster() : 1.0;
    }

    public void setMasterVolume(double volume) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            settings.setVolumeMaster(volume);
            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateVolume(settings.getUserId(), "MasterVolume", volume)
            );
        }
        updateBgmPlayerVolume();
    }

    public double getBgmVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeBgm() : 1.0;
    }

    public void setBgmVolume(double volume) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            settings.setVolumeBgm(volume);
            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateVolume(settings.getUserId(), "BGMVolume", volume)
            );
        }
        updateBgmPlayerVolume();
    }

    public double getSfxVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeSfx() : 0.3;
    }

    public void setSfxVolume(double volume) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            settings.setVolumeSfx(volume);
            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateVolume(settings.getUserId(), "SFXVolume", volume)
            );
        }
    }

    /**
     * Helper interno per calcolare e aggiornare il volume reale del BGM (Canale * Master)
     */
    private void updateBgmPlayerVolume() {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(getBgmVolume() * getMasterVolume());
        }
    }

    // --- BGM ---

    /**
     * Avvia una traccia BGM. Ferma e libera l'eventuale traccia precedente.
     *
     * @param path path della risorsa (es. "/uni/gaben/iscat/audio/bgm/main.wav")
     * @param loop {@code true} per riproduzione in loop
     */
    public void playBGM(String path, boolean loop) {
        if (currentBgmPath != null && currentBgmPath.equals(path) && bgmPlayer != null) {
            return;
        }
        stopBGM();
        try {
            var url = Objects.requireNonNull(
                    getClass().getResource(path),
                    "Risorsa BGM non trovata: " + path
            );
            bgmPlayer = new MediaPlayer(new Media(url.toExternalForm()));

            // Applica il volume combinato BGM * Master
            updateBgmPlayerVolume();

            if (loop) {
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            bgmPlayer.play();
            currentBgmPath = path;
        } catch (NullPointerException e) {
            System.err.println("[AudioManager] BGM non trovato: " + path);
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore nel caricamento BGM: " + path);
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
            currentBgmPath = "";
        }
    }

    // --- SFX ---

    private final Map<String, Long> lastPlayedMap = new HashMap<>();
    private static final long DEFAULT_COOLDOWN_MS = 30;

    /**
     * Carica tutti i file .wav presenti nella cartella delle risorse indicata.
     */
    public void loadAllSFX(String directoryPath) {
        try {
            URI uri = Objects.requireNonNull(
                    getClass().getResource(directoryPath),
                    "Cartella SFX non trovata: " + directoryPath
            ).toURI();

            if (uri.getScheme().equals("jar")) {
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    scanAndLoadSFX(fs.getPath(directoryPath), directoryPath);
                }
            } else {
                scanAndLoadSFX(Paths.get(uri), directoryPath);
            }

        } catch (NullPointerException e) {
            System.err.println("[AudioManager] Cartella SFX non trovato: " + directoryPath);
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore scansione cartella SFX: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scanAndLoadSFX(Path directory, String resourceBase) {
        try (Stream<Path> walk = Files.walk(directory, 1)) {
            walk.filter(p -> p.getFileName().toString().endsWith(".wav"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String sfxName  = stripExtension(fileName);
                        loadSFX(sfxName, resourceBase + "/" + fileName);
                    });
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore durante la scansione: " + e.getMessage());
        }
    }

    /**
     * Carica un singolo file SFX e lo registra con il nome indicato.
     */
    public void loadSFX(String name, String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("[AudioManager] SFX non trovato: " + path);
            return;
        }
        try {
            sfxMap.put(name, new AudioClip(url.toExternalForm()));
        } catch (UnsupportedOperationException e) {
            System.err.printf("[AudioManager] Formato non supportato: %s%n", path);
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore nel caricamento SFX: " + path);
            e.printStackTrace();
        }
    }

    /**
     * Riproduce un SFX applicando un filtro anti-baccano (Throttling) e calcolando il volume reale.
     *
     * @param name chiave identificativa dell'SFX (es. "shoot")
     */
    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip == null) {
            System.err.println("[AudioManager] SFX non trovato in mappa: " + name);
            return;
        }

        long now = System.currentTimeMillis();
        long lastPlayed = lastPlayedMap.getOrDefault(name, 0L);

        long requiredCooldown = name.equalsIgnoreCase("shoot") ? 90 : DEFAULT_COOLDOWN_MS;

        if (now - lastPlayed < requiredCooldown) {
            return;
        }

        lastPlayedMap.put(name, now);

        clip.play(getSfxVolume() * getMasterVolume());
    }

    /**
     * Carica tutti gli SFX dalla cartella di default.
     */
    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    // --- Utility ---

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}