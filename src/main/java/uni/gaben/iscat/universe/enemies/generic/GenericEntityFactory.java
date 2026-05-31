package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.database.sqlite.EnemyDAO;
import uni.gaben.iscat.universe.rendering.RenderRegistry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fabbrica polimorfica per la generazione dinamica di entità guidate da database.
 * Implementa il pattern Factory accoppiato a una cache Flyweight ({@link ConcurrentHashMap})
 * per minimizzare la latenza di I/O del database SQLite durante il gameplay.
 * Si occupa di assemblare la triade architetturale di un nemico generico:
 * - Modello Fisico ({@link GenericEntityModel})
 * - Intelligenza Artificiale/Cervello ({@link GenericEntityBrain})
 * - Rendering Grafico/Vista ({@link GenericEntityView})
 */
public class GenericEntityFactory {

    /** Costruttore privato per impedire l'istanziazione di questa classe utility. */
    private GenericEntityFactory() {}

    /** Cache di memorizzazione locale: associa l'EntityKey normalizzata alle sue impostazioni strutturali. */
    private static final Map<String, GenericEntitySettings> cache = new ConcurrentHashMap<>();

    /**
     * Instanzia, assembla e registra nel ciclo di gioco un'entità nemica basandosi sui dati del DB.
     * Crea il corpo rigido, alloca il controller logico della CPU e mappa il renderer grafico dedicatogli.
     *
     * @param entityKey  Chiave identificativa univoca del nemico (es. "iscat_mob"). Matches col DB.
     * @param x          Posizione di spawn iniziale sull'asse X (espressa in pixel).
     * @param y          Posizione di spawn iniziale sull'asse Y (espressa in pixel).
     * @param universe   Il modello del mondo fisico a cui aggiungere il corpo del nemico.
     * @param controller Il controller dell'universo su cui registrare il ciclo di update del cervello dell'IA.
     * @return L'istanza del modello generato (GenericEntityModel), oppure null se la chiave non è presente nel database.
     */
    public static GenericEntityModel spawn(
            String entityKey,
            double x, double y,
            uni.gaben.iscat.universe.UniverseModel universe,
            uni.gaben.iscat.universe.UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        // Recupero delle impostazioni (da cache o da DB in caso di cache-miss)
        GenericEntitySettings settings = loadSettings(normalizedKey);
        if (settings == null) {
            System.err.println("[GenericEntityFactory] Impossibile spawnare: EntityKey sconosciuta '" + normalizedKey + "'");
            return null;
        }

        // Assemblaggio dei tre componenti core dell'entità
        GenericEntityModel model = new GenericEntityModel(x, y, settings);
        GenericEntityBrain brain = new GenericEntityBrain(model);
        GenericEntityView view = new GenericEntityView(settings);

        // Registrazione dei componenti nei rispettivi sottosistemi del motore di gioco
        universe.addEntity(model);
        controller.addEntityController(brain);
        RenderRegistry.getInstance().register(model, view);

        return model;
    }

    /**
     * Esegue il caricamento preventivo (Pre-load) di tutte le definizioni dei nemici dal DB alla cache.
     * Si consiglia di invocare questo metodo una sola volta all'avvio dell'applicazione (es. schermata di caricamento)
     * per azzerare completamente qualsiasi microscatto o latenza di lettura del disco durante le ondate di gioco.
     */
    public static void preloadAll() {
        EnemyDAO.findAll().forEach(s -> {
            if (s != null && s.entityKey != null) {
                cache.put(s.entityKey.toLowerCase().trim(), s);
            }
        });
        System.out.println("[GenericEntityFactory] Pre-caricate con successo " + cache.size() + " definizioni dal database.");
    }

    /**
     * Gestisce l'estrazione e la risoluzione delle impostazioni di un'entità.
     * Ispeziona prima la mappa cache thread-safe; in caso di assenza, interroga in modo sicuro
     * il Data Access Object (DAO) e aggiorna la cache per gli accessi futuri.
     *
     * @param entityKey Chiave normalizzata dell'entità da cercare.
     * @return L'oggetto GenericEntitySettings corrispondente, oppure null se non esiste traccia nel DB.
     */
    private static GenericEntitySettings loadSettings(String entityKey) {
        GenericEntitySettings cached = cache.get(entityKey);
        if (cached != null) {
            return cached;
        }

        Optional<GenericEntitySettings> found = EnemyDAO.findByKey(entityKey);
        if (found.isPresent()) {
            GenericEntitySettings settings = found.get();
            System.out.println("[GenericEntityFactory] Caricamento dinamico di '" + entityKey + "' eseguito dal database.");
            cache.put(entityKey, settings);
            return settings;
        }

        return null;
    }
}