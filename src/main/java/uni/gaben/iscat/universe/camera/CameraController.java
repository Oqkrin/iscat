package uni.gaben.iscat.universe.camera;

/**
 * Controller that updates the {@link CameraModel} to follow a target position.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Converting the target world position to a camera centre target.</li>
 *   <li>Applying an initial snap to avoid a "flying‑in" artefact.</li>
 *   <li>Updating both camera springs with the given timestep.</li>
 * </ul>
 * </p>
 */
public class CameraController {

    /**
     * Updates the camera model to chase the given target position.
     *
     * <p>The method computes the desired camera centre as
     * {@code (targetWorldX - viewW/2, targetWorldY - viewH/2)}. This target is
     * fed into the X and Y springs of the camera model. On the very first frame
     * where the view dimensions are valid, the camera is snapped directly to the
     * target to prevent a visual jump from (0,0).</p>
     *
     * @param model        the camera model to update (must not be {@code null})
     * @param targetWorldX target X coordinate in world pixels (e.g. player centre)
     * @param targetWorldY target Y coordinate in world pixels
     * @param viewW        current canvas width (screen pixels, must be > 0 for snapping)
     * @param viewH        current canvas height (screen pixels, must be > 0 for snapping)
     * @param dt           timestep in seconds (used for spring integration)
     */
    public void update(CameraModel model,
                       double targetWorldX,
                       double targetWorldY,
                       double viewW,
                       double viewH,
                       double dt) {

        // Desired camera centre: centre the target in the viewport
        double targetCentreX = targetWorldX - viewW / 2.0;
        double targetCentreY = targetWorldY - viewH / 2.0;

        // Tell the springs where we want them to go
        model.getSpringX().setTarget(targetCentreX);
        model.getSpringY().setTarget(targetCentreY);

        // Snap the camera to the target on the first valid frame.
        // Without this, the springs would interpolate from their initial (0,0)
        // position, causing a distracting "camera flies into place" effect.
        if (!model.isSnapped() && viewW > 0 && viewH > 0) {
            model.getSpringX().setPosition(targetCentreX);
            model.getSpringY().setPosition(targetCentreY);
            model.setSnapped(true);
        }

        // Advance the spring simulations
        model.getSpringX().update(dt);
        model.getSpringY().update(dt);
    }
}