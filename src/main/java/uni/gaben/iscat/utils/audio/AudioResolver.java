package uni.gaben.iscat.utils.audio;

import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Risolutore di risorse audio per gli effetti sonori (SFX).
 * Gestisce la ricerca dei file `.wav` applicando la precedenza rigorosa:
 * <ol>
 * <li>Esterna (Custom): {@code entities/audio/SFX/custom/{nome}.wav}</li>
 * <li>Esterna (Core): {@code entities/audio/SFX/core/{nome}.wav}</li>
 * <li>Interna (Classpath): {@code /uni/gaben/iscat/audio/SFX/{nome}.wav}</li>
 * </ol>
 */
public final class AudioResolver {

    private AudioResolver() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Risolve il nome di un effetto sonoro nel rispettivo {@link URL} operativo,
     * effettuando i controlli a cascata sulle cartelle esterne prima di ripiegare sul classpath.
     *
     * @param soundName Il nome del file sonoro (con o senza estensione .wav).
     * @return L'{@link URL} della risorsa localizzata, o {@code null} se non è stato trovato alcun file.
     */
    public static URL resolve(String soundName) {
        // Normalizza il nome del file assicurando la presenza dell'estensione .wav
        String fileName = soundName.toLowerCase().endsWith(".wav") ? soundName : soundName + ".wav";

        Path root = ExternalResourceResolver.getEntitiesRoot();
        if (root != null) {
            // 1. Controllo nel percorso Custom (Modding/Personalizzazioni utente)
            Path customPath = root.resolve("audio/SFX/custom/" + fileName);
            if (Files.isRegularFile(customPath)) {
                try {
                    return customPath.toUri().toURL();
                } catch (Exception ignored) {}
            }

            // 2. Controllo nel percorso Core esterno (Aggiornamenti o pacchetti esterni base)
            Path corePath = root.resolve("audio/SFX/core/" + fileName);
            if (Files.isRegularFile(corePath)) {
                try {
                    return corePath.toUri().toURL();
                } catch (Exception ignored) {}
            }
        }

        // 3. Fallback definitivo sulle risorse interne predefinite del Classpath
        String internalPath = "/uni/gaben/iscat/audio/SFX/" + fileName;
        URL url = AudioResolver.class.getResource(internalPath);
        if (url == null) {
            System.err.println("[AudioResolver] SFX not found in classpath: " + internalPath);
        }
        return url;
    }
}