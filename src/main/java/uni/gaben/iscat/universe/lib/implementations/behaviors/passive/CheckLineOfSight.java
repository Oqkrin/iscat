package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * Runs every frame as a {@link PassiveBehavior} and updates the {@code isFree}
 * flag that shooter/seeker behaviors query.
 *
 * BUG FIXED: previously implemented the old {@code AiBehavior} interface.
 * {@code addPassive(checkLineOfSight)} would silently no-op because
 * {@code AiBehaviours.add()} only pattern-matches MovementBehavior /
 * AttackBehavior / PassiveBehavior. The LoS check therefore never ran,
 * so shooters in Core, FakeIscat, and Master could never fire.
 */
public class CheckLineOfSight implements PassiveBehavior {

    private boolean isFree = true;
    private final AbstractEntityModel target;

    public CheckLineOfSight(AbstractEntityModel target) {
        this.target = target;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (universe == null || target == null) return;

        Vector2 startPos  = npc.getTransform().getTranslation();
        Vector2 targetPos = target.getTransform().getTranslation();

        Vector2 direction       = targetPos.difference(startPos);
        double  distanceToTarget = direction.getMagnitude();

        if (distanceToTarget <= 0.001) {
            this.isFree = true;
            return;
        }

        direction.normalize();

        Ray ray = new Ray(startPos, direction);
        RaycastResult result = universe.raycastClosest(
                ray, distanceToTarget,
                new DetectFilter(true, true, Filter.DEFAULT_FILTER));

        if (result != null && result.getBody() != null) {
            Body firstHit = (Body) result.getBody();
            this.isFree = (firstHit == target);
        } else {
            this.isFree = true;
        }
    }

    public boolean hasLineOfSightWithTarget() {
        return isFree;
    }
}
