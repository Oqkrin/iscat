package uni.gaben.iscat.controller.game;

import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.utils.SessionScoreTracker;

/**
 * Handles the setup, destruction, and reset of the game universe.
 */
public class GameLifecycleManager {

    private final GameModel gameModel;
    private final GameInputsHandler inputs;
    private final GameLoopTimer loopTimer;

    public GameLifecycleManager(GameModel gameModel, GameInputsHandler inputs, GameLoopTimer loopTimer) {
        this.gameModel = gameModel;
        this.inputs = inputs;
        this.loopTimer = loopTimer;
    }

    /**
     * Resets the entire universe physics state and recreates necessary controllers.
     *
     * @param onPlayerDeath Callback to execute when the player dies.
     * @return The freshly constructed UniverseController and UniverseWaveController.
     */
    public ControllersBundle resetUniverse(Runnable onPlayerDeath) {
        CameraModel camera = gameModel.getCameraModel();
        double canvasW = camera.getScreenWidth();
        double canvasH = camera.getScreenHeight();

        if (canvasW <= 0 || canvasH <= 0) {
            canvasW = UniverseSettings.DEFAULT_WIDTH;
            canvasH = UniverseSettings.DEFAULT_HEIGHT;
        }

        // 1. Reset the universe model (fresh physics world)
        gameModel.resetUniverse();
        UniverseModel freshUniverse = gameModel.getUniverseModel();
        freshUniverse.setDimensions(canvasW, canvasH);
        freshUniverse.getStarfieldModel().generate(canvasW, canvasH);

        // 2. Create brand new controllers for the fresh universe
        UniverseController newUniverseController = new UniverseController(freshUniverse);
        UniverseWaveController newWaveController = new UniverseWaveController();
        newWaveController.reset();

        // 3. Re‑initialise the UniverseSpawner with the new universe and controllers
        UniverseSpawner.getInstance().init(freshUniverse, newUniverseController, newWaveController);

        // 4. Spawn player and asteroids
        double midX = canvasW / 2.0;
        double midY = canvasH / 2.0;
        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        AsteroidFieldManager asteroidFieldManager = new AsteroidFieldManager();
        asteroidFieldManager.spawnInitialAsteroidBelts(midX, midY);

        // 5. Reset camera springs to the new player position
        camera.getSpringX().setPosition(midX);
        camera.getSpringY().setPosition(midY);
        camera.getSpringX().snap();
        camera.getSpringY().snap();

        // 6. Re‑attach death callback
        freshUniverse.getPlayer().setOnDeathCallback(onPlayerDeath);

        // 7. Reset input flags and loop timer
        inputs.resetInputs();
        loopTimer.resetTimer();

        // 8. Reset session score tracker
        SessionScoreTracker.getInstance().reset();

        return new ControllersBundle(newUniverseController, newWaveController);
    }

    public record ControllersBundle(UniverseController universeController, UniverseWaveController waveController) {}
}
