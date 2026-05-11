package uni.gaben.iscat.gamenex.universe;

import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobController;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobModel;
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
        register("ASTEROID", (x, y) -> spawnAsteroid(x, y, 30.0));
        register("ISCAT_MOB", this::spawnIscatMob);
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
    public AsteroidModel spawnAsteroid(double x, double y, double radius) {
        AsteroidModel asteroid = new AsteroidModel(x, y, radius);
        model.addEntity(asteroid);
        return asteroid;
    }
}
