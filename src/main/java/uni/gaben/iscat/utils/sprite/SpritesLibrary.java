package uni.gaben.iscat.utils.sprite;

import java.util.HashMap;
import java.util.Map;


// Cache Library per Cachare gli sprite come piace a lui ^
public class SpritesLibrary {
    private static final SpritesLibrary instance = new SpritesLibrary();
    public static SpritesLibrary getInstance() { return instance; }

    // Cache degli asset: Path -> SpriteAsset
    private final Map<String, SpriteSheetsParser> assets = new HashMap<>();

    /**
     * Recupera uno SpriteAsset esistente o lo crea se è la prima volta.
     */
    public SpriteSheetsParser getSprite(String path, int spriteWidth, int spriteHeight) {
        return assets.computeIfAbsent(path, p -> new SpriteSheetsParser(p, spriteWidth, spriteHeight));
    }
}