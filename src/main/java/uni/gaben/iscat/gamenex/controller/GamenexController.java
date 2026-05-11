package uni.gaben.iscat.gamenex.controller;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.gamenex.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.InputManager;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.world.enviroment.space.SpaceController;
import uni.gaben.iscat.gamenex.world.enviroment.space.SpaceModel;

public class GamenexController {

    private SpaceController spaceController;
    private GamenexModel gamenexModel;
    private CameraModel cameraModel = new CameraModel();
    private AnimationTimer gameLoop;
    private Runnable renderCallback;
    private InputManager inputManager = new InputManager();

    public InputManager getInputManager() { return inputManager; }

    public void setRenderCallback(Runnable renderCallback) {
        this.renderCallback = renderCallback;
    }

    public GamenexController(GamenexModel gamenexModel) {
        this(gamenexModel, new SpaceController());
    }

    public GamenexController(GamenexModel gamenexModel, SpaceController spaceController) {

        this.gamenexModel = gamenexModel;
        this.spaceController = spaceController;

        initTimer(gamenexModel);

    }

    private void initTimer(GamenexModel gamenexModel) {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gamenexModel.getLastUpdate() == 0) {
                    gamenexModel.setLastUpdate(now);
                }

                gamenexModel.setNow(now);
                double dt = gamenexModel.getDt();

                if (dt > 0.25) {
                    dt = 0.25;
                }

                gamenexModel.setAccumulator(gamenexModel.getAccumulator() + dt);

                while (gamenexModel.getAccumulator() >= GamenexModel.TICKUNIT) {
                    tick(GamenexModel.TICKUNIT);
                    gamenexModel.setAccumulator(gamenexModel.getAccumulator() - GamenexModel.TICKUNIT);
                }

                if (renderCallback != null) {
                    renderCallback.run();
                }

                gamenexModel.setLastUpdate(now);
            }
        };
    }

    private void tick(double dt) {
        spaceController.update(dt, inputManager, cameraModel);
    }

    public void startGameLoop() {
        gameLoop.start();
    }
    public void stopGameLoop() {
        gameLoop.stop();
    }

    public SpaceModel getSpaceModel() {
        return spaceController.getSpaceModel();
    }

    public SpaceController getSpaceController() {
        return spaceController;
    }

    public CameraModel getCameraModel() {
        return cameraModel;
    }
}
