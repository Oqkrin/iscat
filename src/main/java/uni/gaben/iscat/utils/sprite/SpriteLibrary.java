package uni.gaben.iscat.utils.sprite;

import java.util.HashMap;
import java.util.Map;

public class SpriteLibrary {
    private static final SpriteLibrary instance = new SpriteLibrary();
    public static SpriteLibrary getInstance() { return instance; }

    // Cache degli asset: Path -> SpriteAsset
    private final Map<String, SpriteDrawer> assets = new HashMap<>();

    /**
     * Recupera uno SpriteAsset esistente o lo crea se è la prima volta.
     */
    public SpriteDrawer getSprite(String path, int frameWidth, int frameHeight) {
        return assets.computeIfAbsent(path, p -> new SpriteDrawer(p, frameWidth, frameHeight));
    }
}