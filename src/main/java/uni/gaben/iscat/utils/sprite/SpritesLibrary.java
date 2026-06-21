package uni.gaben.iscat.utils.sprite;

import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.InputStream;
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
    public SpriteSheetsParser getSprite(String relativePath, int spriteWidth, int spriteHeight) {
        String cacheKey = relativePath + "_" + spriteWidth + "x" + spriteHeight;
        return assets.computeIfAbsent(cacheKey, p -> {
            InputStream is = ExternalResourceResolver.resolve(relativePath);
            if (is == null) {
                throw new RuntimeException("Sprite not found (neither external nor internal): " + relativePath);
            }
            return new SpriteSheetsParser(is, spriteWidth, spriteHeight);
        });
    }
}