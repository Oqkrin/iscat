package uni.gaben.iscat.gamenex.universe;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiController;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.iscat_eater.IscatEaterController;
import uni.gaben.iscat.gamenex.universe.iscat_eater.IscatEaterModel;
import uni.gaben.iscat.gamenex.universe.hearth.HearthController;
import uni.gaben.iscat.gamenex.universe.hearth.HearthModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobController;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartController;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadController;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailController;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static uni.gaben.iscat.gamenex.universe.UniverseSpawnable.*;

/**
 * Utility centralizzato per lo spawning di entità nell'universo.
 */
public class UniverseSpawner {
    private static UniverseSpawner instance;

    private UniverseModel model;
    private UniverseController controller;

    private final Map<String, BiFunction<Double, Double, Object>> spawnRegistry = new HashMap<>();

    private UniverseSpawner() {
        // --- REGISTRAZIONE DINAMICA COMPATTA ---
        // Gli elementi semplici vengono registrati passando i costruttori di Modello e Controller
        register(ASTEROID.name(),  (x, y) -> spawnStandard(AsteroidModel::new, null, x, y));
        register(ISCAT_MOB.name(), (x, y) -> spawnStandard(IscatMobModel::new, IscatMobController::new, x, y));
        register(HEARTH.name(),    (x, y) -> spawnStandard(HearthModel::new, HearthController::new, x, y));
        register(EATER.name(),     (x, y) -> spawnStandard(IscatEaterModel::new, IscatEaterController::new, x, y));

        // Strutture complesse (come il Worm) mantengono il loro metodo di costruzione custom
        register(WORM.name(),      this::spawnWorm);
    }

    /**
     * Il Core della generalizzazione. Gestisce autonomamente il ciclo di instanziazione,
     * registrazione fisica e inserimento nel loop dell'AI per entità standard.
     */
    private <M extends AbstractEntityModel> M spawnStandard(
            BiFunction<Double, Double, M> modelFactory,
            Function<M, ?> controllerFactory,
            double x, double y) {

        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        // 1. Crea il modello fisico nelle coordinate desiderate
        M entityModel = modelFactory.apply(x, y);
        model.addEntity(entityModel);

        // 2. Se l'entità possiede un'intelligenza artificiale, istanzia e aggiungi il controller
        if (controllerFactory != null) {
            Object aiController = controllerFactory.apply(entityModel);
            controller.addAiController((AiController) aiController);
        }

        return entityModel;
    }

    // --- METODI SPECIFICI COMPRESSI (OPZIONALI) ---
    // Se chiami questi metodi direttamente da altre parti del codice, rimangono validi
    // ma ora delegano tutto al motore generalizzato in una sola riga di codice.

    public HearthModel spawnHearth(double x, double y) {
        return spawnStandard(HearthModel::new, HearthController::new, x, y);
    }

    public IscatEaterModel spawnEater(double x, double y) {
        return spawnStandard(IscatEaterModel::new, IscatEaterController::new, x, y);
    }

    public IscatMobModel spawnIscatMob(double x, double y) {
        return spawnStandard(IscatMobModel::new, IscatMobController::new, x, y);
    }

    public AsteroidModel spawnAsteroid(double x, double y) {
        return spawnStandard(AsteroidModel::new, null, x, y);
    }

    /**
     * Entità composta complessa. Questo metodo rimane custom poiché non segue
     * il pattern standard a causa della concatenazione di segmenti fisici diversi.
     */
    public IscatWormHeadModel spawnWorm(double x, double y) {
        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        double spacing = 28.0 / UniverseSettings.SCALE;

        IscatWormHeadModel head = new IscatWormHeadModel(x, y);
        model.addEntity(head);
        controller.addAiController(new IscatWormHeadController(head));

        IscatWormBodyPartModel previous = null;

        for (int i = 0; i < 10; i++) {
            double bodyX = x - (i + 1) * spacing * 1.2;
            IscatWormBodyPartModel body = new IscatWormBodyPartModel(bodyX, y);
            model.addEntity(body);

            IscatWormBodyPartController bodyController = new IscatWormBodyPartController(body);
            bodyController.setPreviousSegment(previous == null ? head : previous);

            controller.addAiController(bodyController);
            previous = body;
        }

        double tailX = x - 11 * spacing * 1.2;
        IscatWormTailModel tail = new IscatWormTailModel(tailX, y);
        model.addEntity(tail);

        IscatWormTailController tailController = new IscatWormTailController(tail);
        tailController.setPreviousSegment(previous);
        controller.addAiController(tailController);

        return head;
    }

    // --- METODI UTILITY RESTANTI ---
    public static synchronized UniverseSpawner getInstance() {
        if (instance == null) instance = new UniverseSpawner();
        return instance;
    }

    public void init(UniverseModel model, UniverseController controller) {
        this.model = model;
        this.controller = controller;
    }

    public void register(String id, BiFunction<Double, Double, Object> factory) {
        spawnRegistry.put(id, factory);
    }

    public Object spawn(String id, double x, double y) {
        BiFunction<Double, Double, Object> factory = spawnRegistry.get(id);
        return factory != null ? factory.apply(x, y) : null;
    }

    public PlayerModel spawnPlayer(double x, double y) {
        PlayerModel player = new PlayerModel(x, y);
        model.setPlayer(player);
        return player;
    }

    public AbstractProjectileModel spawnProjectile(AbstractProjectileModel p) {
        model.addEntity(p);
        return p;
    }

    public Set<String> getSpawnRegistryKeys() {
        return spawnRegistry.keySet();
    }
}