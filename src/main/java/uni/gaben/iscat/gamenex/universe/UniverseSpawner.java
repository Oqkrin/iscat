package uni.gaben.iscat.gamenex.universe;

import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.eater.EaterController;
import uni.gaben.iscat.gamenex.universe.eater.EaterModel;
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
import java.util.function.BiFunction;

/**
 * Utility per la generazione (spawning) di entità nell'universo.
 * Implementa il pattern Singleton per garantire un punto di creazione centralizzato.
 * Gestisce la creazione dei modelli fisici, la loro registrazione nel mondo 
 * e l'aggancio dei controller AI necessari.
 * 
 * MIGLIORAMENTO: Utilizza ora un registro dinamico di factory per permettere lo spawning
 * di nuove entità tramite ID stringa, facilitando l'estensione del gioco senza modificare il codice core.
 */
public class UniverseSpawner {
    private static UniverseSpawner instance;

    private UniverseModel model;
    private UniverseController controller;
    
    /** Registro delle factory: associa un ID entità a una funzione che crea il modello (x, y). */
    private final Map<String, BiFunction<Double, Double, Object>> spawnRegistry = new HashMap<>();

    private UniverseSpawner() {
        // Registrazione iniziale delle entità core per permettere lo spawning dinamico
        register("ASTEROID", this::spawnAsteroid);
        register("ISCAT_MOB", this::spawnIscatMob);
        register("HEARTH", this::spawnHearth);
        register("EATER", this::spawnEater);

        register("WORM", this::spawnWorm);
    }

    public HearthModel spawnHearth(double x, double y) {
        HearthModel hearth = new HearthModel(x, y);
        model.addEntity(hearth);
        HearthController hearthController = new HearthController(hearth);
        controller.addAiController(hearthController);
        return hearth;
    }

    public EaterModel spawnEater(double x, double y) {
        EaterModel eater = new EaterModel(x, y);
        model.addEntity(eater);
        EaterController eaterController = new EaterController(eater);
        controller.addAiController(eaterController);
        return eater;
    }

    public IscatWormHeadModel spawnWorm(double x, double y) {
        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        double spacing = 28.0 / UniverseSettings.SCALE; // distanza tra i segmenti in metri

        // 1. CREA LA TESTA
        IscatWormHeadModel head = new IscatWormHeadModel(x, y);
        model.addEntity(head);
        IscatWormHeadController headController = new IscatWormHeadController(head);
        controller.addAiController(headController);

        IscatWormBodyPartModel previous = null;
        IscatWormBodyPartModel firstBody = null;

        // 2. CREA 10 BODY PARTS
        for (int i = 0; i < 10; i++) {
            double bodyX = x - (i + 1) * spacing * 1.2;
            double bodyY = y;

            IscatWormBodyPartModel body = new IscatWormBodyPartModel(bodyX, bodyY);
            model.addEntity(body);

            IscatWormBodyPartController bodyController = new IscatWormBodyPartController(body);

            // Collega al segmento precedente
            if (previous == null) {
                bodyController.setPreviousSegment(head);   // primo body segue la testa
                firstBody = body;
            } else {
                bodyController.setPreviousSegment(previous);
            }

            controller.addAiController(bodyController);
            previous = body;
        }

        // 3. CREA LA TAIL
        double tailX = x - 11 * spacing * 1.2;
        double tailY = y;

        IscatWormTailModel tail = new IscatWormTailModel(tailX, tailY);
        model.addEntity(tail);

        IscatWormTailController tailController = new IscatWormTailController(tail);
        tailController.setPreviousSegment(previous);   // segue l'ultimo body part
        controller.addAiController(tailController);

        System.out.println("IscatWorm spawnato con 1 Head + 10 Body + 1 Tail @ (" + x + ", " + y + ")");

        return head;   // restituiamo la testa come "entità principale" del verme
    }

    /**
     * Restituisce l'istanza singleton dello spawner.
     * Utilizza il modificatore synchronized per garantire la thread-safety.
     */
    public static synchronized UniverseSpawner getInstance() {
        if (instance == null) {
            instance = new UniverseSpawner();
        }
        return instance;
    }

    /**
     * Inizializza lo spawner con i riferimenti necessari al mondo di gioco.
     * Deve essere chiamato una sola volta all'avvio della scena Gamenex.
     * @param model Il modello dell'universo (gestione corpi fisici Dyn4j).
     * @param controller Il controller dell'universo (gestione logica e AI).
     */
    public void init(UniverseModel model, UniverseController controller) {
        this.model = model;
        this.controller = controller;
    }

    /**
     * Registra un nuovo tipo di entità nel sistema globale.
     * @param id L'identificativo unico dell'entità (es. "ENEMY_BOSS").
     * @param factory Funzione che riceve le coordinate pixel (x, y) e restituisce l'oggetto creato.
     */
    public void register(String id, BiFunction<Double, Double, Object> factory) {
        spawnRegistry.put(id, factory);
    }

    /**
     * Genera un'entità in base al suo ID registrato.
     * Questo metodo è il punto di ingresso per lo spawning dinamico (es. da file di configurazione).
     * @param id L'ID dell'entità da generare.
     * @param x Coordinata X in pixel.
     * @param y Coordinata Y in pixel.
     * @return L'oggetto creato, o null se l'ID non risulta registrato.
     */
    public Object spawn(String id, double x, double y) {
        BiFunction<Double, Double, Object> factory = spawnRegistry.get(id);
        if (factory != null) {
            return factory.apply(x, y);
        }
        return null;
    }

    /**
     * Genera il giocatore nel mondo e lo imposta come entità focale.
     * @param x Coordinata X iniziale (pixel).
     * @param y Coordinata Y iniziale (pixel).
     * @return Il modello del giocatore creato.
     */
    public PlayerModel spawnPlayer(double x, double y) {
        PlayerModel player = new PlayerModel(x, y);
        model.setPlayer(player);
        return player;
    }

    /**
     * Genera un nemico IscatMob, lo registra nella fisica e gli assegna un controller AI.
     * Il controller viene automaticamente aggiunto alla lista di aggiornamento dell'universo.
     * @param x Coordinata X (pixel).
     * @param y Coordinata Y (pixel).
     * @return Il modello del mob creato.
     */
    public IscatMobModel spawnIscatMob(double x, double y) {
        IscatMobModel mob = new IscatMobModel(x, y);
        model.addEntity(mob);

        IscatMobController mobController = new IscatMobController(mob);
        controller.addAiController(mobController);

        return mob;
    }

    /**
     * Genera un asteroide fisico nel mondo.
     * Il raggio viene convertito internamente da pixel a metri per la simulazione Dyn4j.
     * @param x Coordinata X (pixel).
     * @param y Coordinata Y (pixel).
     * @param radius Raggio dell'asteroide (pixel).
     * @return Il modello dell'asteroide creato.
     */
    public AsteroidModel spawnAsteroid(double x, double y, int radius) {
        AsteroidModel asteroid = new AsteroidModel(x, y, radius);
        model.addEntity(asteroid);
        return asteroid;
    }

    public AsteroidModel spawnAsteroid(double x, double y) {
        AsteroidModel asteroid = new AsteroidModel(x, y, 0);
        model.addEntity(asteroid);
        return asteroid;
    }
}
