package uni.gaben.iscat.universe.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.modifiers.MovementModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Shooter;

import java.util.*;

public class Brain<T extends AbstractEntityModel> {
    private final T entity;
    private final Shooter<T> shooter;
    private final List<MovementModifier> modifiers = new ArrayList<>();

    private final Map<ActionCategory, List<Action>> actions = new HashMap<>();
    // One active action per category (null if none)
    private final Map<ActionCategory, Action> active = new HashMap<>();
    // Categories blocked by running actions (includes own category and explicit blocks)
    private final Set<ActionCategory> blockedCategories = new HashSet<>();

    private MovementGoal currentMovementGoal;
    private final MovementGoal defaultMovementGoal;
    private final double maxForce, maxVelocity, rotationSpeed;

    public Brain(T entity, MovementGoal defaultGoal,
                 double maxForce, double maxVelocity, double rotationSpeed) {
        this.entity = entity;
        this.shooter = new Shooter<>(entity);
        this.defaultMovementGoal = defaultGoal;
        this.currentMovementGoal = defaultGoal;
        this.maxForce = maxForce;
        this.maxVelocity = maxVelocity;
        this.rotationSpeed = rotationSpeed;
    }

    public void addAction(Action action) {
        actions.computeIfAbsent(action.getCategory(), k -> new ArrayList<>())
                .add(action);
    }

    public void update(UniverseModel world, double dt) {
        // 1. Determine blocked categories from currently active actions
        blockedCategories.clear();
        for (Action a : active.values()) {
            if (a == null) continue;
            blockedCategories.add(a.getCategory());
            blockedCategories.addAll(a.getBlockedCategories());
        }

        // 2. Tick every active action once, collect finished ones
        Set<ActionCategory> finishedCategories = new HashSet<>();
        for (Map.Entry<ActionCategory, Action> entry : active.entrySet()) {
            Action a = entry.getValue();
            if (a == null) continue;
            if (!a.update(this, world, dt)) {
                finishedCategories.add(entry.getKey());
            }
        }
        // Remove finished actions
        finishedCategories.forEach(active::remove);

        // 3. For each category, if it's empty AND not blocked, try to activate a new action
        for (ActionCategory cat : ActionCategory.values()) {
            if (blockedCategories.contains(cat)) continue;
            if (active.containsKey(cat)) continue;   // already has a running action
            List<Action> catActions = actions.get(cat);
            if (catActions == null || catActions.isEmpty()) continue;

            // Actions are added in priority order – first that canActivate wins
            for (Action a : catActions) {
                if (a.canActivate(entity, world, dt)) {
                    active.put(cat, a);
                    a.onActivate(this, world);
                    break;
                }
            }
        }

        // 4. Compute movement (even if no MOVEMENT action, the default goal stays)
        Vector2 desired = currentMovementGoal.compute(entity, world, dt);
        for (MovementModifier mod : modifiers) {
            desired = mod.modify(desired, entity, world, dt);
        }
        applySteering(desired, dt);
        if (rotationSpeed > 0 && desired.getMagnitudeSquared() > 0.01) {
            faceDirection(desired.getDirection(), dt);
        }
    }

    public void setMovementGoal(MovementGoal goal) { this.currentMovementGoal = goal; }
    public MovementGoal getDefaultGoal() { return defaultMovementGoal; }
    public T getEntity() { return entity; }
    public Shooter<T> getShooter() { return shooter; }

    public double angleToTarget(Vector2 pos) {
        return pos.copy()
                .subtract(entity.getTransform().getTranslation())
                .getDirection();
    }

    public double angleToPlayer(UniverseModel world) {
        PlayerModel player = world.getPlayer();
        if (player == null) return 0;
        return angleToTarget(player.getTransform().getTranslation());
    }

    private void applySteering(Vector2 desired, double dt) {
        Vector2 currentVel = entity.getLinearVelocity();
        Vector2 steering = desired.copy().subtract(currentVel);
        double mag = steering.getMagnitude();
        if (mag > maxForce) {
            steering.multiply(maxForce / mag);
        }
        entity.applyForce(steering);
    }

    private void faceDirection(double targetAngle, double dt) {
        double current = entity.getTransform().getRotationAngle();
        double diff = targetAngle - current;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        double step = rotationSpeed * dt;
        if (Math.abs(diff) < step) step = Math.abs(diff);
        entity.getTransform().setRotation(current + Math.signum(diff) * step);
    }
}