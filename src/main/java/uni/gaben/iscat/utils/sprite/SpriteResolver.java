package uni.gaben.iscat.utils.sprite;

import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Risolutore di risorse grafiche per gli sprite (sprite sheet).
 * Gestisce la ricerca e il caricamento dei file `.png` applicando la precedenza rigorosa:
 * <ol>
 * <li>Esterna (Custom): {@code entities/sprites/custom/{cartella}/{nome}.png}</li>
 * <li>Esterna (Core): {@code entities/sprites/core/{cartella}/{nome}.png}</li>
 * <li>Interna (Classpath): {@code /uni/gaben/iscat/sprites/{cartella}/{nome}.png}</li>
 * </ol>
 */
public final class SpriteResolver {

    private SpriteResolver() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Risolve il percorso di uno sprite e restituisce il relativo {@link InputStream} operativo,
     * effettuando i controlli a cascata sui percorsi esterni prima di ripiegare sul classpath.
     *
     * @param folder     La sotto-cartella di destinazione (es. "players", "enemies").
     * @param spriteName Il nome del file d'immagine (con o senza estensione .png).
     * @return L'{@link InputStream} della risorsa localizzata, o {@code null} se l'immagine non è stata trovata.
     */
    public static InputStream resolve(String folder, String spriteName) {
        // Normalizza il nome del file assicurando la presenza dell'estensione .png
        String fileName = spriteName.toLowerCase().endsWith(".png") ? spriteName : spriteName + ".png";

        Path root = ExternalResourceResolver.getEntitiesRoot();
        if (root != null) {
            // 1. Controllo nel percorso Custom (Modding/Texture pack utente)
            Path customPath = root.resolve("sprites/custom/" + folder + "/" + fileName);
            if (Files.isRegularFile(customPath)) {
                try {
                    return Files.newInputStream(customPath);
                } catch (Exception ignored) {}
            }

            // 2. Controllo nel percorso Core esterno (Asset base aggiornabili esternamente)
            Path corePath = root.resolve("sprites/core/" + folder + "/" + fileName);
            if (Files.isRegularFile(corePath)) {
                try {
                    return Files.newInputStream(corePath);
                } catch (Exception ignored) {}
            }
        }

        // 3. Fallback definitivo sulle risorse grafiche predefinite del Classpath
        String internalCustomPath = "/uni/gaben/iscat/sprites/custom/" + folder + "/" + fileName;
        InputStream is = SpriteResolver.class.getResourceAsStream(internalCustomPath);
        if (is != null) return is;

        String internalCorePath = "/uni/gaben/iscat/sprites/core/" + folder + "/" + fileName;
        is = SpriteResolver.class.getResourceAsStream(internalCorePath);
        if (is != null) return is;

        String internalPath = "/uni/gaben/iscat/sprites/" + folder + "/" + fileName;
        is = SpriteResolver.class.getResourceAsStream(internalPath);
        if (is == null) {
            System.err.println("[SpriteResolver] Sprite not found in classpath: " + internalPath);
        }
        return is;
    }
}