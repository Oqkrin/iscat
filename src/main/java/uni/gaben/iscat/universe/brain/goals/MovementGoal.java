package uni.gaben.iscat.universe.brain.goals;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.brain.Target;

import java.util.Random;

@FunctionalInterface
public interface MovementGoal {
    Vector2 compute(AbstractEntityModel self, UniverseModel world, double dt);

    // ── Idle ───────────────────────────────────────────────────────────────
    static MovementGoal idle() {
        return (self, world, dt) -> UU.vector2zero();
    }

    // ── Chase a target ─────────────────────────────────────────────────────
    static MovementGoal chase(Target target, double speed) {
        return (self, world, dt) -> {
            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) return UU.vector2zero();
            Vector2 to = targetPos.copy().subtract(self.getTransform().getTranslation());
            if (to.getMagnitude() < 0.1) return UU.vector2zero();
            return to.getNormalized().multiply(speed);
        };
    }

    // ── Flee from a target ─────────────────────────────────────────────────
    static MovementGoal flee(Target target, double speed) {
        return (self, world, dt) -> {
            Vector2 threat = target.getPosition(world);
            if (threat == null) return UU.vector2zero();
            Vector2 away = self.getTransform().getTranslation().copy().subtract(threat);
            if (away.getMagnitude() < 0.1) return UU.vector2zero();
            return away.getNormalized().multiply(speed);
        };
    }

    // ── Orbit around a target ──────────────────────────────────────────────
    static MovementGoal orbit(Target center, double speed, double radius, boolean clockwise) {
        return (self, world, dt) -> {
            Vector2 centerPos = center.getPosition(world);
            if (centerPos == null) return UU.vector2zero();
            Vector2 toCenter = centerPos.copy().subtract(self.getTransform().getTranslation());
            double dist = toCenter.getMagnitude();
            // Radial correction: pull toward ideal radius
            Vector2 radial = toCenter.getNormalized().multiply((dist - radius) * 0.5);
            // Tangential movement: orbit circle
            Vector2 tangent = new Vector2(-toCenter.y, toCenter.x);
            if (clockwise) tangent.negate();
            return radial.add(tangent.getNormalized().multiply(speed));
        };
    }

    // ── Wander (random drift) ──────────────────────────────────────────────
    static MovementGoal wander(double speed, double minDist, double maxDist) {
        // Wander state must be stored externally (could use a class, but here we use a trick:
        // attach a mutable reference via a 1-element array)
        return new MovementGoal() {
            private final Random rand = new Random();
            private Vector2 wanderTarget = null;

            @Override
            public Vector2 compute(AbstractEntityModel self, UniverseModel world, double dt) {
                Vector2 pos = self.getTransform().getTranslation();
                if (wanderTarget == null || pos.distance(wanderTarget) < 0.2) {
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double dist = minDist + rand.nextDouble() * (maxDist - minDist);
                    wanderTarget = pos.add(new Vector2(Math.cos(angle), Math.sin(angle)).multiply(dist));
                }
                Vector2 to = wanderTarget.copy().subtract(pos);
                if (to.getMagnitude() < 0.1) return UU.vector2zero();
                return to.getNormalized().multiply(speed);
            }
        };
    }

    // ── Kite (maintain distance from target) ───────────────────────────────
    static MovementGoal kite(Target target, double speed, double preferredRange) {
        return (self, world, dt) -> {
            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) return UU.vector2zero();
            Vector2 toTarget = targetPos.copy().subtract(self.getTransform().getTranslation());
            double dist = toTarget.getMagnitude();
            if (dist < preferredRange * 0.8) {
                // Too close → retreat
                return toTarget.getNormalized().multiply(-speed * 0.6);
            } else if (dist > preferredRange * 1.2) {
                // Too far → advance
                return toTarget.getNormalized().multiply(speed * 0.4);
            } else {
                // In sweet spot → strafe
                Vector2 perp = new Vector2(-toTarget.y, toTarget.x);
                if (Math.random() > 0.5) perp.negate();
                return perp.getNormalized().multiply(speed * 0.5);
            }
        };
    }

    // ── Move to a fixed point ──────────────────────────────────────────────
    static MovementGoal moveTo(Vector2 point, double speed) {
        return (self, world, dt) -> {
            Vector2 to = point.copy().subtract(self.getTransform().getTranslation());
            if (to.getMagnitude() < 0.1) return UU.vector2zero();
            return to.getNormalized().multiply(speed);
        };
    }

    // ── Move to a dynamic target (stop when close) ─────────────────────────
    static MovementGoal moveToTarget(Target target, double speed) {
        return (self, world, dt) -> {
            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) return UU.vector2zero();
            Vector2 to = targetPos.copy().subtract(self.getTransform().getTranslation());
            if (to.getMagnitude() < 0.5) return UU.vector2zero();  // arrival
            return to.getNormalized().multiply(speed);
        };
    }

}