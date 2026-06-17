package uni.gaben.iscat.universe.entities.json;

import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.EntityFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EntityJsonLoader {

    private EntityJsonLoader() {
        /* This utility class should not be instantiated */
    }

    /**
     * Scans the given resource directory and returns a future that completes
     * with a list of raw JSON objects (one per .json file found).
     */
    public static CompletableFuture<List<JSONObject>> loadAllFromDirectory(String dirPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL dirUrl = EntityFactory.class.getResource(dirPath);

                if (dirUrl == null) {
                    System.err.println("[EntityJsonLoader] Directory not found: " + dirPath);
                    return Collections.emptyList();
                }

                Path targetPath = resolvePath(dirPath, dirUrl.toURI());
                return loadJsonFilesFromPath(targetPath);

            } catch (URISyntaxException | IOException ex) {
                System.err.println("[EntityJsonLoader] Error scanning directory " + dirPath + ": " + ex.getMessage());
                ex.printStackTrace();
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
    private static List<JSONObject> loadJsonFilesFromPath(Path directoryPath) throws IOException {
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
     * Reads a file and converts it into a JSONObject. Returns null if it fails.
     */
    private static JSONObject readJsonFile(Path filePath) {
        try {
            String jsonText = Files.readString(filePath);
            return new JSONObject(jsonText);
        } catch (Exception ex) {
            System.err.println("[EntityJsonLoader] Failed to read or parse JSON file at " + filePath + ": " + ex.getMessage());
            return null;
        }
    }
}