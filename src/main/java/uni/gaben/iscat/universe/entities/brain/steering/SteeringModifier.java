package uni.gaben.iscat.universe.entities.brain.steering;

import javafx.beans.property.DoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;

import java.util.List;

@FunctionalInterface
public interface SteeringModifier {

    void computeSteer(AbstractPhysicalEntityModel self, UniverseModel universe, double maxForce, double dt, Vector2 outForce);

    static SteeringModifier separation(Target neighborhood, double separationRadius, DoubleProperty weight) {
        Vector2 toNeighbor = UU.vector2zero();
        Vector2 forwardDir   = UU.vector2zero();

        return (self, universe, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(universe);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();

            // 1. Accumulate separation forces from neighbors
            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                toNeighbor.set(selfPos).subtract(neighbor.getTransform().getTranslation());
                double distSq = toNeighbor.getMagnitudeSquared();

                if (distSq > 0.0001 && distSq < (separationRadius * separationRadius)) {
                    double dist = Math.sqrt(distSq);
                    double strength = 1.0 - (dist / separationRadius);

                    toNeighbor.normalize();               // normalise in place
                    toNeighbor.multiply(maxForce * strength);
                    outForce.add(toNeighbor);
                }
            }

            if (outForce.isZero()) return;

            // 2. Remove any backward component relative to self’s velocity
            outForce.normalize();                         // get unit direction
            Vector2 selfVel = self.getLinearVelocity();
            if (!selfVel.isZero()) {
                forwardDir.set(selfVel).normalize();      // own forward direction
                double dot = outForce.dot(forwardDir);
                if (dot < 0) {
                    // Subtract the backward projection manually
                    outForce.x -= forwardDir.x * dot;
                    outForce.y -= forwardDir.y * dot;
                    if (outForce.isZero()) {
                        // Fallback: pure lateral (left‑hand perpendicular)
                        outForce.set(-forwardDir.y, forwardDir.x);
                    } else {
                        outForce.normalize();
                    }
                }
            }

            // 3. Apply final weight
            outForce.multiply(maxForce * weight.get());
        };
    }

    static SteeringModifier alignment(Target neighborhood, DoubleProperty weight) {
        Vector2 avgVelocity = UU.vector2zero();

        return (self, universe, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            avgVelocity.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(universe);
            if (neighbors == null || neighbors.isEmpty()) return;

            int count = 0;
            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                avgVelocity.add(neighbor.getLinearVelocity());
                count++;
            }

            if (count > 0) {
                avgVelocity.divide(count);
                if (!avgVelocity.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    avgVelocity.setMagnitude(maxForce);
                }
                outForce.set(avgVelocity).subtract(self.getLinearVelocity());
                if (!outForce.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    outForce.setMagnitude(maxForce * weight.get());
                }
            }
        };
    }

    static SteeringModifier cohesion(Target neighborhood, DoubleProperty weight) {
        Vector2 centerOfMass = UU.vector2zero();

        return (self, universe, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            centerOfMass.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(universe);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                centerOfMass.add(neighbor.getTransform().getTranslation());
                count++;
            }

            if (count > 0) {
                centerOfMass.divide(count);
                Vector2 desired = centerOfMass.subtract(selfPos);
                if (!desired.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    desired.setMagnitude(maxForce);

                    outForce.set(desired).subtract(self.getLinearVelocity());
                    if (!outForce.isZero()) {
                        // CORRETTO: Separato normalize() da multiply()
                        outForce.setMagnitude(maxForce * weight.get());
                    }
                }
            }
        };
    }

    static SteeringModifier collisionAvoidance(Target threats, double maxPredictionTime,
                                               double avoidRadius, DoubleProperty weight) {
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();
        // Additional workspace for the entity's forward direction
        Vector2 forwardDir = UU.vector2zero();

        return (self, universe, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> entities = threats.getEntities(universe);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            AbstractPhysicalEntityModel mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            for (int i = 0; i < entities.size(); i++) {
                AbstractPhysicalEntityModel threat = entities.get(i);
                if (threat == self || threat.shouldRemove()) continue;

                dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
                dv.set(threat.getLinearVelocity()).subtract(selfVel);

                double dvSq = dv.getMagnitudeSquared();
                if (dvSq < 0.0001) continue;

                double t = -dp.dot(dv) / dvSq;
                if (t > 0 && t < maxPredictionTime) {
                    double cx = dp.x + (dv.x * t);
                    double cy = dp.y + (dv.y * t);
                    if ((cx * cx + cy * cy) < (avoidRadius * avoidRadius)) {
                        if (t < shortestTime) {
                            shortestTime = t;
                            mostImminent = threat;
                        }
                    }
                }
            }

            // ---- avoidance force calculation ----
            if (mostImminent != null) {
                Vector2 threatVel = mostImminent.getLinearVelocity();

                if (!threatVel.isZero()) {
                    // 1. bullet direction → store in dp, normalise in place
                    dp.set(threatVel).normalize();

                    // 2. lateral evasion direction → set dp to its perpendicular
                    double x = dp.x;         // keep values before overwriting
                    double y = dp.y;
                    dp.set(-y, x);           // 90° counter‑clockwise

                    // 3. Choose the side that moves away from the bullet (use dv as 'toSelf')
                    dv.set(selfPos).subtract(mostImminent.getTransform().getTranslation());
                    if (dp.dot(dv) < 0) {
                        dp.multiply(-1);     // flip direction
                    }

                    // 4. Never allow a backward component relative to self's velocity
                    forwardDir.set(selfVel);
                    if (!forwardDir.isZero()) {
                        forwardDir.normalize();               // normalise in place
                        double dot = dp.dot(forwardDir);
                        if (dot < 0) {
                            // Remove the backward projection – manually, no copy
                            dp.x -= forwardDir.x * dot;
                            dp.y -= forwardDir.y * dot;

                            // Re‑normalise, fallback to a pure perpendicular if zero
                            if (dp.isZero()) {
                                dp.set(-forwardDir.y, forwardDir.x);
                            } else {
                                dp.normalize();
                            }
                        }
                    }

                    outForce.set(dp);
                } else {
                    // Stationary threat – use 'toSelf' direction (dv already holds that vector)
                    dv.set(selfPos).subtract(mostImminent.getTransform().getTranslation());
                    if (!dv.isZero()) dv.normalize();
                    outForce.set(dv);
                }

                // Scale by urgency and weight
                double urgency = 1.0 - (shortestTime / maxPredictionTime);
                urgency = Math.clamp(urgency, 0.1, 1.0);

                if (!outForce.isZero()) {
                    outForce.multiply(maxForce * weight.get() * urgency);
                }
            }
        };
    }

    /**
     * Pushes an entity back into the camera’s viewport when it gets too close to an edge.
     *
     * @param marginPx  how many screen pixels the entity must always stay away from the edge.
     * @param weight    overall strength multiplier.
     */
    /**
     * Pushes an entity away from the viewport edges when it gets too close.
     * Uses direct edge normals and dampens outward velocity – no camera‑centre steering.
     *
     * @param marginPx  desired clearance from the screen edge (pixels)
     * @param weight    overall strength multiplier
     */
    static SteeringModifier boundaryContainment(double marginPx, DoubleProperty weight) {
        Vector2 outwardVel = UU.vector2zero();   // reusable

        return (self, universe, maxForce, dt, outForce) -> {
            outForce.set(0, 0);

            CameraModel camera = universe.getCamera();
            if (camera == null) return;

            // 1. Viewport rectangle in world units
            double zoom = camera.getZoom();
            double halfVW = (camera.getScreenWidth()  / 2.0) / zoom;
            double halfVH = (camera.getScreenHeight() / 2.0) / zoom;
            double minX = camera.getX() - halfVW;
            double maxX = camera.getX() + halfVW;
            double minY = camera.getY() - halfVH;
            double maxY = camera.getY() + halfVH;

            double halfW = UU.pxToM(self.getWidthPx())  * 0.5;
            double halfH = UU.pxToM(self.getHeightPx()) * 0.5;
            double marginWorld = UU.pxToM(marginPx);

            Vector2 pos = self.getTransform().getTranslation();
            Vector2 vel = self.getLinearVelocity();

            double forceX = 0.0, forceY = 0.0;

            // ---- Left / Right ----
            double leftPen  = (minX + marginWorld) - (pos.x - halfW);   // positive if too far left
            double rightPen = (pos.x + halfW) - (maxX - marginWorld);   // positive if too far right

            if (leftPen > 0) {
                // Push right (positive X) proportional to penetration, plus damp leftward velocity
                forceX += leftPen * 4.0;                          // stiffness (tuneable)
                forceX -= Math.min(vel.x, 0) * 2.0;               // damp outward speed (if moving left)
            } else if (rightPen > 0) {
                // Push left (negative X)
                forceX -= rightPen * 4.0;
                forceX -= Math.max(vel.x, 0) * 2.0;               // damp outward speed (if moving right)
            }

            // ---- Top / Bottom ----
            double topPen    = (pos.y + halfH) - (maxY - marginWorld);   // positive if too far up
            double bottomPen = (minY + marginWorld) - (pos.y - halfH);   // positive if too far down

            if (bottomPen > 0) {
                // Push down (negative Y) – wait, world Y is up? Assuming +Y is up.
                // If your world uses +Y = up, then "down" is -Y.
                // The renderer's minY is top, maxY is bottom? In JavaFX, Y increases downward usually.
                // We'll stick with the same convention as before: minY = top edge? Need consistency.
                // In your renderer, minY = camera.getY() - halfVH (top edge), maxY = camera.getY() + halfVH (bottom edge).
                // So +Y is down? Usually yes. We'll adjust forces accordingly.
                // bottomPen positive means entity is too close to bottom (maxY), so push up (negative Y).
                forceY -= bottomPen * 4.0;
                // Damp downward velocity (if moving down, vel.y > 0)
                forceY -= Math.max(vel.y, 0) * 2.0;
            } else if (topPen > 0) {
                // Entity too close to top (minY), push down (positive Y)
                forceY += topPen * 4.0;
                // Damp upward velocity (if moving up, vel.y < 0)
                forceY -= Math.min(vel.y, 0) * 2.0;
            }

            if (Math.abs(forceX) < 0.0001 && Math.abs(forceY) < 0.0001) return;

            // Combine and scale by maxForce * weight
            outForce.set(forceX, forceY);
            double mag = outForce.getMagnitude();
            if (mag > maxForce * weight.get()) {
                outForce.setMagnitude(maxForce * weight.get());
            }
            // Optional: apply the weight factor (you can bake it into the stiffness/damping instead)
            outForce.multiply(weight.get());
        };
    }

}