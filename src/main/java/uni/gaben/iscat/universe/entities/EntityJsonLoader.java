package uni.gaben.iscat.universe.entities;

import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityJsonLoader {

    /**
     * Scans the given resource directory and returns a future that completes
     * with a list of raw JSON objects (one per .json file found).
     */
    public static CompletableFuture<List<JSONObject>> loadAllFromDirectory(String dirPath) {
        return CompletableFuture.supplyAsync(() -> {
            List<JSONObject> jsonObjects = new ArrayList<>();
            try {
                URL dirUrl = EntityFactory.class.getResource(dirPath);
                if (dirUrl == null) {
                    System.err.println("[EntityJsonLoader] Directory non trovata: " + dirPath);
                    return jsonObjects;
                }
                URI uri = dirUrl.toURI();
                Path myPath;
                if (uri.getScheme().equals("jar")) {
                    FileSystem fileSystem;
                    try {
                        fileSystem = FileSystems.getFileSystem(uri);
                    } catch (FileSystemNotFoundException e) {
                        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    myPath = fileSystem.getPath(dirPath);
                } else {
                    myPath = Paths.get(uri);
                }

                try (Stream<Path> walk = Files.walk(myPath, 1)) {
                    List<Path> jsonFiles = walk
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .collect(Collectors.toList());

                    for (Path filePath : jsonFiles) {
                        String jsonText = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                        JSONObject json = new JSONObject(jsonText);
                        jsonObjects.add(json);
                    }
                }
            } catch (Exception ex) {
                System.err.println("[EntityJsonLoader] Errore nella scansione di " + dirPath + ": " + ex.getMessage());
                ex.printStackTrace();
            }
            return jsonObjects;
        });
    }
}