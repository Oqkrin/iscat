package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.database.sqlite.EnemyDAO;
import uni.gaben.iscat.universe.rendering.RenderRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds a complete (Model + Brain + View) triple for any enemy defined
 * in the Entita DB table, identified by its EntityKey string.
 * This is the only class that needs to change when you add a new DB-driven
 * enemy — you just insert a row in the DB, nothing in code.
 * ── How it plugs into UniverseSpawner ────────────────────────────────────
 * UniverseSpawner.spawnCustomRuntimeEntity() already exists and is called
 * whenever spawn(id, x, y) receives a string that doesn't match
 * UniverseSpawnable enum. Replace its body with:
 *   private Object spawnCustomRuntimeEntity(String id, double x, double y) {
 *       return GenericEntityFactory.spawn(id, x, y,
 *                   model, controller);
 *   }
 * That's the only change needed in existing code.
 * ── Settings cache ────────────────────────────────────────────────────────
 * DB reads are cached on first use so repeated spawns of the same enemy
 * (e.g. waves of "iscat_mob") never hit the DB again.
 */
public class GenericEntityFactory {

    private GenericEntityFactory() {}

    /** Settings cache: entityKey → GenericEntitySettings */
    private static final Map<String, GenericEntitySettings> cache = new ConcurrentHashMap<>();

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Spawns a DB-driven enemy into the universe.
     *
     * @param entityKey  matches the EntityKey column in Entita (e.g. "iscat_mob")
     * @param x          spawn position in pixels
     * @param y          spawn position in pixels
     * @param universe   the UniverseModel to add the entity body to
     * @param controller the UniverseController to register the brain with
     * @return the spawned GenericEntityModel, or null if the key is unknown
     */
    public static GenericEntityModel spawn(
            String entityKey,
            double x, double y,
            uni.gaben.iscat.universe.UniverseModel universe,
            uni.gaben.iscat.universe.UniverseController controller) {

        GenericEntitySettings settings = loadSettings(entityKey);
        if (settings == null) {
            System.err.println("[GenericEntityFactory] Unknown entityKey: '" + entityKey + "'");
            return null;
        }

        GenericEntityModel model = new GenericEntityModel(x, y, settings);
        GenericEntityBrain brain = new GenericEntityBrain(model);
        GenericEntityView view = new GenericEntityView(settings);
        universe.addEntity(model);
        controller.addEntityController(brain);
        RenderRegistry.getInstance().register(model, view);

        return model;
    }

    // ── Cache helpers ─────────────────────────────────────────────────────────

    /**
     * Pre-loads all enemy definitions from the DB into the cache.
     * Call once at game start (e.g. from your loading screen) to avoid
     * any first-spawn DB latency.
     */
    public static void preloadAll() {
        EnemyDAO.findAll().forEach(s -> cache.put(s.entityKey, s));
        System.out.println("[GenericEntityFactory] Preloaded " + cache.size() + " enemy definitions.");
    }

    /** Returns a cached settings object, loading from DB on first access. */
    private static GenericEntitySettings loadSettings(String entityKey) {
        return cache.computeIfAbsent(entityKey, key -> {
            Optional<GenericEntitySettings> found = EnemyDAO.findByKey(key);
            found.ifPresent(s -> System.out.println(
                    "[GenericEntityFactory] Loaded '" + key + "' from DB."));
            return found.orElse(null);
        });
    }
}