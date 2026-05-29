package uni.gaben.iscat.universe.lib.behaviurs;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.AvoidanceModifier;
import uni.gaben.iscat.universe.lib.interfaces.controller.IEntityController;

import java.util.ArrayList;
import java.util.List;

public abstract class AiController implements IEntityController {
    protected final AbstractEntityModel entity;
    protected MovementStrategy movementStrategy;
    protected final List<AvoidanceModifier> modifiers = new ArrayList<>();
    protected final List<AttackBehavior> attacks = new ArrayList<>();

    // Steering parameters
    protected final double maxForce;
    protected final double maxVelocity;
    protected final double rotationSpeed;

    public AiController(AbstractEntityModel entity, double maxForce, double maxVelocity, double rotationSpeed) {
        this.entity = entity;
        this.maxForce = maxForce;
        this.maxVelocity = maxVelocity;
        this.rotationSpeed = rotationSpeed;
    }

    public void setMovementStrategy(MovementStrategy strategy) {
        this.movementStrategy = strategy;
    }

    public void addModifier(AvoidanceModifier modifier) {
        modifiers.add(modifier);
    }

    public void addAttack(AttackBehavior attack) {
        attacks.add(attack);
    }

    @Override
    public void update(UniverseModel world, double dt) {
        if (entity.shouldRemove()) return;

        // 1. Tick all attacks (cooldowns)
        for (AttackBehavior a : attacks) a.tick(entity, world, dt);

        // 2. Select and execute highest‑priority attack
        AttackBehavior bestAttack = null;
        double bestPriority = 0;
        for (AttackBehavior a : attacks) {
            double prio = a.getPriority(entity, world);
            if (prio > bestPriority) {
                bestPriority = prio;
                bestAttack = a;
            }
        }
        if (bestAttack != null) bestAttack.execute(entity, world, dt);

        // 3. Compute desired velocity from movement strategy
        Vector2 desired = (movementStrategy != null)
                ? movementStrategy.computeDesiredVelocity(entity, world, dt)
                : new Vector2();

        // 4. Apply all avoidance modifiers in order
        for (AvoidanceModifier mod : modifiers) {
            desired = mod.modify(desired, entity, world, dt);
        }

        // 5. Apply steering force
        applySteering(desired, dt);


        faceDirection(desired.getDirection(), dt);
    }

    private void applySteering(Vector2 desired, double dt) {
        if (desired == null) return;
        Vector2 currentVel = entity.getLinearVelocity();
        Vector2 steering = desired.copy().subtract(currentVel);
        double mag = steering.getMagnitude();
        if (mag > maxForce) steering.multiply(maxForce / mag);
        entity.applyForce(steering);
    }

    private void faceDirection(double targetAngle, double dt) {
        double current = entity.getTransform().getRotationAngle();
        double diff = targetAngle - current;
        while (diff < -Math.PI) diff += 2*Math.PI;
        while (diff > Math.PI) diff -= 2*Math.PI;
        double step = rotationSpeed * dt;
        if (Math.abs(diff) < step) step = Math.abs(diff);
        entity.getTransform().setRotation(current + Math.signum(diff) * step);
    }
}
