package uni.gaben.iscat.controller.game;

import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.asteroids.AsteroidMazeGenerator;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.SessionScoreTracker;

/**
 * Gestore del ciclo di vita del mondo di gioco, responsabile delle fasi di allocazione,
 * distruzione e ripristino dell'universo astronomico e delle sue componenti fisiche.
 * Coordina la reinizializzazione dei motori di spawn, la generazione procedurale degli ostacoli,
 * il riallineamento della telecamera e l'azzeramento delle metriche di sessione.
 */
public class GameLifecycleManager {

    /** Il modello dati globale contenente lo stato corrente e i riferimenti ai moduli dell'universo. */
    private final GameModel gameModel;

    /** Il gestore degli input fisici da azzerare all'avvio della partita. */
    private final GameInputsHandler inputs;

    /** Il timer del ciclo principale di gioco da resettare per stabilizzare il calcolo del delta time. */
    private final GameLoopTimer loopTimer;

    /**
     * Costruisce il manager del ciclo di vita collegandolo ai moduli core del gameplay.
     *
     * @param gameModel Il modello globale del gioco.
     * @param inputs    Il gestore degli input fisici di gioco.
     * @param loopTimer Il timer che scandisce il ciclo di aggiornamento.
     */
    public GameLifecycleManager(GameModel gameModel, GameInputsHandler inputs, GameLoopTimer loopTimer) {
        this.gameModel = gameModel;
        this.inputs = inputs;
        this.loopTimer = loopTimer;
    }

    /**
     * Ripristina integralmente lo stato fisico dell'universo e alloca ex-novo i controller dedicati.
     * Il processo include la rigenerazione dello sfondo stellato (starfield), la pulizia dello spawner,
     * il posizionamento iniziale del giocatore (con la skin scelta), la creazione del labirinto di asteroidi,
     * il riallineamento istantaneo della telecamera e il reset dello score tracker.
     *
     * @param onPlayerDeath Callback da eseguire nel thread grafico in caso di sconfitta del giocatore.
     * @param skinKey       La stringa identificativa (chiave di registro) della skin selezionata per la navicella.
     * @return Un record {@link GameControllers} contenente i riferimenti aggiornati a {@link UniverseController} e {@link UniverseWaveController}.
     */
    public GameControllers resetUniverse(Runnable onPlayerDeath, String skinKey) {
        CameraModel camera = gameModel.getCameraModel();
        double canvasW = camera.getScreenWidth();
        double canvasH = camera.getScreenHeight();

        if (canvasW <= 0 || canvasH <= 0) {
            canvasW = UniverseSettings.DEFAULT_WIDTH;
            canvasH = UniverseSettings.DEFAULT_HEIGHT;
        }

        // 1. Reset dell'universo nel modello dati
        gameModel.resetUniverse();
        UniverseModel newUniverse = gameModel.getUniverseModel();
        newUniverse.setDimensions(canvasW, canvasH);
        newUniverse.getStarfieldModel().generate(canvasW, canvasH);

        // 2. Creazione dei controller per il nuovo universo
        UniverseController newUniverseController = new UniverseController(newUniverse);
        UniverseWaveController newWaveController = new UniverseWaveController();
        newWaveController.reset();

        // 3. Re‑inizializzazione del modulo di spawn singleton con i nuovi riferimenti
        UniverseSpawner.getInstance().init(newUniverse, newUniverseController, newWaveController);

        // 4. Spawn del giocatore e generazione del labirinto di asteroidi
        UniverseSpawner.getInstance().spawnPlayer(0.0, 0.0, skinKey);

        AsteroidMazeGenerator asteroidMazeGenerator = new AsteroidMazeGenerator();
        asteroidMazeGenerator.generate(0.0, 0.0);

        // 5. Reset immediato (snap) delle molle di ammortizzamento della telecamera
        camera.getSpringX().setPosition(0.0);
        camera.getSpringY().setPosition(0.0);
        camera.getSpringX().snap();
        camera.getSpringY().snap();

        // 6. Ri-assegnazione della callback di morte al modello del giocatore
        newUniverse.getPlayer().setOnDeathCallback(onPlayerDeath);

        // 7. Reset dei flag di input e del timer del loop
        inputs.resetInputs();
        loopTimer.resetTimer();

        // 8. Reset del tracciatore dei punteggi della sessione corrente
        SessionScoreTracker.getInstance().reset();

        return new GameControllers(newUniverseController, newWaveController);
    }

    /**
     * Record contenitore utilizzato per incapsulare e trasferire in modo atomico la coppia di controller
     * (universo e ondate) generati al termine della procedura di ripristino.
     *
     * @param universeController Il controller della fisica e delle entità dell'universo.
     * @param waveController     Il controller di gestione delle ondate di nemici.
     */
    public record GameControllers(UniverseController universeController, UniverseWaveController waveController) {}
}