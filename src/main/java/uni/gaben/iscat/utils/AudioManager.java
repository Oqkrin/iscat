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
 * Gestione centralizzata dell'audio (BGM + SFX) per l'applicazione ISCAT.
 * Gestisce la riproduzione dei sottofondi musicali (BGM) tramite {@link MediaPlayer}
 * e degli effetti sonori (SFX) tramite {@link AudioClip}, supportando il throttling
 * temporale per evitare sovrapposizioni acustiche fastidiose.
 * <p>
 * Implementa il pattern Singleton in modalità thread-safe tramite Holder interno.
 */
public final class AudioManager {


    private AudioManager() {}

    /**
     * Holder interno per l'inizializzazione lazy e thread-safe dell'istanza Singleton.
     */
    private static final class Holder {
        private static final AudioManager INSTANCE = new AudioManager();
    }

    /**
     * Restituisce l'istanza unica dell'AudioManager.
     *
     * @return L'istanza corrente di {@code AudioManager}.
     */
    public static AudioManager getInstance() {
        return Holder.INSTANCE;
    }

    private MediaPlayer bgmPlayer;
    private String currentBgmPath = "";
    private final Map<String, AudioClip> sfxMap = new HashMap<>();
    private final Map<String, Long> lastPlayedMap = new HashMap<>();

    private static final long DEFAULT_COOLDOWN_MS = 30;

    /**
     * Associa in modo statico ogni vista di gioco al rispettivo file audio di sottofondo.
     *
     * @param scene La scena di destinazione.
     * @return Il percorso della risorsa audio associata.
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

    /**
     * Sincronizza i volumi interni con quelli correnti definiti nelle impostazioni utente.
     * Da invocare a seguito di modifiche o caricamenti massivi del profilo utente.
     */
    public void syncVolumes() {
        updateBgmPlayerVolume();
    }

    /**
     * Restituisce il volume Master corrente.
     *
     * @return Il valore del volume master (range 0.0 - 1.0).
     */
    public double getMasterVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeMaster() : 1.0;
    }

    /**
     * Imposta il volume Master e aggiorna asincronamente il database.
     *
     * @param volume Il nuovo livello di volume (range 0.0 - 1.0).
     */
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

    /**
     * Restituisce il volume specifico per il canale BGM.
     *
     * @return Il valore del volume BGM (range 0.0 - 1.0).
     */
    public double getBgmVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeBgm() : 1.0;
    }

    /**
     * Imposta il volume per il canale BGM e aggiorna asincronamente il database.
     *
     * @param volume Il nuovo livello di volume per la musica (range 0.0 - 1.0).
     */
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

    /**
     * Restituisce il volume specifico per il canale degli effetti sonori (SFX).
     *
     * @return Il valore del volume SFX (range 0.0 - 1.0).
     */
    public double getSfxVolume() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        return (settings != null) ? settings.getVolumeSfx() : 0.3;
    }

    /**
     * Imposta il volume per il canale SFX e aggiorna asincronamente il database.
     *
     * @param volume Il nuovo livello di volume per gli effetti (range 0.0 - 1.0).
     */
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
     * Calcola e applica il volume finale sul lettore BGM moltiplicando il canale per il master.
     */
    private void updateBgmPlayerVolume() {
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(getBgmVolume() * getMasterVolume());
        }
    }

    /**
     * Avvia la riproduzione di una traccia BGM. Se una traccia differente è in esecuzione,
     * viene interrotta e rilasciata automaticamente.
     *
     * @param path Il percorso della risorsa audio (es. "/audio/bgm/track.wav").
     * @param loop {@code true} se la traccia deve ripetersi indefinitamente.
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

    /**
     * Interrompe la riproduzione della traccia BGM corrente e ne rilascia le risorse allocate.
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
     * Scansiona e carica in memoria tutti i file audio con estensione .wav presenti nella cartella indicata.
     * Supporta la lettura sia da file system nativo che dall'interno di archivi distribuiti (.jar).
     *
     * @param directoryPath Il percorso della cartella delle risorse (es. "/audio/sfx").
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

    /**
     * Esegue l'analisi effettiva dei file nella directory recuperata, registrando ogni traccia valida.
     */
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
     * Registra un singolo effetto sonoro all'interno del registro di sistema.
     *
     * @param name Nome logico identificativo (chiave di tracciamento).
     * @param path Percorso fisico della risorsa audio.
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
     * Riproduce l'effetto sonoro richiesto previa verifica del cooldown temporale (throttling anti-baccano)
     * e rimodulando l'intensità sonora in base ai canali SFX e Master correnti.
     *
     * @param name Il nome logico dell'effetto sonoro da riprodurre (es. "shoot").
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
     * Seleziona ed estrae una chiave casuale pescata tra gli effetti d'archivio caricati in memoria.
     * Utile per variazioni di feedback audio o riproduzioni randomiche nei menu.
     *
     * @return Una stringa identificativa dell'SFX, oppure {@code null} se il registro è vuoto.
     */
    public String getRandomSfxKey() {
        if (sfxMap.isEmpty()) {
            return null;
        }
        List<String> keys = new ArrayList<>(sfxMap.keySet());
        int randomIndex = new Random().nextInt(keys.size());
        return keys.get(randomIndex);
    }

    /**
     * Avvia il caricamento iniziale massivo caricando tutti gli SFX presenti nella cartella predefinita.
     */
    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}