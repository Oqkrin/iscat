package uni.gaben.iscat.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * Risolutore globale di risorse esterne e interne del motore grafico.
 * Fornisce un sistema di fallback e override automatico (Modding-friendly):
 * cerca prima i file nella cartella esterna di root (se configurata) e, in caso di assenza,
 * ripiega sulle risorse predefinite integrate nel Classpath (JAR).
 */
public final class ExternalResourceResolver {

    private static Path entitiesRoot = null;

    private ExternalResourceResolver() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Inizializza il percorso della directory delle entità esterne.
     * Da invocare una sola volta all'avvio del gioco (es. {@code ExternalResourceResolver.init(Path.of("./entities"));}).
     *
     * @param externalEntitiesDir Il {@link Path} della cartella esterna contenente gli asset custom.
     */
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
     * Restituisce un {@link InputStream} operativo per un determinato percorso relativo,
     * applicando la precedenza al file esterno se presente sul disco.
     *
     * @param relativePath Il percorso relativo dell'asset (es. "json/enemies/slime.json").
     * @return L'{@link InputStream} della risorsa, oppure {@code null} se non viene trovata in nessun percorso.
     */
    public static InputStream resolve(String relativePath) {
        if (entitiesRoot != null) {
            String extPathStr = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            if (extPathStr.startsWith("uni/gaben/iscat/")) {
                extPathStr = extPathStr.substring("uni/gaben/iscat/".length());
            }
            Path externalFile = entitiesRoot.resolve(extPathStr);
            if (Files.isRegularFile(externalFile)) {
                try {
                    return Files.newInputStream(externalFile);
                } catch (IOException e) {
                    System.err.println("[Resolver] Impossibile leggere il file esterno: " + externalFile);
                }
            }
        }

        // Fallback sul Classpath interno
        String cpPath = relativePath.startsWith("/") ? relativePath : "/" + relativePath;
        InputStream is = ExternalResourceResolver.class.getResourceAsStream(cpPath);
        if (is == null) {
            System.err.println("[Resolver] Risorsa non trovata nel Classpath: " + cpPath);
        }
        return is;
    }

    /**
     * Scansiona una directory relativa (profondità 1) combinando i file interni con quelli esterni.
     * Se un file esterno possiede lo stesso identico nome di un file interno, quest'ultimo viene
     * sovrascitto (override) nella lista finale restituita, permettendo il modding parziale dei dati.
     *
     * @param relativeDir La cartella relativa da scansionare (es. "json/enemies").
     * @param extension   L'estensione dei file da filtrare (es. ".json").
     * @return Una {@link List} di oggetti {@link Path} assoluti (esterni o mappati dal file system interno).
     * @throws IOException In caso di errori gravi di accesso ai file system.
     */
    public static List<Path> listFiles(String relativeDir, String extension) throws IOException {
        // LinkedHashMap mantiene l'ordine di inserimento (NomeFile -> Path Assoluto)
        Map<String, Path> resultMap = new LinkedHashMap<>();

        // 1. Scansione dei file interni di default (vengono inseriti per primi)
        try {
            URI uri = Objects.requireNonNull(
                    ExternalResourceResolver.class.getResource("/" + relativeDir),
                    "Directory interna non trovata nel Classpath: /" + relativeDir
            ).toURI();

            if ("jar".equals(uri.getScheme())) {
                // Gestione robusta del FileSystem JAR tramite try-with-resources per evitare resource leak
                try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                    Path internalDir = fs.getPath("/" + relativeDir);
                    scanDirectory(internalDir, extension, resultMap, false);
                }
            } else {
                Path internalDir = Paths.get(uri);
                scanDirectory(internalDir, extension, resultMap, false);
            }
        } catch (NullPointerException npe) {
            // Expected: internal classpath directory doesn't exist (e.g. json/custom/).
            // Will be populated from external folder only.
        } catch (Exception e) {
            System.err.println("[Resolver] Errore durante la lettura della directory interna '" + relativeDir + "': " + e.getMessage());
        }

        // 2. Scansione dei file esterni (sovrascrivono le chiavi omonime inserite dallo step 1)
        if (entitiesRoot != null) {
            String extPathStr = relativeDir.startsWith("/") ? relativeDir.substring(1) : relativeDir;
            if (extPathStr.startsWith("uni/gaben/iscat/")) {
                extPathStr = extPathStr.substring("uni/gaben/iscat/".length());
            }
            Path extDir = entitiesRoot.resolve(extPathStr);
            if (Files.isDirectory(extDir)) {
                scanDirectory(extDir, extension, resultMap, true);
            }
        }

        return new ArrayList<>(resultMap.values());
    }

    /**
     * Esegue la scansione di supporto estraendo i file regolari filtrati per estensione.
     */
    private static void scanDirectory(Path dir, String extension, Map<String, Path> registry, boolean overwrite) throws IOException {
        try (Stream<Path> walk = Files.walk(dir, 1)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(extension.toLowerCase()))
                    .forEach(p -> {
                        String name = p.getFileName().toString();
                        if (overwrite) {
                            registry.put(name, p); // Sovrascrive l'eventuale risorsa di default
                        } else {
                            registry.putIfAbsent(name, p); // Inserisce solo se non presente
                        }
                    });
        }
    }
}