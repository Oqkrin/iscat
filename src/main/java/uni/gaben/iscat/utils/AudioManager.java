package uni.gaben.iscat.utils;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.user.UserSettings;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Centralised audio manager for BGM and SFX.
 * <p>
 * Now uses {@link ExternalResourceResolver} to first check for external
 * audio files (e.g. in an ./entities folder next to the JAR) before
 * falling back to classpath resources.
 */
public final class AudioManager {

    private AudioManager() {}

    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    private Random random = new Random();
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

    // ── Volume controls (unchanged, except using internal paths) ────────────
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
            bgmPlayer.setVolume(getBgmVolume() * getMasterVolume());
        }
    }

    // ── BGM playback (now with external‑first resolution) ───────────────────
    /**
     * Plays a BGM track. If the same track is already playing, nothing happens.
     *
     * @param internalPath The classpath path (e.g. "/uni/gaben/iscat/audio/BGM/…")
     * @param loop         true to loop indefinitely
     */
    public void playBGM(String internalPath, boolean loop) {
        if (currentBgmPath != null && currentBgmPath.equals(internalPath) && bgmPlayer != null) {
            return;
        }
        stopBGM();

        // 1. Try external file first
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
            currentBgmPath = internalPath;  // track by logical path
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

    // ── SFX loading & playback (external‑first) ─────────────────────────────
    /**
     * Scans a directory for .wav files, combining external and internal
     * resources (external files override internal ones with the same name).
     */
    public void loadAllSFX(String relativeDirectory) {
        // 1. External directory scan
        if (ExternalResourceResolver.getEntitiesRoot() != null) {
            Path extDir = ExternalResourceResolver.getEntitiesRoot()
                    .resolve(relativeDirectory.replaceFirst("^/", ""));
            if (Files.isDirectory(extDir)) {
                scanAndLoadSFXFromPath(extDir);
            }
        }

        // 2. Internal classpath scan (fallback)
        try {
            URI uri = Objects.requireNonNull(
                    getClass().getResource(relativeDirectory),
                    "[AudioManager] Internal SFX directory not found: " + relativeDirectory
            ).toURI();

            Path internalPath = (uri.getScheme().equals("jar"))
                    ? FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(relativeDirectory)
                    : Paths.get(uri);

            try (Stream<Path> walk = Files.walk(internalPath, 1)) {
                walk.filter(p -> p.getFileName().toString().endsWith(".wav"))
                        .forEach(p -> {
                            String fileName = p.getFileName().toString();
                            String sfxName = stripExtension(fileName);
                            // Only load if not already overridden by an external version
                            if (!sfxMap.containsKey(sfxName)) {
                                loadSFXFromClasspath(sfxName, relativeDirectory + "/" + fileName);
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("[AudioManager] Error scanning internal SFX: " + e.getMessage());
        }
    }

    /** Loads an SFX by name from an external path (or classpath fallback). */
    public void loadSFX(String name, String internalPath) {
        // Already loaded?
        if (sfxMap.containsKey(name)) return;

        URL resolvedUrl = resolveAudioResource(internalPath);
        if (resolvedUrl != null) {
            sfxMap.put(name, new AudioClip(resolvedUrl.toExternalForm()));
            System.out.println("[AudioManager] Loaded SFX: " + name + " from " + resolvedUrl);
        } else {
            System.err.println("[AudioManager] SFX not found: " + internalPath);
        }
    }

    /** Plays an SFX (unchanged usage of sfxMap). */
    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip == null) {
            System.err.println("[AudioManager] SFX not in map: " + name);
            return;
        }
        long now = System.currentTimeMillis();
        long lastPlayed = lastPlayedMap.getOrDefault(name, 0L);
        long requiredCooldown = name.equalsIgnoreCase("shoot") ? 90 : DEFAULT_COOLDOWN_MS;
        if (now - lastPlayed < requiredCooldown) return;
        lastPlayedMap.put(name, now);
        clip.play(getSfxVolume() * getMasterVolume());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolves an audio resource: checks external folder first, then classpath.
     * Returns a URL usable by Media/AudioClip, or null if not found anywhere.
     */
    private static URL resolveAudioResource(String internalPath) {
        // External check: mirror the directory structure under entities root
        if (ExternalResourceResolver.getEntitiesRoot() != null) {
            Path externalFile = ExternalResourceResolver.getEntitiesRoot()
                    .resolve(internalPath.replaceFirst("^/", ""));
            if (Files.isRegularFile(externalFile)) {
                try {
                    return externalFile.toUri().toURL();
                } catch (Exception e) {
                    System.err.println("[AudioManager] Cannot convert external file to URL: " + externalFile);
                }
            }
        }
        // Fallback to classpath
        URL classpathUrl = AudioManager.class.getResource(internalPath);
        if (classpathUrl == null) {
            System.err.println("[AudioManager] Resource not found on classpath: " + internalPath);
        }
        return classpathUrl;
    }

    private void scanAndLoadSFXFromPath(Path directory) {
        try (Stream<Path> walk = Files.walk(directory, 1)) {
            walk.filter(p -> p.getFileName().toString().endsWith(".wav"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String sfxName = stripExtension(fileName);
                        // Load external SFX directly, overriding any internal one
                        try {
                            URL fileUrl = p.toUri().toURL();
                            sfxMap.put(sfxName, new AudioClip(fileUrl.toExternalForm()));
                            System.out.println("[AudioManager] Loaded external SFX: " + sfxName + " from " + fileUrl);
                        } catch (Exception e) {
                            System.err.println("[AudioManager] Failed to load external SFX: " + p);
                        }
                    });
        } catch (Exception e) {
            System.err.println("[AudioManager] Error scanning external SFX: " + e.getMessage());
        }
    }

    private void loadSFXFromClasspath(String name, String classpathPath) {
        URL url = getClass().getResource(classpathPath);
        if (url != null) {
            sfxMap.put(name, new AudioClip(url.toExternalForm()));
        } else {
            System.err.println("[AudioManager] Internal SFX not found: " + classpathPath);
        }
    }

    public String getRandomSfxKey() {
        if (sfxMap.isEmpty()) return null;
        List<String> keys = new ArrayList<>(sfxMap.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}