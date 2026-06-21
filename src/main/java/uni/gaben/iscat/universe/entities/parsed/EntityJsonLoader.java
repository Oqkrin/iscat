package uni.gaben.iscat.universe.entities.parsed;

import org.json.JSONObject;
import uni.gaben.iscat.utils.ExternalResourceResolver;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Utilità per il caricamento asincrono e il salvataggio di file JSON relativi alle entità di gioco.
 */
public class EntityJsonLoader {

    private EntityJsonLoader() {}

    /**
     * Contenitore per un oggetto JSON parsed e il suo percorso di origine.
     */
    public record LoadedJson(JSONObject json, String originPath) {
    }

    /**
     * Scansiona una cartella di risorse in modo asincrono e carica tutti i file .json trovati.
     */
    public static CompletableFuture<List<LoadedJson>> loadAllFromDirectory(String dirPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
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
     * Risolve il percorso del file gestendo sia il file system standard che le risorse dentro il file JAR.
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
     * Legge la cartella a livello superficiale (profondità 1) e parsa i file .json.
     */
    private static List<LoadedJson> loadJsonFilesFromPath(Path directoryPath) throws IOException {
        try (Stream<Path> walk = Files.walk(directoryPath, 1)) {
            return walk
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(EntityJsonLoader::readJsonFile)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    /**
     * Legge un singolo file e lo converte in un oggetto LoadedJson (ritorna null se fallisce).
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
     * Salva un oggetto JSONObject su un file nel percorso assoluto specificato.
     */
    public static void saveJsonToFile(JSONObject json, String absolutePath) {
        try {
            Files.writeString(Path.of(absolutePath), json.toString(4), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("[EntityJsonLoader] Failed to save JSON to " + absolutePath + ": " + e.getMessage());
        }
    }
}