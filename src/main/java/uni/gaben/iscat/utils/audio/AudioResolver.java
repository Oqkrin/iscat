package uni.gaben.iscat.utils.audio;

import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Resolves sound effects with the same custom → core → internal priority.
 * Looks in:
 * 1. entities/audio/SFX/custom/{soundName}.wav
 * 2. entities/audio/SFX/core/{soundName}.wav
 * 3. internal classpath /uni/gaben/iscat/audio/SFX/{soundName}.wav
 */
public final class AudioResolver {
    private AudioResolver() {
        /* This utility class should not be instantiated */
    }


    public static URL resolve(String soundName) {
        String fileName = soundName.endsWith(".wav") ? soundName : soundName + ".wav";

        Path root = ExternalResourceResolver.getEntitiesRoot();
        if (root != null) {
            Path customPath = root.resolve("audio/SFX/custom/" + fileName);
            if (Files.isRegularFile(customPath)) {
                try { return customPath.toUri().toURL(); } catch (Exception ignored) {}
            }
            Path corePath = root.resolve("audio/SFX/core/" + fileName);
            if (Files.isRegularFile(corePath)) {
                try { return corePath.toUri().toURL(); } catch (Exception ignored) {}
            }
        }

        String internalPath = "/uni/gaben/iscat/audio/SFX/" + fileName;
        URL url = AudioResolver.class.getResource(internalPath);
        if (url == null) {
            System.err.println("[AudioResolver] SFX not found: " + internalPath);
        }
        return url;
    }
}