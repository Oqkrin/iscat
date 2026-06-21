package uni.gaben.iscat.utils.sprite;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Registro centralizzato e cache per il caricamento degli sprite sheet.
 * Evita di caricare e processare più volte lo stesso file sul file system,
 * memorizzando in memoria le istanze di {@link SpriteSheetsParser} associate alle entità.
 */
public class SpritesLibrary {

    private static final SpritesLibrary INSTANCE = new SpritesLibrary();

    public static SpritesLibrary getInstance() {
        return INSTANCE;
    }

    private final Map<String, SpriteSheetsParser> assets = new HashMap<>();

    /**
     * Recupera (o crea e memorizza in cache se assente) un parser per lo sprite sheet richiesto.
     * La risorsa viene individuata tramite la catena di priorità del {@link SpriteResolver}.
     *
     * @param folder      La sotto-cartella di destinazione degli asset (es. "players" o "enemies").
     * @param spriteName  Il nome dello sprite senza estensione (es. "slime").
     * @param frameWidth  Larghezza in pixel di un singolo frame.
     * @param frameHeight Altezza in pixel di un singolo frame.
     * @return L'istanza di {@link SpriteSheetsParser} pronta per lo slicing e l'animazione.
     * @throws RuntimeException Se la risorsa grafica non viene localizzata in nessun percorso valido.
     */
    public SpriteSheetsParser getSprite(String folder, String spriteName,
                                        int frameWidth, int frameHeight) {
        String cacheKey = folder + "/" + spriteName + "_" + frameWidth + "x" + frameHeight;

        return assets.computeIfAbsent(cacheKey, k -> {
            InputStream is = SpriteResolver.resolve(folder, spriteName);
            if (is == null) {
                throw new RuntimeException("Sprite asset non trovato nei percorsi validi: " + folder + "/" + spriteName);
            }
            return new SpriteSheetsParser(is, frameWidth, frameHeight);
        });
    }

    /**
     * Scompone un percorso assoluto interno nel rispettivo formato (cartella + nome_file)
     * delegando la logica al metodo di caricamento esplicito.
     * Mantiene la retrocompatibilità con i vecchi componenti del motore grafico (es. EntityRenderer).
     *
     * @param path        Il percorso completo (es. "/uni/gaben/iscat/sprites/enemies/slime.png").
     * @param frameWidth  Larghezza in pixel di un singolo frame.
     * @param frameHeight Altezza in pixel di un singolo frame.
     * @return L'istanza di {@link SpriteSheetsParser} corrispondente.
     * @throws RuntimeException Se il formato del percorso non è compatibile con i pattern supportati.
     */
    public SpriteSheetsParser getSprite(String path, int frameWidth, int frameHeight) {
        String normalised = path.replace('\\', '/');
        String[] parts = normalised.split("/");

        if (parts.length >= 3) {
            String folder = parts[parts.length - 2];
            String fileName = parts[parts.length - 1];

            // Rimuove l'estensione finale se presente
            String spriteName = fileName.toLowerCase().endsWith(".png")
                    ? fileName.substring(0, fileName.length() - 4)
                    : fileName;

            return getSprite(folder, spriteName, frameWidth, frameHeight);
        }

        throw new RuntimeException("Impossibile analizzare e convertire il formato del percorso sprite fornito: " + path);
    }
}