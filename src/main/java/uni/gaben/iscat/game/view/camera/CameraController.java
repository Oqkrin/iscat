package uni.gaben.iscat.game.view.camera;

/**
 * Logic for updating the CameraModel.
 */
public class CameraController {

    /**
     * Updates the camera model to chase the target position.
     * @param model        The camera model to update
     * @param targetWorldX target X in world pixels
     * @param targetWorldY target Y in world pixels
     * @param viewW        canvas width
     * @param viewH        canvas height
     * @param dt           timestep
     */
    public void update(CameraModel model, double targetWorldX, double targetWorldY,
                       double viewW, double viewH, double dt) {
        
        double targetX = targetWorldX - viewW / 2.0;
        double targetY = targetWorldY - viewH / 2.0;

        model.getSpringX().setTarget(targetX);
        model.getSpringY().setTarget(targetY);

        // Snap on first valid frame to avoid lerp-from-origin glitch
        if (!model.isSnapped() && viewW > 0 && viewH > 0) {
            model.getSpringX().setPosition(targetX);
            model.getSpringY().setPosition(targetY);
            model.setSnapped(true);
        }

        model.getSpringX().update(dt);
        model.getSpringY().update(dt);
    }
}
