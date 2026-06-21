package uni.gaben.iscat.utils.audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.ExternalResourceResolver;
import uni.gaben.iscat.utils.SessionManager;

import java.net.URL;
import java.util.*;

/**
 * Gestore centralizzato del comparto audio del gioco (BGM e SFX).
 * Applica una strategia di risoluzione "external-first" dando priorità alle risorse
 * esterne (custom/override) rispetto alle risorse interne integrate nel classpath.
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

    /**
     * Mappa ciascuna vista logica del gioco ({@link IscatViews}) al rispettivo percorso
     * interno della traccia di background (BGM).
     */
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

    private void updateBgmPlayerVolume() {
        if (bgmPlayer != null) {
            UserSettings settings = SessionManager.getInstance().getCurrentSettings();
            if (settings != null) {
                bgmPlayer.setVolume(settings.getVolumeBgm() * settings.getVolumeMaster());
            }
        }
    }

    /**
     * Avvia la riproduzione di una traccia musicale di sottofondo.
     * Se la traccia è già in esecuzione, la chiamata viene ignorata per evitare interruzioni.
     */
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

    /**
     * Interrompe la riproduzione della BGM corrente e ne rilascia le risorse.
     */
    public void stopBGM() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
            currentBgmPath = "";
        }
    }

    /**
     * Carica un singolo effetto sonoro identificato per nome.
     * Sfrutta {@link AudioResolver} per cercare prima nelle cartelle esterne (custom, core)
     * e infine ripiegare sulle risorse interne nel classpath.
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
     * Riproduce un effetto sonoro, caricandolo dinamicamente *on-demand* se non presente in cache.
     * Include un controllo interno di cooldown temporale per evitare sovrapposizioni sgradevoli.
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

        // Calcola il cooldown richiesto (lo sparo ha un intervallo personalizzato a 90ms)
        long requiredCooldown = "shoot".equalsIgnoreCase(name.trim()) ? 90 : DEFAULT_COOLDOWN_MS;
        if (now - lastPlayed < requiredCooldown) return;

        lastPlayedMap.put(name, now);
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        double volume = (settings != null) ? settings.getVolumeSfx() * settings.getVolumeMaster() : 1.0;
        clip.play(volume);
    }

    /**
     * Restituisce una chiave SFX casuale tra quelle attualmente memorizzate in cache.
     */
    public String getRandomSfxKey() {
        if (sfxMap.isEmpty()) return null;
        List<String> keys = new ArrayList<>(sfxMap.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    /**
     * Esegue il pre-caricamento degli SFX di default presenti nel percorso interno.
     */
    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    /**
     * Effettua la scansione di una directory interna del classpath per individuare e registrare
     * tutti i file audio `.wav` di sistema (senza sovrascrivere eventuali override esterni già caricati).
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
                // Gestione robusta del FileSystem JAR tramite blocco try-with-resources
                try (java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    internalPath = fs.getPath(relativeDirectory);
                    scanAndMapFiles(internalPath, relativeDirectory);
                }
            } else {
                internalPath = java.nio.file.Paths.get(dirUrl.toURI());
                scanAndMapFiles(internalPath, relativeDirectory);
            }

        } catch (Exception e) {
            System.err.println("[AudioManager] Error scanning internal SFX: " + e.getMessage());
        }
    }

    private void scanAndMapFiles(java.nio.file.Path rootPath, String relativeDirectory) throws java.io.IOException {
        try (java.util.stream.Stream<java.nio.file.Path> walk = java.nio.file.Files.walk(rootPath, 1)) {
            walk.filter(java.nio.file.Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".wav"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        String sfxName = fileName.substring(0, fileName.lastIndexOf('.'));
                        if (!sfxMap.containsKey(sfxName)) {
                            URL resource = getClass().getResource(relativeDirectory + "/" + fileName);
                            if (resource != null) {
                                sfxMap.put(sfxName, new AudioClip(resource.toExternalForm()));
                            }
                        }
                    });
        }
    }

    /**
     * Risolve l'URI di una risorsa audio (utilizzato principalmente dalle BGM)
     * verificando preventivamente la presenza nel root esterno e ripiegando sul classpath in seconda istanza.
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