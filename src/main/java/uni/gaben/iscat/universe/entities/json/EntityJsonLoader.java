package uni.gaben.iscat.universe.entities.json;

import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityJsonLoader {

    private EntityJsonLoader() {
        /* This utility class should not be instantiated */
    }

    public record LoadedJson(JSONObject json, String originPath) {
    }

    /**
     * Scans the given resource directory and returns a future that completes
     * with a list of loaded JSON objects (containing the parsed object and its origin path).
     */
    public static CompletableFuture<List<LoadedJson>> loadAllFromDirectory(String dirPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Strip leading slash and use as relative path for resolver
                String relativeDir = dirPath.startsWith("/") ? dirPath.substring(1) : dirPath;
                List<Path> files = ExternalResourceResolver.listFiles(relativeDir, ".json");
                return files.stream()
                        .map(EntityJsonLoader::readJsonFile)
                        .filter(Objects::nonNull)
                        .toList();
            } catch (IOException e) {
                System.err.println("[EntityJsonLoader] Error scanning directory: " + dirPath + " – " + e.getMessage());
                return Collections.emptyList();
            }
        });
    }

    /**
     * Resolves the correct Path object, handling both standard file systems and JAR resources.
     */
    private static Path resolvePath(String dirPath, URI uri) throws IOException {
        if ("jar".equals(uri.getScheme())) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
            return fileSystem.getPath(dirPath);
        }

        return Paths.get(uri);
    }

    /**
     * Walks the directory surface level (depth 1) and parses all .json files.
     */
    private static List<LoadedJson> loadJsonFilesFromPath(Path directoryPath) throws IOException {
        try (Stream<Path> walk = Files.walk(directoryPath, 1)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(EntityJsonLoader::readJsonFile)
                    .filter(Objects::nonNull) // Filters out any files that failed to read/parse
                    .toList();
        }
    }

    /**
     * Reads a file and converts it into a LoadedJson. Returns null if it fails.
     */
    private static LoadedJson readJsonFile(Path filePath) {
        try {
            String jsonText = Files.readString(filePath);
            return new LoadedJson(new JSONObject(jsonText), filePath.toAbsolutePath().toString());
        } catch (Exception ex) {
            System.err.println("[EntityJsonLoader] Failed to read or parse JSON file at " + filePath + ": " + ex.getMessage());
            return null;
        }
    }

    /**
     * Saves a JSONObject to the specified path string.
     */
    public static void saveJsonToFile(JSONObject json, String absolutePath) {
        try {
            Files.writeString(Path.of(absolutePath), json.toString(4), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("[EntityJsonLoader] Saved JSON to: " + absolutePath);
        } catch (IOException e) {
            System.err.println("[EntityJsonLoader] Failed to save JSON to " + absolutePath + ": " + e.getMessage());
        }
    }
}