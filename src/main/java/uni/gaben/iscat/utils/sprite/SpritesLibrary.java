package uni.gaben.iscat.utils.sprite;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SpritesLibrary {

    private static final SpritesLibrary instance = new SpritesLibrary();
    public static SpritesLibrary getInstance() { return instance; }

    private final Map<String, SpriteSheetsParser> assets = new HashMap<>();

    // ── New explicit method (folder + sprite name) ──────────────────────────
    /**
     * Returns a SpriteSheetsParser for the given logical sprite.
     * The sprite is resolved using the SpriteResolver (custom → core → internal).
     *
     * @param folder      "players" or "enemies"
     * @param spriteName  the sprite name without extension (e.g. "slime")
     * @param frameWidth  frame width in pixels
     * @param frameHeight frame height in pixels
     */
    public SpriteSheetsParser getSprite(String folder, String spriteName,
                                        int frameWidth, int frameHeight) {
        String cacheKey = folder + "/" + spriteName + "_" + frameWidth + "x" + frameHeight;
        return assets.computeIfAbsent(cacheKey, k -> {
            InputStream is = SpriteResolver.resolve(folder, spriteName);
            if (is == null) {
                throw new RuntimeException("Sprite not found: " + folder + "/" + spriteName);
            }
            return new SpriteSheetsParser(is, frameWidth, frameHeight);
        });
    }

    // ── Legacy method (full path like "/uni/.../sprites/enemies/slime.png") ─
    /**
     * Parses a full sprite path into folder + sprite name and delegates to the
     * folder‑based method.  This keeps existing callers (EntityRenderer,
     * AnimatedCanvas) working without changes.
     */
    public SpriteSheetsParser getSprite(String path, int frameWidth, int frameHeight) {
        // Normalise to forward slashes
        String normalised = path.replace('\\', '/');
        String[] parts = normalised.split("/");
        if (parts.length >= 3) {
            String folder = parts[parts.length - 2];                     // "players" or "enemies"
            String fileName = parts[parts.length - 1];
            String spriteName = fileName.endsWith(".png")
                    ? fileName.substring(0, fileName.length() - 4)
                    : fileName;
            return getSprite(folder, spriteName, frameWidth, frameHeight);
        }
        // If the path format is unexpected, try a direct classpath load (shouldn't happen)
        throw new RuntimeException("Cannot parse sprite path: " + path);
    }
}