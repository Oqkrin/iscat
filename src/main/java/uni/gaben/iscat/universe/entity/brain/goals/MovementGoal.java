package uni.gaben.iscat.universe.entity.brain.goals;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Target;

import java.util.Random;

@FunctionalInterface
public interface MovementGoal {
    // Shared zero vector (singleton, avoids allocations)
    Vector2 ZERO = new Vector2(0, 0);
    
    Vector2 compute(AbstractEntityModel self, UniverseModel world, double dt);

    // ── Idle ───────────────────────────────────────────────────────────────
    static MovementGoal idle() {
        return (self, world, dt) -> ZERO;
    }

    // ── Chase a target ─────────────────────────────────────────────────────
    static MovementGoal chase(Target target, double speed) {
        // Workspace vector (reused per frame, zero allocation)
        Vector2 workspace = new Vector2();
        return (self, world, dt) -> {
            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) return ZERO;
            
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute direction inline (no copy, no allocations)
            workspace.x = targetPos.x - selfPos.x;
            workspace.y = targetPos.y - selfPos.y;
            
            double magSq = workspace.x * workspace.x + workspace.y * workspace.y;
            if (magSq < 0.01) return ZERO; // Too close
            
            // Single-pass normalization and scaling
            double magnitude = Math.sqrt(magSq);
            double scale = speed / magnitude;
            workspace.x *= scale;
            workspace.y *= scale;
            return workspace;
        };
    }

    // ── Flee from a target ─────────────────────────────────────────────────
    static MovementGoal flee(Target target, double speed) {
        // Workspace vector (reused per frame, zero allocation)
        Vector2 workspace = new Vector2();
        return (self, world, dt) -> {
            Vector2 threat = target.getPosition(world);
            if (threat == null) return ZERO;
            
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute away vector inline (no copy, no allocations)
            workspace.x = selfPos.x - threat.x;
            workspace.y = selfPos.y - threat.y;
            
            double magSq = workspace.x * workspace.x + workspace.y * workspace.y;
            if (magSq < 0.01) return ZERO; // Too close
            
            // Single-pass normalization and scaling
            double magnitude = Math.sqrt(magSq);
            double scale = speed / magnitude;
            workspace.x *= scale;
            workspace.y *= scale;
            return workspace;
        };
    }

    // ── Orbit around a target ──────────────────────────────────────────────
    static MovementGoal orbit(Target center, double speed, double radius, boolean clockwise) {
        // Workspace vectors (reused per frame, zero allocation)
        Vector2 result = new Vector2();
        return (self, world, dt) -> {
            Vector2 centerPos = center.getPosition(world);
            if (centerPos == null) return ZERO;
            
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute toCenter inline
            double toCenterX = centerPos.x - selfPos.x;
            double toCenterY = centerPos.y - selfPos.y;
            double dist = Math.sqrt(toCenterX * toCenterX + toCenterY * toCenterY);
            
            if (dist < 0.01) return ZERO;
            
            // Radial correction: pull toward ideal radius
            double radialScale = (dist - radius) * 0.5 / dist;
            double radialX = toCenterX * radialScale;
            double radialY = toCenterY * radialScale;
            
            // Tangential movement: perpendicular to radius (orbit circle)
            double tangentX = -toCenterY;
            double tangentY = toCenterX;
            if (clockwise) {
                tangentX = -tangentX;
                tangentY = -tangentY;
            }
            
            double tangentMag = Math.sqrt(tangentX * tangentX + tangentY * tangentY);
            if (tangentMag > 0.01) {
                double tangentScale = speed / tangentMag;
                tangentX *= tangentScale;
                tangentY *= tangentScale;
            }
            
            // Combine radial + tangent
            result.x = radialX + tangentX;
            result.y = radialY + tangentY;
            return result;
        };
    }

    // ── Wander (random drift) ──────────────────────────────────────────────
    static MovementGoal wanderAroundTarget(double speed, double minDist, double maxDist) {
        return new MovementGoal() {
            private final Random rand = new Random();
            private final Vector2 wanderTarget = new Vector2(); // Workspace for target
            private final Vector2 result = new Vector2(); // Workspace for result
            private boolean hasTarget = false;

            @Override
            public Vector2 compute(AbstractEntityModel self, UniverseModel world, double dt) {
                Vector2 pos = self.getTransform().getTranslation();
                
                // Check if we need a new target
                if (!hasTarget) {
                    double angle = rand.nextDouble() * 2 * Math.PI;
                    double dist = minDist + rand.nextDouble() * (maxDist - minDist);
                    wanderTarget.x = pos.x + Math.cos(angle) * dist;
                    wanderTarget.y = pos.y + Math.sin(angle) * dist;
                    hasTarget = true;
                } else {
                    // Check if close enough to target (recalculate only when needed)
                    double dx = wanderTarget.x - pos.x;
                    double dy = wanderTarget.y - pos.y;
                    double distSq = dx * dx + dy * dy;
                    if (distSq < 0.04) { // 0.2^2 = 0.04
                        double angle = rand.nextDouble() * 2 * Math.PI;
                        double dist = minDist + rand.nextDouble() * (maxDist - minDist);
                        wanderTarget.x = pos.x + Math.cos(angle) * dist;
                        wanderTarget.y = pos.y + Math.sin(angle) * dist;
                    }
                }
                
                // Compute direction to target
                result.x = wanderTarget.x - pos.x;
                result.y = wanderTarget.y - pos.y;
                
                double magSq = result.x * result.x + result.y * result.y;
                if (magSq < 0.01) return ZERO;
                
                // Single-pass normalization and scaling
                double magnitude = Math.sqrt(magSq);
                double scale = speed / magnitude;
                result.x *= scale;
                result.y *= scale;
                return result;
            }
        };
    }

    // ── Kite (maintain distance from target) ───────────────────────────────
    static MovementGoal kite(Target target, double speed, double preferredRange) {
        // Workspace vector (reused per frame, zero allocation)
        Vector2 workspace = new Vector2();
        return (self, universe, dt) -> {
            Vector2 targetPos = target.getPosition(universe);
            if (targetPos == null) return ZERO;
            
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute toTarget inline
            workspace.x = targetPos.x - selfPos.x;
            workspace.y = targetPos.y - selfPos.y;
            
            double dist = Math.sqrt(workspace.x * workspace.x + workspace.y * workspace.y);
            if (dist < 0.01) return ZERO;
            
            if (dist < preferredRange * 0.8) {
                // Too close → retreat (negative scale)
                double scale = (-speed * 0.6) / dist;
                workspace.x *= scale;
                workspace.y *= scale;
            } else if (dist > preferredRange * 1.2) {
                // Too far → advance
                double scale = (speed * 0.4) / dist;
                workspace.x *= scale;
                workspace.y *= scale;
            } else {
                // In sweet spot → strafe (perpendicular)
                double perpX = -workspace.y;
                double perpY = workspace.x;
                if (Math.random() > 0.5) {
                    perpX = -perpX;
                    perpY = -perpY;
                }
                double perpMag = Math.sqrt(perpX * perpX + perpY * perpY);
                if (perpMag > 0.01) {
                    double scale = (speed * 0.5) / perpMag;
                    workspace.x = perpX * scale;
                    workspace.y = perpY * scale;
                } else {
                    workspace.x = 0;
                    workspace.y = 0;
                }
            }
            return workspace;
        };
    }

    // ── Move to a fixed point ──────────────────────────────────────────────
    static MovementGoal moveTo(Vector2 point, double speed) {
        // Workspace vector (reused per frame, zero allocation)
        Vector2 workspace = new Vector2();
        return (self, world, dt) -> {
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute direction inline
            workspace.x = point.x - selfPos.x;
            workspace.y = point.y - selfPos.y;
            
            double magSq = workspace.x * workspace.x + workspace.y * workspace.y;
            if (magSq < 0.01) return ZERO;
            
            // Single-pass normalization and scaling
            double magnitude = Math.sqrt(magSq);
            double scale = speed / magnitude;
            workspace.x *= scale;
            workspace.y *= scale;
            return workspace;
        };
    }

    // ── Move to a dynamic target (stop when close) ─────────────────────────
    static MovementGoal moveToTarget(Target target, double speed) {
        // Workspace vector (reused per frame, zero allocation)
        Vector2 workspace = new Vector2();
        return (self, world, dt) -> {
            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) return ZERO;
            
            Vector2 selfPos = self.getTransform().getTranslation();
            // Compute direction inline
            workspace.x = targetPos.x - selfPos.x;
            workspace.y = targetPos.y - selfPos.y;
            
            double magSq = workspace.x * workspace.x + workspace.y * workspace.y;
            if (magSq < 0.25) return ZERO; // 0.5^2 = 0.25 (arrival threshold)
            
            // Single-pass normalization and scaling
            double magnitude = Math.sqrt(magSq);
            double scale = speed / magnitude;
            workspace.x *= scale;
            workspace.y *= scale;
            return workspace;
        };
    }

}