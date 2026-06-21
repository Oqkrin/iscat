package uni.gaben.iscat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ExternalResourceResolver {

    private static Path entitiesRoot = null;

    /** Call once at startup, e.g. Path.of("./entities"). */
    public static void init(Path externalEntitiesDir) {
        if (Files.isDirectory(externalEntitiesDir)) {
            entitiesRoot = externalEntitiesDir;
            System.out.println("[Resolver] External entities directory: " + entitiesRoot);
        } else {
            System.out.println("[Resolver] External entities directory not found, using only internal resources.");
        }
    }

    public static Path getEntitiesRoot() { return entitiesRoot; }

    /**
     * Returns an InputStream for a given relative path (like "json/enemies/slime.json"),
     * preferring external file if it exists, otherwise classpath.
     */
    public static InputStream resolve(String relativePath) {
        if (entitiesRoot != null) {
            Path externalFile = entitiesRoot.resolve(relativePath);
            if (Files.isRegularFile(externalFile)) {
                try {
                    return Files.newInputStream(externalFile);
                } catch (IOException e) {
                    System.err.println("[Resolver] Cannot read external file: " + externalFile);
                }
            }
        }
        String cpPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        InputStream is = ExternalResourceResolver.class.getResourceAsStream(cpPath);
        if (is == null) {
            System.err.println("[Resolver] Resource not found: " + cpPath);
        }
        return is;
    }

    /**
     * Scans a relative directory for all files with the given extension (depth 1).
     * Combines external and internal files: if an external file with the same name
     * exists, it replaces the internal one.
     *
     * @param relativeDir e.g. "json/enemies"
     * @param extension   e.g. ".json"
     * @return list of absolute Paths (external or temporary/internal)
     */
    public static List<Path> listFiles(String relativeDir, String extension) throws IOException {
        Map<String, Path> resultMap = new LinkedHashMap<>(); // filename -> path

        // 1. Internal files first (they will be overwritten by external with same name)
        try {
            URI uri = Objects.requireNonNull(
                    ExternalResourceResolver.class.getResource("/" + relativeDir),
                    "Internal directory not found: /" + relativeDir
            ).toURI();

            Path internalDir;
            if ("jar".equals(uri.getScheme())) {
                FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                internalDir = fs.getPath("/" + relativeDir);
            } else {
                internalDir = Paths.get(uri);
            }

            try (Stream<Path> walk = Files.walk(internalDir, 1)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(extension))
                        .forEach(p -> {
                            String name = p.getFileName().toString();
                            resultMap.putIfAbsent(name, p);
                        });
            }
        } catch (Exception e) {
            System.err.println("[Resolver] Error reading internal directory: " + relativeDir + " – " + e.getMessage());
        }

        // 2. External files – overwrite any internal entry with the same filename
        if (entitiesRoot != null) {
            Path extDir = entitiesRoot.resolve(relativeDir);
            if (Files.isDirectory(extDir)) {
                try (Stream<Path> walk = Files.walk(extDir, 1)) {
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(extension))
                            .forEach(p -> {
                                String name = p.getFileName().toString();
                                resultMap.put(name, p);   // overwrites internal if exists
                            });
                }
            }
        }

        return new ArrayList<>(resultMap.values());
    }
}