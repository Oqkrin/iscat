package uni.gaben.iscat;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import uni.gaben.iscat.game.utils.settings.AudioSettings;

import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Gestione centralizzata dell'audio (BGM + SFX) per ISCAT.
 * Singleton thread-safe tramite initialization-on-demand holder.
 */
public class IscatAudioManager {

    // --- Singleton ---

    private IscatAudioManager() {}

    private static final class Holder {
        private static final IscatAudioManager INSTANCE = new IscatAudioManager();
    }

    public static IscatAudioManager getInstance() {
        return Holder.INSTANCE;
    }

    // --- Stato interno ---

    private MediaPlayer bgmPlayer;
    private final Map<String, AudioClip> sfxMap = new HashMap<>();

    private double bgmVolume = AudioSettings.VOLUME_BGM;
    private double sfxVolume = AudioSettings.VOLUME_SFX;

    // --- Volumi ---

    /**
     * Sincronizza i volumi interni con quelli correnti in AudioSettings.
     * Da chiamare dopo un caricamento massivo dei settings da file.
     */
    public void syncVolumes() {
        bgmVolume = AudioSettings.VOLUME_BGM;
        sfxVolume = AudioSettings.VOLUME_SFX;
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(bgmVolume);
        }
    }

    public void setBgmVolume(double volume) {
        bgmVolume = volume;
        AudioSettings.VOLUME_BGM = volume;
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(volume);
        }
    }

    public void setSfxVolume(double volume) {
        sfxVolume = volume;
        AudioSettings.VOLUME_SFX = volume;
    }

    // --- BGM ---

    /**
     * Avvia una traccia BGM. Ferma e libera l'eventuale traccia precedente.
     *
     * @param path path della risorsa (es. "/uni/gaben/iscat/audio/bgm/main.mp3")
     * @param loop {@code true} per riproduzione in loop
     */
    public void playBGM(String path, boolean loop) {
        stopBGM();
        try {
            var url = Objects.requireNonNull(
                    getClass().getResource(path),
                    "Risorsa BGM non trovata: " + path
            );
            bgmPlayer = new MediaPlayer(new Media(url.toExternalForm()));
            bgmPlayer.setVolume(bgmVolume);
            if (loop) {
                bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            bgmPlayer.play();
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
        }
    }

    // --- SFX ---

    /**
     * Carica tutti i file .wav presenti nella cartella delle risorse indicata.
     * Il nome del file (senza estensione) viene usato come chiave nella mappa SFX.
     *
     * @param directoryPath path della cartella risorse (es. "/uni/gaben/iscat/audio/SFX")
     */
    public void loadAllSFX(String directoryPath) {
        try {
            URI uri = Objects.requireNonNull(
                    getClass().getResource(directoryPath),
                    "Cartella SFX non trovata: " + directoryPath
            ).toURI();

            if (uri.getScheme().equals("jar")) {
                // Esecuzione da JAR: il FileSystem viene chiuso dopo l'uso
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    scanAndLoadSFX(fs.getPath(directoryPath), directoryPath);
                }
            } else {
                scanAndLoadSFX(Paths.get(uri), directoryPath);
            }

        } catch (NullPointerException e) {
            System.err.println("[AudioManager] Cartella SFX non trovata: " + directoryPath);
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
                        System.out.println("[AudioManager] SFX caricato: " + sfxName);
                    });
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore durante la scansione: " + e.getMessage());
        }
    }

    /**
     * Carica un singolo file SFX e lo registra con il nome indicato.
     *
     * @param name chiave identificativa dell'SFX
     * @param path path della risorsa
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
            System.err.printf(
                    "[AudioManager] Formato non supportato: %s%n" +
                            "  → Converti il file in WAV PCM 16-bit (es. con Audacity).%n", path
            );
        } catch (Exception e) {
            System.err.println("[AudioManager] Errore nel caricamento SFX: " + path);
            e.printStackTrace();
        }
    }

    /**
     * Riproduce un SFX precedentemente caricato.
     *
     * @param name chiave identificativa dell'SFX
     */
    public void playSFX(String name) {
        AudioClip clip = sfxMap.get(name);
        if (clip != null) {
            clip.play(sfxVolume);
        } else {
            System.err.println("[AudioManager] SFX non trovato in mappa: " + name);
        }
    }

    /**
     * Carica tutti gli SFX dalla cartella di default.
     * Da chiamare all'avvio del gioco.
     */
    public void loadDefaultAudio() {
        loadAllSFX("/uni/gaben/iscat/audio/SFX");
    }

    // --- Utility ---

    /** Rimuove l'estensione da un nome di file (es. "boom.wav" → "boom"). */
    private static String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot > 0 ? fileName.substring(0, dot) : fileName;
    }
}