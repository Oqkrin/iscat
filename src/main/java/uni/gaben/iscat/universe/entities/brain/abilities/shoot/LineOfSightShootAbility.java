package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;

public class LineOfSightShootAbility extends ShootAbility {
    private final double maxAngle;
    private final Ray ray = new Ray(new Vector2(), Vector2.create(1, Math.PI));
    private final Vector2 forward = new Vector2();
    private final DetectFilter<Body, BodyFixture> losFilter;

    public LineOfSightShootAbility(double combatRange, double cooldownSec,
                                   ProjectileType bulletType, Pattern pattern,
                                   Target target, boolean aimAtTarget, double maxAngle,
                                   AbstractPhysicalEntityModel self,
                                   double dannoProiettile,
                                   int attackStateIndex) { // <-- AGGIUNTO QUI

        super(combatRange, cooldownSec, bulletType, pattern, target, aimAtTarget, 0, dannoProiettile, attackStateIndex);
        this.maxAngle = maxAngle;
        this.losFilter = new DetectFilter<>(true, true, null) {
            @Override
            public boolean isAllowed(Body body, BodyFixture fixture) {
                return body != self && super.isAllowed(body, fixture);
            }
        };
    }

    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        if (!super.canActivate(self, world, dt)) return false;

        Vector2 myPos = self.getTransform().getTranslation();
        Vector2 targetPos = target.getPosition(world);
        if (targetPos == null) return false;

        // Direction to target (avoid copy if possible, but distance check needs a copy)
        Vector2 toTarget = targetPos.copy().subtract(myPos);
        double distance = toTarget.getMagnitude();
        if (distance < 0.01) return true;

        // Forward direction
        double facingAngle = self.getTransform().getRotationAngle();
        forward.x = Math.cos(facingAngle);
        forward.y = Math.sin(facingAngle);

        Vector2 dirToTarget = toTarget.getNormalized();
        double angleToTarget = Math.acos(forward.dot(dirToTarget));
        if (angleToTarget > maxAngle) return false;

        // Reuse ray object
        ray.setStart(myPos);
        ray.setDirection(dirToTarget);

        RaycastResult<Body, BodyFixture> closest = world.raycastClosest(ray, distance, losFilter);
        if (closest == null) return true;

        Body hit = closest.getBody();
        if (hit instanceof AbstractPhysicalEntityModel &&
                hit.getTransform().getTranslation().distanceSquared(targetPos) < 0.01) {
            return true;
        }
        return closest.getRaycast().getDistance() >= distance - 0.01;
    }
}