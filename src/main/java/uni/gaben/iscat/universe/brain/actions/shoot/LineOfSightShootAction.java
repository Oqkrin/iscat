package uni.gaben.iscat.universe.brain.actions.shoot;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.RaycastResult;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.projectiles.ProjectileType;

public class LineOfSightShootAction extends ShootAction {
    private final double maxAngle;   // half‑angle of the forward cone (radians)

    public LineOfSightShootAction(double combatRange, double cooldownSec,
                                  ProjectileType bulletType, AttackPattern pattern,
                                  Target target, double maxAngle) {
        super(combatRange, cooldownSec, bulletType, pattern, target);
        this.maxAngle = maxAngle;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        if (!super.canActivate(self, world, dt)) return false;

        // --- Angle check ---
        Vector2 myPos = self.getTransform().getTranslation();
        Vector2 targetPos = target.getPosition(world);
        if (targetPos == null) return false;

        Vector2 toTarget = targetPos.copy().subtract(myPos);
        double distance = toTarget.getMagnitude();
        if (distance < 0.01) return true;   // already on top

        // Entity's forward direction (based on current rotation)
        double facingAngle = self.getTransform().getRotationAngle();
        Vector2 forward = new Vector2(Math.cos(facingAngle), Math.sin(facingAngle));

        double angleToTarget = Math.acos(forward.dot(toTarget.getNormalized()));
        if (angleToTarget > maxAngle) {
            return false;   // target outside forward cone
        }

        // --- Line of sight check (unchanged) ---
        Ray ray = new Ray(myPos, toTarget.getNormalized());
        DetectFilter<Body, BodyFixture> filter = new DetectFilter<>(true, true, null) {
            @Override
            public boolean isAllowed(Body body, BodyFixture fixture) {
                if (body == self) return false;
                return super.isAllowed(body, fixture);
            }
        };

        RaycastResult<Body, BodyFixture> closest = world.raycastClosest(ray, distance, filter);
        if (closest == null) return true;

        Body hitBody = closest.getBody();
        if (hitBody instanceof AbstractEntityModel &&
                hitBody.getTransform().getTranslation().distanceSquared(targetPos) < 0.01) {
            return true;
        }
        return closest.getRaycast().getDistance() >= distance - 0.01;
    }
}