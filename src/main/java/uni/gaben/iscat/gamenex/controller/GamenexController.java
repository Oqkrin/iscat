package uni.gaben.iscat.gamenex.controller;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.universe.UniverseController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;

/**
 * Controller principale del motore Gamenex.
 * Coordina il ciclo di gioco, gestisce l'input dell'utente e supervisiona
 * i sotto-controller dell'universo e della telecamera.
 */
public class GamenexController {

    private UniverseController universeController;
    private GamenexModel gamenexModel;
    private CameraModel cameraModel = new CameraModel();
    private AnimationTimer gameLoop;
    private Runnable renderCallback;
    private InputManager inputManager = new InputManager();

    /**
     * Restituisce il gestore dell'input associato a questo controller.
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * Imposta la funzione di callback per il rendering.
     * Viene eseguita al termine di ogni frame del ciclo di gioco.
     */
    public void setRenderCallback(Runnable renderCallback) {
        this.renderCallback = renderCallback;
    }

    /**
     * Costruttore standard: inizializza il motore con i valori predefiniti.
     */
    public GamenexController(GamenexModel gamenexModel) {
        this(gamenexModel, new UniverseController());
    }

    /**
     * Costruttore avanzato: permette l'iniezione di un UniverseController personalizzato.
     * Inizializza il mondo fisico, genera le entità iniziali e prepara il timer di gioco.
     */
    public GamenexController(GamenexModel gamenexModel, UniverseController universeController) {
        this.gamenexModel = gamenexModel;
        this.universeController = universeController;

        // Inizializzazione dello Spawner e generazione del mondo iniziale
        UniverseSpawner spawner = UniverseSpawner.getInstance();
        spawner.init(universeController.getSpaceModel(), universeController);

        double w = universeController.getSpaceModel().getWidth();
        double h = universeController.getSpaceModel().getHeight();

        // Se le dimensioni non sono ancora vincolate alla finestra, usa i default
        if (w <= 0) w = UniverseSettings.DEFAULT_WIDTH;
        if (h <= 0) h = UniverseSettings.DEFAULT_HEIGHT;

        // Spawning iniziale: Giocatore, Asteroide di test e primo Mob
        spawner.spawnPlayer(w / 2.0, h / 2.0);
        spawner.spawnAsteroid(UniverseSettings.TEST_ASTEROID_X,
                UniverseSettings.TEST_ASTEROID_Y);
        spawner.spawnIscatMob(w / 2.0 + 200, h / 2.0 + 200);

        initTimer(gamenexModel);
    }

    /**
     * Inizializza l'AnimationTimer (il battito cardiaco del gioco).
     * Utilizza una logica a "Tempo Accumulato" per garantire che la fisica proceda
     * a passi costanti (TICKUNIT) indipendentemente dalla velocità di rendering.
     */
    private void initTimer(GamenexModel gamenexModel) {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gamenexModel.getLastUpdate() == 0) {
                    gamenexModel.setLastUpdate(now);
                }

                gamenexModel.setNow(now);
                double dt = gamenexModel.getDt();

                // Evita il "salto del tempo" (Spiral of Death) se il computer rallenta troppo
                if (dt > GamenexModel.ACCUMULATORUNIT) {
                    dt = GamenexModel.ACCUMULATORUNIT;
                }

                gamenexModel.setAccumulator(gamenexModel.getAccumulator() + dt);

                // Esegue tanti "tick" fisici quanti necessari per coprire il tempo trascorso
                while (gamenexModel.getAccumulator() >= GamenexModel.TICKUNIT) {
                    tick(GamenexModel.TICKUNIT);
                    gamenexModel.setAccumulator(gamenexModel.getAccumulator() - GamenexModel.TICKUNIT);
                }

                // Esegue il rendering (la View)
                if (renderCallback != null) {
                    renderCallback.run();
                }

                gamenexModel.setLastUpdate(now);
            }
        };
    }

    /**
     * Esegue un singolo passo logico della simulazione.
     * @param dt Il passo temporale costante (Delta Time).
     */
    private void tick(double dt) {
        if (!gamenexModel.isPaused()) {
            universeController.update(dt, inputManager, cameraModel);
        }
    }

    /**
     * Inverte lo stato di pausa del gioco.
     */
    public void togglePause() {
        gamenexModel.setPaused(!gamenexModel.isPaused());
    }

    /**
     * Forza lo stato di pausa a un valore specifico.
     */
    public void setPaused(boolean paused) {
        gamenexModel.setPaused(paused);
    }

    /**
     * Avvia il ciclo di gioco principale.
     */
    public void startGameLoop() {
        gameLoop.start();
    }

    /**
     * Ferma il ciclo di gioco principale.
     */
    public void stopGameLoop() {
        gameLoop.stop();
    }

    /**
     * Genera un nuovo asteroide in una posizione casuale vicino al centro della visuale attuale.
     * Utilizza il nuovo sistema di spawning dinamico.
     */
    public void spawnAsteroid() {
        double x = cameraModel.getX() + (universeController.getSpaceModel().getWidth() / 2.0);
        double y = cameraModel.getY() + (universeController.getSpaceModel().getHeight() / 2.0);
        
        x += (Math.random() - 0.5) * 400;
        y += (Math.random() - 0.5) * 400;

        UniverseSpawner.getInstance().spawn("ASTEROID", x, y);
    }

    /**
     * Genera un nuovo IscatMob vicino al centro della visuale attuale.
     * Utilizza il nuovo sistema di spawning dinamico.
     */
    public void spawnIscatMob() {
        double x = cameraModel.getX() + (universeController.getSpaceModel().getWidth() / 2.0);
        double y = cameraModel.getY() + (universeController.getSpaceModel().getHeight() / 2.0);

        x += (Math.random() - 0.5) * 400;
        y += (Math.random() - 0.5) * 400;

        UniverseSpawner.getInstance().spawn("ISCAT_MOB", x, y);
    }

    public void spawnHearth() {
        double x = cameraModel.getX() + (universeController.getSpaceModel().getWidth() / 2.0);
        double y = cameraModel.getY() + (universeController.getSpaceModel().getHeight() / 2.0);

        x += (Math.random() - 0.5) * 400;
        y += (Math.random() - 0.5) * 400;

        UniverseSpawner.getInstance().spawn("HEARTH", x, y);
    }

    private boolean showFps = false;

    /**
     * Determina se mostrare il contatore FPS sull'interfaccia.
     */
    public void setShowFps(boolean show) {
        this.showFps = show;
    }

    /**
     * Restituisce true se la visualizzazione degli FPS è attiva.
     */
    public boolean isShowFps() {
        return showFps;
    }

    /**
     * Restituisce il modello dell'universo (per accesso ai dati fisici).
     */
    public UniverseModel getSpaceModel() {
        return universeController.getSpaceModel();
    }

    /**
     * Restituisce il controller dell'universo.
     */
    public UniverseController getSpaceController() {
        return universeController;
    }

    /**
     * Restituisce il modello della telecamera.
     */
    public CameraModel getCameraModel() {
        return cameraModel;
    }
}
