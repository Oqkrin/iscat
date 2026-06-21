package uni.gaben.iscat.utils.sprite;

import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves sprite sheets using the order:
 * 1. entities/sprites/custom/{folder}/{name}.png
 * 2. entities/sprites/core/{folder}/{name}.png
 * 3. internal classpath /uni/gaben/iscat/sprites/{folder}/{name}.png
 */
public final class SpriteResolver {

    public static InputStream resolve(String folder, String spriteName) {
        String fileName = spriteName.endsWith(".png") ? spriteName : spriteName + ".png";

        Path root = ExternalResourceResolver.getEntitiesRoot();
        if (root != null) {
            // 1. External custom
            Path customPath = root.resolve("sprites/custom/" + folder + "/" + fileName);
            if (Files.isRegularFile(customPath)) {
                try { return Files.newInputStream(customPath); } catch (Exception ignored) {}
            }
            // 2. External core
            Path corePath = root.resolve("sprites/core/" + folder + "/" + fileName);
            if (Files.isRegularFile(corePath)) {
                try { return Files.newInputStream(corePath); } catch (Exception ignored) {}
            }
        }

        // 3. Internal classpath
        String internalPath = "/uni/gaben/iscat/sprites/" + folder + "/" + fileName;
        InputStream is = SpriteResolver.class.getResourceAsStream(internalPath);
        if (is == null) {
            System.err.println("[SpriteResolver] Sprite not found: " + internalPath);
        }
        return is;
    }
}