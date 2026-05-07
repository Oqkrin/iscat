package uni.gaben.iscat;

/**
 * Singleton facade per la navigazione.
 * Altri controller chiamano questo per cambiare scena senza conoscere Stage/Scene.
 */
public class IscatNavigator {
    private static IscatNavigator instance;
    private IscatModel model;

    private IscatNavigator() {}

    public static IscatNavigator getInstance() {
        if (instance == null) {
            instance = new IscatNavigator();
        }
        return instance;
    }

    /**
     * Inizializza il navigator con il model dell'applicazione.
     * Chiamato da IscatApplication durante il bootstrap.
     */
    public void initialize(IscatModel model) {
        this.model = model;
    }

    /**
     * Naviga alla scena specificata.
     */
    public void navigateTo(IscatScenes scene) {
        if (model == null) {
            throw new IllegalStateException("IscatNavigator non inizializzato. Chiamare initialize() prima.");
        }
        model.setCurrentScene(scene);
    }
}
