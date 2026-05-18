package uni.gaben.iscat.gamenex.controller;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.universe.UniverseController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;

public class GamenexController {
    private GamenexModel gamenexModel;
    private UniverseController universeController;
    private AnimationTimer gameLoop;
    private Runnable drawCall;
    private GamenexInputs gamenexInputs = new GamenexInputs();

    public GamenexInputs getInputManager() {
        return gamenexInputs;
    }

    public void setDrawCall(Runnable drawCall) {
        this.drawCall = drawCall;
    }

    public GamenexController(GamenexModel gamenexModel, UniverseController universeController) {
        this.gamenexModel = gamenexModel;
        this.universeController = universeController;

        // Inizializzazione dello Spawner e generazione del mondo iniziale
        UniverseSpawner.getInstance().init(getUniverseModel(), universeController);

        double midX = getUniverseModel().getWidth() / 2.0;
        double midY = getUniverseModel().getHeight() / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);

        // Stabilize spring hooks immediately to prevent camera lens pan jitter during launch
        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        setupTimer(gamenexModel);
    }

    private void setupTimer(GamenexModel gamenexModel) {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gamenexModel.getLastUpdate() == 0) {
                    gamenexModel.setLastUpdate(now);
                    return;
                }

                gamenexModel.setNow(now);
                double dt = gamenexModel.getDt();

                if (dt > GamenexModel.ACCUMULATORUNIT) {
                    dt = GamenexModel.ACCUMULATORUNIT;
                }

                tick(dt);

                if (drawCall != null) {
                    drawCall.run();
                }

                gamenexModel.setLastUpdate(now);
            }
        };
    }

    private void tick(double dt) {
        if (gamenexModel.isPaused()) {
            universeController.updatev(dt, gamenexInputs, getCameraModel());
        }
    }

    public void togglePause() {
        gamenexModel.setPaused(gamenexModel.isPaused());
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    public void debugSpawn(String spawnableId) {
        // THE FIX: Drops models symmetrically balanced over the actual camera center position
        double spawnWorldX = getCameraModel().getX() + ((Math.random() - 0.5) * 400);
        double spawnWorldY = getCameraModel().getY() + ((Math.random() - 0.5) * 400);

        UniverseSpawner.getInstance().spawn(spawnableId, spawnWorldX, spawnWorldY);
    }

    private boolean showFps = false;

    public void setShowFps(boolean show) {
        this.showFps = show;
    }

    public boolean isFpsOn() {
        return showFps;
    }

    public UniverseModel getUniverseModel() {
        return gamenexModel.getUniverseModel();
    }

    public UniverseController getUniverseController() {
        return universeController;
    }

    public CameraModel getCameraModel() {
        return gamenexModel.getCameraModel();
    }

    public void resetUniverse() {
        UniverseModel newUniverse = new UniverseModel();
        gamenexModel.setUniverseModel(newUniverse);
        universeController = new UniverseController(newUniverse);

        UniverseSpawner.getInstance().init(newUniverse, universeController);
        
        double midX = newUniverse.getWidth() / 2.0;
        double midY = newUniverse.getHeight() / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);
    }
}