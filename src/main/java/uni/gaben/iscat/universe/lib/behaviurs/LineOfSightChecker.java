package uni.gaben.iscat.universe.lib.behaviurs;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.UniverseModel;

public class LineOfSightChecker {
    private final Body ownerBody;
    private boolean hasLoS = false;

    public LineOfSightChecker(Body ownerBody) {
        this.ownerBody = ownerBody;
    }

    public void update(Vector2 from, Vector2 to, UniverseModel world) {
        double distance = from.distance(to);
        if (distance < 0.01) {
            hasLoS = true;
            return;
        }
        Vector2 direction = to.copy().subtract(from).getNormalized();
        Ray ray = new Ray(from, direction);

        DetectFilter<Body, BodyFixture> filter = new DetectFilter<>(true, true, null) {
            @Override
            public boolean isAllowed(Body body, BodyFixture fixture) {
                // Skip the entity's own body
                if (body == ownerBody) return false;
                return super.isAllowed(body, fixture);
            }
        };

        RaycastResult<Body, BodyFixture> result = world.raycastClosest(ray, distance, filter);
        hasLoS = (result == null);
    }

    public boolean hasLineOfSight() {
        return hasLoS;
    }
}