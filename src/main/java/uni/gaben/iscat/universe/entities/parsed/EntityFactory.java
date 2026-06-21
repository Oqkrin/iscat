package uni.gaben.iscat.universe.entities.parsed;

import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Fabbrica globale per la gestione del ciclo di vita delle entità di gioco.
 * Carica, memorizza in cache e istanzia i modelli e i cervelli (IA) partendo dai file JSON.
 */
public class EntityFactory {
    private EntityFactory() {}

    static final String ENEMIES_DIR = "/uni/gaben/iscat/json/enemies/";
    static final String PLAYERS_DIR = "/uni/gaben/iscat/json/players/";
    static final String CUSTOM_DIR  = "/uni/gaben/iscat/json/custom/";

    private static final Map<String, EntityRecord> cache = new ConcurrentHashMap<>();
    private static final Map<String, JSONObject> rawJsonCache = new ConcurrentHashMap<>();
    private static final Map<String, String> originPathCache = new ConcurrentHashMap<>();
    private static volatile CompletableFuture<Map<String, EntityRecord>> cacheReadyFuture;
    private static final Object futureLock = new Object();

    /**
     * Crea e posiziona nel mondo di gioco una nuova entità e il rispettivo controller IA.
     * Attende in modo sincrono se la cache non è ancora stata inizializzata.
     */
    public static EntityModel spawn(
            String entityKey,
            double x, double y,
            UniverseModel universe,
            UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        Map<String, EntityRecord> currentCache = ensureCacheLoaded().join();
        EntityRecord entity = currentCache.get(normalizedKey);

        if (entity == null) {
            System.err.println("[EntityFactory] EntityKey sconosciuta: '" + normalizedKey + "'");
            return null;
        }

        EntityModel model = new EntityModel(x, y, entity);
        EntityBrain brain = EntityBrain.fromRecord(model);

        universe.addEntity(model);
        controller.addEntityController(brain);

        return model;
    }

    /**
     * Garantisce che il caricamento asincrono iniziale dei file JSON sia avviato.
     */
    public static CompletableFuture<Map<String, EntityRecord>> ensureCacheLoaded() {
        if (cacheReadyFuture == null) {
            synchronized (futureLock) {
                if (cacheReadyFuture == null) {
                    cacheReadyFuture = preloadAllAsyncInternal();
                }
            }
        }
        return cacheReadyFuture;
    }

    /**
     * Esegue internamente il caricamento asincrono parallelo di tutte le directory di configurazione.
     */
    private static CompletableFuture<Map<String, EntityRecord>> preloadAllAsyncInternal() {
        CompletableFuture<List<EntityJsonLoader.LoadedJson>> enemiesFuture = EntityJsonLoader.loadAllFromDirectory(ENEMIES_DIR);
        CompletableFuture<List<EntityJsonLoader.LoadedJson>> playersFuture = EntityJsonLoader.loadAllFromDirectory(PLAYERS_DIR);
        CompletableFuture<List<EntityJsonLoader.LoadedJson>> customFuture  = EntityJsonLoader.loadAllFromDirectory(CUSTOM_DIR);

        return CompletableFuture.allOf(enemiesFuture, playersFuture, customFuture).thenApply(v -> {
            Map<String, EntityRecord> fresh = new ConcurrentHashMap<>();
            Map<String, JSONObject> freshRaw = new ConcurrentHashMap<>();
            Map<String, String> freshPaths = new ConcurrentHashMap<>();

            Stream.of(enemiesFuture.join(), playersFuture.join(), customFuture.join())
                    .flatMap(List::stream)
                    .forEach(loaded -> {
                        EntityRecord r = EntityRecordParser.parse(loaded.json());
                        if (r.entityKey() != null && !r.entityKey().isEmpty()) {
                            String key = r.entityKey().toLowerCase().trim();
                            fresh.put(key, r);
                            freshRaw.put(key, loaded.json());
                            freshPaths.put(key, loaded.originPath());
                        }
                    });

            cache.clear();
            cache.putAll(fresh);
            rawJsonCache.clear();
            rawJsonCache.putAll(freshRaw);
            originPathCache.clear();
            originPathCache.putAll(freshPaths);
            System.out.println("[EntityFactory] Cache updated. Total: " + cache.size());
            return cache;
        });
    }

    /**
     * Aggiunge o aggiorna manualmente un record all'interno della cache a runtime (es. per l'editor interno).
     */
    public static void addOrUpdateEntity(EntityRecord record) {
        if (record == null || record.entityKey() == null || record.entityKey().isEmpty()) return;
        String key = record.entityKey().toLowerCase().trim();
        cache.put(key, record);
    }

    /**
     * Registra manualmente un JSON grezzo e il suo percorso nella cache in-memory.
     * Usato dall'editor per rendere disponibili le entità custom subito dopo il salvataggio.
     */
    public static void registerRawJson(String entityKey, JSONObject json, String originPath) {
        if (entityKey == null || entityKey.isEmpty()) return;
        String key = entityKey.toLowerCase().trim();
        rawJsonCache.put(key, json);
        originPathCache.put(key, originPath);
    }

    public static Map<String, EntityRecord> getCache() {
        return cache;
    }

    /**
     * Recupera l'oggetto JSON grezzo associato a una chiave entità.
     */
    public static JSONObject getRawJson(String entityKey) {
        if (entityKey == null) return null;
        return rawJsonCache.get(entityKey.toLowerCase().trim());
    }

    /**
     * Recupera il percorso file assoluto di origine da cui è stata caricata l'entità.
     */
    public static String getOriginPath(String entityKey) {
        if (entityKey == null) return null;
        return originPathCache.get(entityKey.toLowerCase().trim());
    }

}