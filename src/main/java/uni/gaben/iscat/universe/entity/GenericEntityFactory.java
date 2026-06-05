package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.database.IscatDB;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabbrica polimorfica per la generazione dinamica di entità guidate da database.
 * Implementa il pattern Factory accoppiato a una cache Flyweight ({@link ConcurrentHashMap})
 * per minimizzare la latenza di I/O del database SQLite durante il gameplay.
 */
public class GenericEntityFactory {

    private GenericEntityFactory() {}

    private static final Map<String, GenericEntitySettings> cache = new ConcurrentHashMap<>();

    /**
     * Instanzia, assembla e registra nel ciclo di gioco un'entità nemica basandosi sui dati del DB.
     */
    public static GenericEntityModel spawn(
            String entityKey,
            double x, double y,
            uni.gaben.iscat.universe.UniverseModel universe,
            uni.gaben.iscat.universe.UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        GenericEntitySettings settings = loadSettings(normalizedKey);
        if (settings == null) {
            System.err.println("[GenericEntityFactory] Impossibile spawnare: EntityKey sconosciuta '" + normalizedKey + "'");
            return null;
        }

        GenericEntityModel model = new GenericEntityModel(x, y, settings);
        GenericEntityBrain brain = new GenericEntityBrain(model);

        universe.addEntity(model);
        controller.addEntityController(brain);

        return model;
    }

    /**
     * Esegue il caricamento preventivo (Pre-load) in BACKGROUND di tutte le definizioni dal DB alla cache.
     * Restituisce un CompletableFuture per permettere alla schermata di caricamento di sapere quando ha finito.
     */
    public static CompletableFuture<Void> preloadAllAsync() {
        // Scarichiamo l'intera operazione pesante sul thread dedicato al database
        return IscatDB.getInstance().queryAsync(() -> IscatDB.getInstance().getEnemyDAO().findAll())
                .thenAccept(enemies -> {
                    for (GenericEntitySettings s : enemies) {
                        if (s != null && s.entityKey != null) {
                            cache.put(s.entityKey.toLowerCase().trim(), s);
                        }
                    }
                    System.out.println("[GenericEntityFactory] Cache pronta. Pre-caricate " + cache.size() + " definizioni.");
                }).exceptionally(ex -> {
                    System.err.println("[GenericEntityFactory] Errore durante il pre-caricamento: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Ispeziona la cache. In caso di assenza (Cache-Miss), effettua un caricamento di ripiego (fallback)
     * sincrono dal DB, segnalando l'anomalia prestazionale.
     */
    private static GenericEntitySettings loadSettings(String entityKey) {
        GenericEntitySettings cached = cache.get(entityKey);
        if (cached != null) {
            return cached;
        }

        // Se arriviamo qui, significa che preloadAllAsync() non è stato invocato all'inizio del livello,
        // oppure la chiave richiesta non esiste nel database.
        System.err.println("[PERFORMANCE WARNING] Cache-miss per '" + entityKey + "' durante il gameplay! " +
                "La query sincrona su SQLite potrebbe causare microscatti.");

        // Eseguiamo la query sincrona diretta: inutile usare queryAsync().join() che aggiunge solo overhead
        Optional<GenericEntitySettings> dbResult = IscatDB.getInstance().getEnemyDAO().findByKey(entityKey);

        if (dbResult.isPresent()) {
            GenericEntitySettings settings = dbResult.get();
            cache.put(entityKey, settings);
            return settings;
        }

        return null;
    }
}