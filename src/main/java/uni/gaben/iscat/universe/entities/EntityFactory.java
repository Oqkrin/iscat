package uni.gaben.iscat.universe.entities;

import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.json.EntityJsonLoader;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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

    // ------------------------------------------------------------
    //  Spawn (blocks until cache is ready – lazy initialisation)
    // ------------------------------------------------------------
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

    // ------------------------------------------------------------
    //  Cache future management
    // ------------------------------------------------------------
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
     * Internal load: combines enemy + player + custom JSON, parses them, and
     * atomically replaces the cache content.
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
                        EntityRecord r = EntityRecordParser.parse(loaded.json);
                        if (r.entityKey() != null && !r.entityKey().isEmpty()) {
                            String key = r.entityKey().toLowerCase().trim();
                            fresh.put(key, r);
                            freshRaw.put(key, loaded.json);
                            freshPaths.put(key, loaded.originPath);
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

    // ------------------------------------------------------------
    //  Runtime editing (file changes, unit creator)
    // ------------------------------------------------------------

    /**
     * Trigger a full reload from disk. The returned future completes when the
     * new data is ready. Subsequent spawn() calls will wait for this refresh.
     */
    public static CompletableFuture<Void> refreshFromDiskAsync() {
        CompletableFuture<Map<String, EntityRecord>> newLoad = preloadAllAsyncInternal();
        cacheReadyFuture = newLoad;
        return newLoad.thenApply(m -> null);
    }

    /**
     * Add or update an entity record directly – useful for a built‑in unit
     * creator. The record is placed in the live cache immediately.
     * (To persist it, also write the JSON file and call refreshFromDiskAsync).
     */
    public static void addOrUpdateEntity(EntityRecord record) {
        if (record == null || record.entityKey() == null || record.entityKey().isEmpty()) return;
        String key = record.entityKey().toLowerCase().trim();
        cache.put(key, record);
    }

    /**
     * Remove an entity from the live cache.
     */
    public static void removeEntity(String entityKey) {
        if (entityKey != null) {
            cache.remove(entityKey.toLowerCase().trim());
        }
    }

    // ------------------------------------------------------------
    //  Cache access (read-only for UI, etc.)
    // ------------------------------------------------------------
    public static Map<String, EntityRecord> getCache() {
        return cache;
    }

    public static JSONObject getRawJson(String entityKey) {
        if (entityKey == null) return null;
        return rawJsonCache.get(entityKey.toLowerCase().trim());
    }

    public static String getOriginPath(String entityKey) {
        if (entityKey == null) return null;
        return originPathCache.get(entityKey.toLowerCase().trim());
    }

    // Package-private for testing / advanced use
    static void setCacheReadyFuture(CompletableFuture<Map<String, EntityRecord>> future) {
        cacheReadyFuture = future;
    }
}