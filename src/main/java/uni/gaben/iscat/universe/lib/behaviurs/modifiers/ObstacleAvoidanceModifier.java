package uni.gaben.iscat.universe.lib.behaviurs.modifiers;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class ObstacleAvoidanceModifier implements AvoidanceModifier {
    private final double lookAhead = 2.0;
    private final double angleStep = Math.toRadians(30);

    @Override
    public Vector2 modify(Vector2 desired, AbstractEntityModel entity, UniverseModel world, double dt) {
        if (desired.getMagnitudeSquared() < 0.01) return desired;

        Vector2 pos = entity.getTransform().getTranslation();
        Vector2 dir = desired.getNormalized();
        double bestDot = -1;
        Vector2 bestDir = null;

        for (double off = -angleStep; off <= angleStep; off += angleStep) {
            Vector2 testDir = dir.rotate(off);
            Ray ray = new Ray(pos, testDir);

            // Create a proper DetectFilter that ignores sensors, disabled bodies, and the entity itself.
            DetectFilter<Body, BodyFixture> filter = new DetectFilter<>(true, true, null) {
                @Override
                public boolean isAllowed(Body body, BodyFixture fixture) {
                    // Skip the entity's own body so it doesn't block itself
                    if (body == entity) return false;
                    return super.isAllowed(body, fixture);
                }
            };

            RaycastResult<Body, BodyFixture> hit = world.raycastClosest(ray, lookAhead, filter);

            if (hit == null || hit.getRaycast().getDistance() >= lookAhead - 0.2) {
                double dot = testDir.dot(dir);
                if (dot > bestDot) {
                    bestDot = dot;
                    bestDir = testDir;
                }
            }
        }

        if (bestDir != null && bestDot < 0.99) {
            return bestDir.multiply(desired.getMagnitude());
        }
        return desired;
    }
}