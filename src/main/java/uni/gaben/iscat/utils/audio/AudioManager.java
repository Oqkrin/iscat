package uni.gaben.iscat.utils.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.ExternalResourceResolver;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.audio.AudioResolver;   // adjust package if needed

import java.net.URL;
import java.util.*;

/**
 * Centralised audio manager for BGM and SFX.
 * Uses external‑first resolution for BGM and SFX (custom → core → internal).
 */
public final class AudioManager {

    private AudioManager() {}

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    private final Random random = new Random();
    private MediaPlayer bgmPlayer;
    private String currentBgmPath = "";
    private final Map<String, AudioClip> sfxMap = new HashMap<>();
    private final Map<String, Long> lastPlayedMap = new HashMap<>();

    private static final long DEFAULT_COOLDOWN_MS = 30;

    // ── BGM path mapping (unchanged) ─────────────────────────────────────────
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

    // ── Volume controls ─────────────────────────────────────────────────────
    public void syncVolumes() { updateBgmPlayerVolume(); }

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

    private void updateBgmPlayerVolume() {
        if (bgmPlayer != null) {
            UserSettings settings = SessionManager.getInstance().getCurrentSettings();
            if (settings != null) {
                bgmPlayer.setVolume(settings.getVolumeBgm() * settings.getVolumeMaster());
            }
        }
    }

    // ── BGM playback (external‑first) ───────────────────────────────────────
    public void playBGM(String internalPath, boolean loop) {
        if (currentBgmPath != null && currentBgmPath.equals(internalPath) && bgmPlayer != null) {
            return;
        }
        stopBGM();

        URL resolvedUrl = resolveAudioResource(internalPath);
        if (resolvedUrl == null) {
            System.err.println("[AudioManager] BGM not found: " + internalPath);
            return;
        }

        try {
            bgmPlayer = new MediaPlayer(new Media(resolvedUrl.toExternalForm()));
            updateBgmPlayerVolume();
            if (loop) {
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            bgmPlayer.play();
            currentBgmPath = internalPath;
        } catch (Exception e) {
            System.err.println("[AudioManager] Error playing BGM: " + internalPath);
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

    // ── SFX (lazy load with AudioResolver – custom → core → internal) ─────
    /**
     * Loads a single SFX by name. Uses {@link AudioResolver} which checks
     * entities/audio/SFX/custom, then core, then internal classpath.
     */
    public void loadSFX(String name) {
        if (sfxMap.containsKey(name)) return;

        URL resolved = AudioResolver.resolve(name);
        if (resolved != null) {
            sfxMap.put(name, new AudioClip(resolved.toExternalForm()));
        } else {
            System.err.println("[AudioManager] SFX not found: " + name);
        }
    }

    /**
     * Plays an SFX, loading it on demand if not already cached.
     */
    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip == null) {
            loadSFX(name);
            clip = sfxMap.get(name);
            if (clip == null) return;
        }

        long now = System.currentTimeMillis();
        long lastPlayed = lastPlayedMap.getOrDefault(name, 0L);
        long requiredCooldown = name.equalsIgnoreCase("shoot") ? 90 : DEFAULT_COOLDOWN_MS;
        if (now - lastPlayed < requiredCooldown) return;

        lastPlayedMap.put(name, now);
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        double volume = (settings != null) ? settings.getVolumeSfx() * settings.getVolumeMaster() : 1.0;
        clip.play(volume);
    }

    /**
     * Returns a random SFX key from the currently loaded map.
     */
    public String getRandomSfxKey() {
        if (sfxMap.isEmpty()) return null;
        List<String> keys = new ArrayList<>(sfxMap.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    /**
     * Preloads SFX from the internal classpath directory.
     * External custom/core overrides are not loaded here; they will be
     * resolved on first use by {@link AudioResolver} when a specific sound is requested.
     */
    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    /**
     * Scans the internal classpath directory for .wav files and loads them
     * into the SFX map if not already present (external overrides are ignored here).
     */
    public void loadAllSFX(String relativeDirectory) {
        try {
            URL dirUrl = getClass().getResource(relativeDirectory);
            if (dirUrl == null) {
                System.err.println("[AudioManager] Internal SFX directory not found: " + relativeDirectory);
                return;
            }

            java.nio.file.Path internalPath;
            if (dirUrl.getProtocol().equals("jar")) {
                java.net.URI uri = dirUrl.toURI();
                java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(uri, Collections.emptyMap());
                internalPath = fs.getPath(relativeDirectory);
            } else {
                internalPath = java.nio.file.Paths.get(dirUrl.toURI());
            }

            java.nio.file.Files.walk(internalPath, 1)
                    .filter(java.nio.file.Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".wav"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String sfxName = fileName.substring(0, fileName.lastIndexOf('.'));
                        if (!sfxMap.containsKey(sfxName)) {
                            // Load from classpath (internal) only if not already overridden externally
                            URL resource = getClass().getResource(relativeDirectory + "/" + fileName);
                            if (resource != null) {
                                sfxMap.put(sfxName, new AudioClip(resource.toExternalForm()));
                            }
                        }
                    });

        } catch (Exception e) {
            System.err.println("[AudioManager] Error scanning internal SFX: " + e.getMessage());
        }
    }

    // ── Resource resolution helpers ─────────────────────────────────────────

    /**
     * Resolves an audio resource (used by BGM) checking external root first,
     * then falling back to classpath.
     */
    private static URL resolveAudioResource(String internalPath) {
        if (ExternalResourceResolver.getEntitiesRoot() != null) {
            java.nio.file.Path externalFile = ExternalResourceResolver.getEntitiesRoot()
                    .resolve(internalPath.replaceFirst("^/", ""));
            if (java.nio.file.Files.isRegularFile(externalFile)) {
                try {
                    return externalFile.toUri().toURL();
                } catch (Exception e) {
                    System.err.println("[AudioManager] Cannot convert external file to URL: " + externalFile);
                }
            }
        }
        URL classpathUrl = AudioManager.class.getResource(internalPath);
        if (classpathUrl == null) {
            System.err.println("[AudioManager] Resource not found on classpath: " + internalPath);
        }
        return classpathUrl;
    }
}