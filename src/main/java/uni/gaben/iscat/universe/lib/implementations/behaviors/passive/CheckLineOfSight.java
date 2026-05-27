package uni.gaben.iscat.universe.lib.implementations.behaviors.passive;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;

public class CheckLineOfSight implements PassiveBehavior {

    private boolean isFree = true;
    private final AbstractEntityModel target;

    public CheckLineOfSight(AbstractEntityModel target) {
        this.target = target;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (universe == null || target == null) return;

        // 1. Establish coordinates
        Vector2 startPos = npc.getTransform().getTranslation();
        Vector2 targetPos = target.getTransform().getTranslation();

        // 2. Calculate vector tracking metrics
        Vector2 direction = targetPos.difference(startPos);
        double distanceToTarget = direction.getMagnitude();

        if (distanceToTarget <= 0.001) {
            this.isFree = true;
            return;
        }

        // Normalize the orientation direction vector
        direction.normalize();

        // 3. Create the Ray object
        Ray ray = new Ray(startPos, direction);

        // 4. Query the world matching your JavaDoc method signature:
        // Parameters: ray, maxLength, filter (null means check everything)
        RaycastResult result = universe.raycastClosest(ray, distanceToTarget, new DetectFilter(true, true, Filter.DEFAULT_FILTER));

        // 5. Evaluate the results array
        if (result != null && result.getBody() != null) {
            Body firstObjectHit = (Body) result.getBody();

            if (firstObjectHit == target) {
                // Closest thing hit was our target. Line of sight is completely clear!
                this.isFree = true;
            } else {
                // Something else blocked the ray vector before it reached the target distance
                this.isFree = false;
            }
        } else {
            // The ray reached the distance limit without striking any active fixtures
            this.isFree = true;
        }
    }

    public boolean hasLineOfSightWithTarget() {
        return isFree;
    }
}