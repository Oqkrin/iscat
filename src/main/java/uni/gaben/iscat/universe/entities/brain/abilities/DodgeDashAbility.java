package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DodgeDashAbility extends Ability {

    private final Cooldown dashCooldown;
    private final Cooldown dashDuration;
    private final double maxPredictionTime;
    private final double avoidRadius;
    private final double dashImpulse;
    private final Target threatSupplier;

    private final Random rand = new Random();
    private Vector2 dashDirection = new Vector2();

    public DodgeDashAbility(AbstractPhysicalEntityModel entity, double cooldownSec, double durationSec, double maxPredictionTime,
                            double avoidRadius, double dashImpulse, Target threatSupplier) {
        super("dodgeDash", AbilityCategory.MOVEMENT, Collections.emptySet());
        this.dashCooldown = new Cooldown(durationSec+cooldownSec);
        this.dashDuration = new Cooldown(durationSec);
        this.maxPredictionTime = maxPredictionTime;
        this.avoidRadius = avoidRadius;
        this.dashImpulse = dashImpulse;
        this.threatSupplier = threatSupplier;
    }

    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        if (dashCooldown.isCoolingDown()) return false;
        if (dashDuration.isCoolingDown()) return false; // already dashing

        List<AbstractPhysicalEntityModel> threats = threatSupplier.getEntities(world);
        if (threats == null || threats.isEmpty()) return false;

        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 selfVel = self.getLinearVelocity();
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

        double shortestTime = Double.MAX_VALUE;
        AbstractPhysicalEntityModel mostImminent = null;

        for (AbstractPhysicalEntityModel threat : threats) {
            if (threat == self || threat.shouldRemove()) continue;

            dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
            dv.set(threat.getLinearVelocity()).subtract(selfVel);

            double dvSq = dv.getMagnitudeSquared();
            if (dvSq < 0.0001) continue;

            double t = -dp.dot(dv) / dvSq;

            if (t > 0 && t < maxPredictionTime) {
                double cx = dp.x + (dv.x * t);
                double cy = dp.y + (dv.y * t);
                if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                    if (t < shortestTime) {
                        shortestTime = t;
                        mostImminent = threat;
                    }
                }
            }
        }

        return mostImminent != null;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractPhysicalEntityModel self = brain.getEntity();
        self.setTemporaryTerminalVelocity(self.getTerminalVelocity()*3);
        self.setDashLinearDamping(0);

        // Find the most imminent threat again (or re-use from canActivate, but we recompute for safety)
        List<AbstractPhysicalEntityModel> threats = threatSupplier.getEntities(world);
        if (threats == null || threats.isEmpty()) return;

        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 selfVel = self.getLinearVelocity();
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

        double shortestTime = Double.MAX_VALUE;
        AbstractPhysicalEntityModel mostImminent = null;

        for (AbstractPhysicalEntityModel threat : threats) {
            if (threat == self || threat.shouldRemove()) continue;

            dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
            dv.set(threat.getLinearVelocity()).subtract(selfVel);

            double dvSq = dv.getMagnitudeSquared();
            if (dvSq < 0.0001) continue;

            double t = -dp.dot(dv) / dvSq;
            if (t > 0 && t < maxPredictionTime) {
                double cx = dp.x + (dv.x * t);
                double cy = dp.y + (dv.y * t);
                if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                    if (t < shortestTime) {
                        shortestTime = t;
                        mostImminent = threat;
                    }
                }
            }
        }

        if (mostImminent == null) return;

        // Compute future positions
        Vector2 threatFuture = UU.vector2zero();
        threatFuture.set(mostImminent.getTransform().getTranslation());
        threatFuture.add(mostImminent.getLinearVelocity().x * shortestTime, mostImminent.getLinearVelocity().y * shortestTime);

        Vector2 myFuture = UU.vector2zero();
        myFuture.set(selfPos);
        myFuture.add(selfVel.x * shortestTime, selfVel.y * shortestTime);

        // Direction away from threat
        dashDirection.set(myFuture).subtract(threatFuture).normalize();

        // Add some randomness to avoid predictable patterns
        double randomAngle = (rand.nextDouble() - 0.5) * Math.toRadians(30);
        dashDirection.rotate(randomAngle);

        // Apply impulse
        double mass = self.getMass().getMass();
        self.applyImpulse(dashDirection.multiply(dashImpulse * mass));

        dashDuration.start();
        dashCooldown.start();
    }

    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        if (dashDuration.isReady()) {
           brain.getEntity().restoreTerminalVelocity();
           brain.getEntity().restoreLinearDamping();
        }
        return dashDuration.isCoolingDown(); // true while dashing
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {
        if(dashCooldown.isCoolingDown()) {
            dashCooldown.update(dt);
        }
        if(dashDuration.isCoolingDown()) {
            dashDuration.update(dt);
        }
    }
}