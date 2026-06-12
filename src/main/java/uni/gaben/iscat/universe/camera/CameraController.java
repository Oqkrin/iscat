package uni.gaben.iscat.universe.camera;

import uni.gaben.iscat.utils.Updatable;

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
    public void update(CameraModel model, double targetWorldX, double targetWorldY,
                       double viewW, double viewH, double mouseWorldX, double mouseWorldY, double dt) {

        double zoom = model.getZoom();

        // 1. Calculate Look-Ahead (toward mouse)
        double offsetX = (mouseWorldX - targetWorldX) * 0.15;
        double offsetY = (mouseWorldY - targetWorldY) * 0.15;

        // 2. Clamp it
        double maxOffset = 150 / zoom;
        offsetX = Math.clamp(offsetX, -maxOffset, maxOffset);
        offsetY = Math.clamp(offsetY, -maxOffset, maxOffset);

        // 3. SET TARGET AS WORLD CENTER (Remove the -viewW/2 logic)
        double targetCentreX = targetWorldX + offsetX;
        double targetCentreY = targetWorldY + offsetY;

        model.getSpringX().setTarget(targetCentreX);
        model.getSpringY().setTarget(targetCentreY);

        // Snap on first frame
        if (!model.isSnapped() && viewW > 0 && viewH > 0) {
            model.getSpringX().setPosition(targetCentreX);
            model.getSpringY().setPosition(targetCentreY);
            model.setSnapped(true);
        }

        model.getSpringX().update(dt);
        model.getSpringY().update(dt);
    }
}
