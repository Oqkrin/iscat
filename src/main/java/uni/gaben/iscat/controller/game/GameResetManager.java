package uni.gaben.iscat.controller.game;

import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.utils.SessionScoreTracker;

public class GameResetManager {

    private final GameModel gameModel;
    private final GameStatsManager statsManager;

    public GameResetManager(GameModel gameModel, GameStatsManager statsManager) {
        this.gameModel = gameModel;
        this.statsManager = statsManager;
    }

    public void reset(double canvasWidth, double canvasHeight) {
        SessionScoreTracker.getInstance().reset();

        // Generate new universe mapping
        gameModel.resetUniverse();
        UniverseModel universeModel = gameModel.getUniverseModel();
        CameraModel cameraModel = gameModel.getCameraModel();

        universeModel.setDimensions(canvasWidth, canvasHeight);
        universeModel.getStarfieldModel().generate(canvasWidth, canvasHeight);

        double midX = canvasWidth / 2.0;
        double midY = canvasHeight / 2.0;
        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        UniverseSpawner.getInstance().spawnInitialAsteroidBelts(midX, midY);

        cameraModel.getSpringX().setPosition(midX);
        cameraModel.getSpringY().setPosition(midY);
        cameraModel.getSpringX().snap();
        cameraModel.getSpringY().snap();
    }
}