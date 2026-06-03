package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.actions.Action;
import uni.gaben.iscat.universe.entity.brain.actions.ActionCategory;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.entity.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.modifiers.MovementModifier;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.*;

public class Brain<T extends AbstractEntityModel> implements IEntityController {
    private final T entity;
    private final Shooter<T> shooter;

    // Modifiers: ordered list (preserves order) + map for lookup
    private final List<MovementModifier> modifiersOrder = new ArrayList<>();
    private final Map<String, MovementModifier> modifiersMap = new HashMap<>();

    // Actions: per‑category list (order = priority) + global map for lookup
    private final Map<ActionCategory, List<Action>> actionsByCategory = new EnumMap<>(ActionCategory.class);
    private final Map<String, Action> actionsMap = new HashMap<>();

    // One active action per category
    private final Map<ActionCategory, Action> active = new EnumMap<>(ActionCategory.class);
    // Categories blocked by running actions
    private final Set<ActionCategory> blockedCategories = new HashSet<>();

    private MovementGoal currentMovementGoal;
    private final MovementGoal defaultMovementGoal;

    private RotationGoal currentRotationGoal;
    private final RotationGoal defaultRotationGoal;

    private final double maxForce, maxVelocity, rotationSpeed;
    private final Vector2 steeringAccumulator = new Vector2();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public Brain(T entity, MovementGoal defaultGoal,
                 double maxForce, double maxVelocity, double rotationSpeed) {
        this.entity = entity;
        this.shooter = new Shooter<>(entity);
        this.defaultMovementGoal = defaultGoal;
        this.currentMovementGoal = defaultGoal;
        this.defaultRotationGoal = RotationGoal.movement();
        this.currentRotationGoal = defaultRotationGoal;
        this.maxForce = maxForce;
        this.maxVelocity = maxVelocity;
        this.rotationSpeed = rotationSpeed;
    }

    // ------------------------------------------------------------------------
    // Adding / removing / retrieving actions
    // ------------------------------------------------------------------------

    /**
     * Adds an action with a unique identifier.
     * @param id     unique identifier (e.g., "shoot", "heal")
     * @param action the action to add
     * @throws IllegalArgumentException if an action with the same id already exists
     */
    public void addAction(String id, Action action) {
        if (actionsMap.containsKey(id)) {
            throw new IllegalArgumentException("Action with id '" + id + "' already exists");
        }
        actionsMap.put(id, action);
        actionsByCategory.computeIfAbsent(action.getCategory(), k -> new ArrayList<>()).add(action);
    }

    /**
     * Adds an action with an auto‑generated UUID.
     * @return the generated ID
     */
    public String addAction(Action action) {
        String id = UUID.randomUUID().toString();
        addAction(id, action);
        return id;
    }

    /**
     * Removes an action by its ID.
     * @param id the action identifier
     * @return true if removed, false if not found
     */
    public boolean removeAction(String id) {
        Action action = actionsMap.remove(id);
        if (action == null) return false;
        List<Action> catList = actionsByCategory.get(action.getCategory());
        if (catList != null) {
            catList.remove(action);
            if (catList.isEmpty()) actionsByCategory.remove(action.getCategory());
        }
        // If this action was currently active, deactivate it
        if (active.get(action.getCategory()) == action) {
            active.remove(action.getCategory());
        }
        return true;
    }

    /**
     * Retrieves an action by its ID for modification.
     */
    public Action getAction(String id) {
        return actionsMap.get(id);
    }

    // ------------------------------------------------------------------------
    // Adding / removing / retrieving modifiers
    // ------------------------------------------------------------------------

    /**
     * Adds a movement modifier with a unique identifier.
     * Modifiers are applied in the order they are added.
     */
    public void addModifier(String id, MovementModifier modifier) {
        if (modifiersMap.containsKey(id)) {
            throw new IllegalArgumentException("Modifier with id '" + id + "' already exists");
        }
        modifiersMap.put(id, modifier);
        modifiersOrder.add(modifier);
    }

    /**
     * Adds a modifier with an auto‑generated UUID.
     * @return the generated ID
     */
    public String addModifier(MovementModifier modifier) {
        String id = UUID.randomUUID().toString();
        addModifier(id, modifier);
        return id;
    }

    /**
     * Removes a modifier by its ID.
     * @return true if removed, false if not found
     */
    public boolean removeModifier(String id) {
        MovementModifier mod = modifiersMap.remove(id);
        if (mod == null) return false;
        modifiersOrder.remove(mod);
        return true;
    }

    /**
     * Retrieves a modifier by its ID for modification.
     */
    public MovementModifier getModifier(String id) {
        return modifiersMap.get(id);
    }

    /**
     * Replaces an existing modifier with a new one (same ID, different object).
     * Useful for dynamically changing parameters (e.g., strength, range).
     */
    public boolean replaceModifier(String id, MovementModifier newModifier) {
        if (!modifiersMap.containsKey(id)) return false;
        MovementModifier oldMod = modifiersMap.get(id);
        int index = modifiersOrder.indexOf(oldMod);
        if (index != -1) {
            modifiersOrder.set(index, newModifier);
        }
        modifiersMap.put(id, newModifier);
        return true;
    }

    /**
     * Replaces an existing action (same ID, different object).
     * Useful for dynamically changing attack patterns or cooldowns.
     */
    public boolean replaceAction(String id, Action newAction) {
        Action oldAction = actionsMap.get(id);
        if (oldAction == null) return false;
        // Remove old from category list
        List<Action> catList = actionsByCategory.get(oldAction.getCategory());
        if (catList != null) {
            int idx = catList.indexOf(oldAction);
            if (idx != -1) catList.set(idx, newAction);
        }
        // Update active mapping if this action was running
        if (active.get(oldAction.getCategory()) == oldAction) {
            active.put(oldAction.getCategory(), newAction);
        }
        actionsMap.put(id, newAction);
        return true;
    }

    // ------------------------------------------------------------------------
    // Movement / rotation goals (can be changed anytime)
    // ------------------------------------------------------------------------
    public void setMovementGoal(MovementGoal goal) { this.currentMovementGoal = goal; }
    public MovementGoal getMovementGoal() { return currentMovementGoal; }
    public MovementGoal getDefaultGoal() { return defaultMovementGoal; }

    public void setRotationGoal(RotationGoal goal) { this.currentRotationGoal = goal; }
    public RotationGoal getRotationGoal() { return currentRotationGoal; }
    public RotationGoal getDefaultRotationGoal() { return defaultRotationGoal; }

    // ------------------------------------------------------------------------
    // Getters for tuning parameters (not final anymore)
    // ------------------------------------------------------------------------
    public double getMaxForce() { return maxForce; }
    public double getMaxVelocity() { return maxVelocity; }
    public double getRotationSpeed() { return rotationSpeed; }

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

    // ------------------------------------------------------------------------
    // Core update loop (unchanged logic, uses the ordered structures)
    // ------------------------------------------------------------------------
    @Override
    public void update(UniverseModel universe, double dt) {
        if (true) return;
        blockedCategories.clear();
        for (Action a : active.values()) {
            if (a == null) continue;
            blockedCategories.add(a.getCategory());
            blockedCategories.addAll(a.getBlockedCategories());
        }

        Set<ActionCategory> finishedCategories = new HashSet<>();
        for (Map.Entry<ActionCategory, Action> entry : active.entrySet()) {
            Action a = entry.getValue();
            if (a == null) continue;
            if (!a.update(this, universe, dt)) {
                finishedCategories.add(entry.getKey());
            }
        }
        finishedCategories.forEach(active::remove);

        // Activate new actions for each category if not blocked and no active action
        for (ActionCategory cat : ActionCategory.values()) {
            if (blockedCategories.contains(cat)) continue;
            if (active.containsKey(cat)) continue;
            List<Action> catActions = actionsByCategory.get(cat);
            if (catActions == null || catActions.isEmpty()) continue;
            for (Action a : catActions) {
                if (a.canActivate(entity, universe, dt)) {
                    active.put(cat, a);
                    a.onActivate(this, universe);
                    break;
                }
            }
        }

        // 2. Initialize the base steering target (e.g., from your MovementGoal)
        Vector2 baseGoal = currentMovementGoal.compute(entity, universe, dt);
        steeringAccumulator.x = baseGoal.x;
        steeringAccumulator.y = baseGoal.y;

        // 3. Prioritized Acceleration Allocation Loop
        double remainingForce = maxForce;

        for (MovementModifier mod : modifiersOrder) {
            // Modifiers must now return vectors scaled ONLY by their relative weight multiplier, NOT maxForce.
            Vector2 forceToAdd = mod.computeForce(entity, universe, maxForce, dt);

            double magnitude = forceToAdd.getMagnitude();
            if (magnitude <= 0.0001) continue;

            // How much of this force can we actually apply?
            double forceToApply = Math.min(magnitude, remainingForce);

            // Add the constrained portion to the accumulator
            double scale = forceToApply / magnitude;
            steeringAccumulator.x += forceToAdd.x * scale;
            steeringAccumulator.y += forceToAdd.y * scale;

            // Deduct from our force budget
            remainingForce -= forceToApply;

            // If the budget is exhausted, stop processing lower-priority modifiers entirely!
            if (remainingForce <= 0.01) {
                break;
            }
        }

        // 4. Apply the aggregated steering
        if (steeringAccumulator.getMagnitudeSquared() > 0) {
            applySteering(steeringAccumulator, dt);
        }

        if (rotationSpeed > 0) {
            Double desiredAngle = currentRotationGoal.compute(entity, universe, dt);
            if (desiredAngle != null) {
                faceDirection(desiredAngle, dt);
            }
        }
    }

    private void applySteering(Vector2 desired, double dt) {
        // Safety check: validate desired velocity
        if (desired == null || Double.isNaN(desired.x) || Double.isNaN(desired.y)) {
            return; // Skip invalid steering
        }
        
        Vector2 currentVel = entity.getLinearVelocity();
        
        // Safety check: validate current velocity
        if (currentVel == null || Double.isNaN(currentVel.x) || Double.isNaN(currentVel.y)) {
            // If current velocity is invalid, just apply desired as force
            entity.applyForce(new Vector2(desired.x, desired.y));
            return;
        }
        
        // Compute steering force: desired - current
        double steeringX = desired.x - currentVel.x;
        double steeringY = desired.y - currentVel.y;
        
        // Safety check: validate steering force
        if (Double.isNaN(steeringX) || Double.isNaN(steeringY)) {
            return; // Skip invalid steering
        }
        
        // Apply the steering force
        entity.applyForce(new Vector2(steeringX, steeringY));
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