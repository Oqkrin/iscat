package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

import static uni.gaben.iscat.universe.brain.goals.MovementGoal.ZERO;

/**
 * BoundaryAvoidanceModifier keeps entities within the world boundaries.
 * 
 * COORDINATE SYSTEM:
 * - World boundaries are defined in PIXELS (0,0) to (1280, 720)
 * - Entity positions are in METERS (dyn4j physics)
 * - Conversion: 64 pixels = 1 meter (via UU.mToPx/pxToM)
 * 
 * IMPORTANT: Unlike the camera which centers on a target, entities exist
 * in absolute world coordinates where (0,0) is top-left corner in pixels.
 */
public class BoundaryAvoidanceModifier implements MovementModifier {

    // Workspace vector (reused per frame, zero allocation)
    private final Vector2 avoidance = new Vector2();

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel world, double maxForce, double dt) {
        // Reset workspace primitives
        avoidance.x = 0;
        avoidance.y = 0;

        // Get entity position in PIXELS (world boundaries are in pixels)
        double px = UU.mToPx(self.getTransform().getTranslationX());
        double py = UU.mToPx(self.getTransform().getTranslationY());

        // Entity radius in pixels
        double radiusPx = UU.mToPx(self.getWidthMeters() / 2.0);
        if (radiusPx <= 0) radiusPx = 10.0;

        // World boundaries in pixels: (0, 0) to (w, h)
        double w = world.getWidth();   // 1280 pixels
        double h = world.getHeight();  // 720 pixels

        // Fixed margin (no mass/braking calculations needed for velocity blending)
        double margin = 150.0; // pixels

        // Compute boundary avoidance as velocity contribution
        // Reynolds steering: desired velocity points AWAY from boundary
        
        // X-axis boundaries
        if (px < margin) {
            // Too close to LEFT edge (x = 0)
            // Push RIGHT (positive X direction)
            double penetration = (margin - px) / Math.max(margin, 1.0); // 0..1 (1 = at edge, 0 = at margin limit)
            avoidance.x = maxForce * penetration;
        } else if (px > w - margin) {
            // Too close to RIGHT edge (x = w)
            // Push LEFT (negative X direction)
            double penetration = (px - (w - margin)) / Math.max(margin, 1.0); // 0..1
            avoidance.x = -maxForce * penetration;
        }

        // Y-axis boundaries
        if (py < margin) {
            // Too close to TOP edge (y = 0)
            // Push DOWN (positive Y direction)
            double penetration = (margin - py) / Math.max(margin, 1.0); // 0..1
            avoidance.y = maxForce * penetration;
        } else if (py > h - margin) {
            // Too close to BOTTOM edge (y = h)
            // Push UP (negative Y direction)
            double penetration = (py - (h - margin)) / Math.max(margin, 1.0); // 0..1
            avoidance.y = -maxForce * penetration;
        }

        return avoidance;
    }
}