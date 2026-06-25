package uni.gaben.iscat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Risolutore globale di risorse esterne e interne del motore grafico.
 */
public final class ExternalResourceResolver {

    private static Path entitiesRoot = null;

    // Il prefisso obbligatorio per tutte le risorse interne nel JAR
    private static final String INTERNAL_PREFIX = "/uni/gaben/iscat/";

    private ExternalResourceResolver() {}

    public static void init(Path externalEntitiesDir) {
        if (Files.isDirectory(externalEntitiesDir)) {
            entitiesRoot = externalEntitiesDir;
            System.out.println("[Resolver] Cartella entità esterna rilevata: " + entitiesRoot);
        } else {
            System.out.println("[Resolver] Cartella entità esterna non trovata. Uso esclusivo delle risorse interne del Classpath.");
        }
    }

    public static Path getEntitiesRoot() {
        return entitiesRoot;
    }

    /**
     * @param relativePath Il percorso relativo breve (es. "json/enemies/slime.json").
     */
    public static InputStream resolve(String relativePath) {
        // Pulisce eventuali slash iniziali o il prefisso interno se passato per sbaglio
        String cleanPath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        if (cleanPath.startsWith("uni/gaben/iscat/")) {
            cleanPath = cleanPath.substring("uni/gaben/iscat/".length());
        }

        // 1. Cerca nel disco esterno
        if (entitiesRoot != null) {
            Path externalFile = entitiesRoot.resolve(cleanPath);
            if (Files.isRegularFile(externalFile)) {
                try {
                    return Files.newInputStream(externalFile);
                } catch (IOException e) {
                    System.err.println("[Resolver] Impossibile leggere il file esterno: " + externalFile);
                }
            }
        }

        // 2. Fallback nel Classpath interno (aggiungendo il prefisso obbligatorio)
        String cpPath = INTERNAL_PREFIX + cleanPath;
        InputStream is = ExternalResourceResolver.class.getResourceAsStream(cpPath);
        if (is == null) {
            System.err.println("[Resolver] Risorsa non trovata nel Classpath: " + cpPath);
        }
        return is;
    }

    /**
     * @param relativeDir La cartella relativa breve da scansionare (es. "json/enemies").
     */
    public static List<Path> listFiles(String relativeDir, String extension) throws IOException {
        Map<String, Path> resultMap = new LinkedHashMap<>();

        // Pulisce il percorso
        String cleanDir = relativeDir.startsWith("/") ? relativeDir.substring(1) : relativeDir;
        if (cleanDir.startsWith("uni/gaben/iscat/")) {
            cleanDir = cleanDir.substring("uni/gaben/iscat/".length());
        }

        // 1. Scansione dei file interni di default
        String internalDirToScan = INTERNAL_PREFIX + cleanDir;

        try {
            URL resourceUrl = ExternalResourceResolver.class.getResource(internalDirToScan);
            if (resourceUrl != null) {
                URI uri = resourceUrl.toURI();

                if ("jar".equals(uri.getScheme())) {
                    FileSystem fs;
                    try {
                        fs = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    } catch (FileSystemAlreadyExistsException e) {
                        fs = FileSystems.getFileSystem(uri);
                    }

                    Path internalPath = fs.getPath(internalDirToScan);
                    scanDirectory(internalPath, extension, resultMap, false);
                } else {
                    Path internalPath = Paths.get(uri);
                    scanDirectory(internalPath, extension, resultMap, false);
                }
            } else {
                System.err.println("[Resolver] Attenzione: Directory interna non trovata: " + internalDirToScan);
            }
        } catch (Exception e) {
            System.err.println("[Resolver] Errore durante la scansione della directory interna '" + internalDirToScan + "': " + e.getMessage());
        }

        // 2. Scansione dei file esterni (sovrascrivono i file interni)
        if (entitiesRoot != null) {
            Path extDir = entitiesRoot.resolve(cleanDir);
            if (Files.isDirectory(extDir)) {
                scanDirectory(extDir, extension, resultMap, true);
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    private static void scanDirectory(Path dir, String extension, Map<String, Path> registry, boolean overwrite) throws IOException {
        try (Stream<Path> walk = Files.walk(dir, 1)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(extension.toLowerCase()))
                    .forEach(p -> {
                        String name = p.getFileName().toString();
                        if (overwrite) {
                            registry.put(name, p);
                        } else {
                            registry.putIfAbsent(name, p);
                        }
                    });
        }
    }
}