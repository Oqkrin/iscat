package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.actions.Action;
import uni.gaben.iscat.universe.entity.brain.actions.ActionCategory;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.*;

public class Brain<T extends AbstractEntityModel> implements IEntityController {

    // Cache the Enum array globally to prevent allocation on every single values() call
    protected static final ActionCategory[] CATEGORIES = ActionCategory.values();

    // ========================================================================
    // FIELDS
    // ========================================================================

    // Core Dependencies
    protected final T entity;
    protected final Shooter<T> shooter;

    // Completely contained zero-GC mathematical vector workspaces
    private final Vector2 steerForce = UU.vector2zero();
    private final Vector2 modifierSteer = new Vector2();

    // Movement & Rotation Goals
    protected final SteeringGoal defaultSteeringGoal;
    protected final RotationGoal defaultRotationGoal;
    protected SteeringGoal currentSteeringGoal;
    protected RotationGoal currentRotationGoal;

    // Action Registries & State Management
    private final Map<String, Action> actionsMap = new HashMap<>();
    private final Map<ActionCategory, List<Action>> actionsByCategory = new EnumMap<>(ActionCategory.class);
    private final Map<ActionCategory, Action> activeActions = new EnumMap<>(ActionCategory.class);
    private final Set<ActionCategory> blockedCategories = new HashSet<>();
    private final List<ActionCategory> finishedCategoriesList = new ArrayList<>(CATEGORIES.length);

    // Modifier Registries & State Management
    private final Map<String, SteeringModifier> modifiersMap = new HashMap<>();
    private final List<SteeringModifier> modifiersOrder = new ArrayList<>();

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public Brain(T entity) {
        this.entity = entity;
        this.shooter = new Shooter<>(entity);

        this.defaultSteeringGoal = SteeringGoal.idle();
        this.currentSteeringGoal = defaultSteeringGoal;

        this.defaultRotationGoal = entity.getMaxAngularVelocity() > 0 ? RotationGoal.movement() : RotationGoal.idle();
        this.currentRotationGoal = defaultRotationGoal;
    }

    // ========================================================================
    // CORE UPDATE LOOP
    // ========================================================================

    @Override
    public void update(UniverseModel universe, double dt) {
        if(entity == null || entity.shouldRemove()) return;
        processActionLifecycles(universe, dt);
        computeAndApplySteering(universe, dt);
        processRotation(universe, dt);
    }

    // ========================================================================
    // UPDATE LOOP HELPERS (Private Lifecycle Pipeline)
    // ========================================================================

    private void processActionLifecycles(UniverseModel universe, double dt) {
        blockedCategories.clear();
        finishedCategoriesList.clear();

        // 1. Refresh blocked categories based on what's active (No Iterator allocation)
        for (int i = 0; i < CATEGORIES.length; i++) {
            Action action = activeActions.get(CATEGORIES[i]);
            if (action != null) {
                blockedCategories.add(action.getCategory());
                blockedCategories.addAll(action.getBlockedCategories());
            }
        }

        // 2. Tick current active actions, gather completed categories
        for (int i = 0; i < CATEGORIES.length; i++) {
            ActionCategory cat = CATEGORIES[i];
            Action action = activeActions.get(cat);
            if (action != null) {
                if (!action.update(this, universe, dt)) {
                    finishedCategoriesList.add(cat);
                }
            }
        }

        // Prune the finished actions
        for (int i = 0; i < finishedCategoriesList.size(); i++) {
            activeActions.remove(finishedCategoriesList.get(i));
        }

        // 3. Attempt to evaluate and wake up higher priority idle actions
        for (int i = 0; i < CATEGORIES.length; i++) {
            ActionCategory cat = CATEGORIES[i];
            if (blockedCategories.contains(cat) || activeActions.containsKey(cat)) {
                continue;
            }
            List<Action> catActions = actionsByCategory.get(cat);
            if (catActions == null || catActions.isEmpty()) {
                continue;
            }
            for (int j = 0; j < catActions.size(); j++) {
                Action action = catActions.get(j);
                if (action.canActivate(entity, universe, dt)) {
                    activeActions.put(cat, action);
                    action.onActivate(this, universe);
                    break;
                }
            }
        }
    }

    /**
     * Integrated Steering Engine: Computes independent forces, sums them,
     * bounds them, and processes mass mechanics smoothly without GC churn.
     */
    private void computeAndApplySteering(UniverseModel universe, double dt) {
        Vector2 desiredVelocity = currentSteeringGoal.computeDesiredVelocity(entity, universe, dt);

        if (desiredVelocity == null || desiredVelocity.isZero()) {
            steerForce.set(0, 0);
        } else {
            steerForce.set(desiredVelocity);
        }

        if (!modifiersOrder.isEmpty()) {
            double maxForce = entity.getMaxForce();
            for (int i = 0; i < modifiersOrder.size(); i++) {
                modifierSteer.set(0, 0); // Isolate the modifier math completely
                modifiersOrder.get(i).computeSteer(entity, universe, maxForce, dt, modifierSteer);
                steerForce.add(modifierSteer); // Sum into total force
            }
        }

        double currentMass = entity.getMass().getMass();
        if (currentMass > 0.0 && currentMass != 1.0) {
            steerForce.divide(currentMass);
        }

        entity.applyForce(steerForce);
    }

    private void processRotation(UniverseModel universe, double dt) {
        double maxAngularVelocity = entity.getMaxAngularVelocity();
        if (maxAngularVelocity <= 0) return;

        double desiredAngle = currentRotationGoal.compute(entity, universe, dt);
        if (Double.isNaN(desiredAngle)) return;

        double currentAngle = entity.getTransform().getRotationAngle();

        double diff = desiredAngle - currentAngle;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;

        double angVel = entity.getAngularVelocity();
        if (Math.abs(diff) < 0.01 && Math.abs(angVel) < 0.1) {
            entity.setAngularVelocity(0);
            entity.getTransform().setRotation(desiredAngle); // snap to avoid drift
            return;
        }

        double kp = maxAngularVelocity * 2.0;
        double kd = maxAngularVelocity * 0.5;

        entity.applyTorque(kp * diff - kd * angVel);
    }

    // ========================================================================
    // ACTION API
    // ========================================================================

    public void addAction(String id, Action action) {
        if (actionsMap.containsKey(id)) {
            throw new IllegalArgumentException("Action with id '" + id + "' already exists");
        }
        actionsMap.put(id, action);
        actionsByCategory.computeIfAbsent(action.getCategory(), k -> new ArrayList<>()).add(action);
    }

    public String addAction(Action action) {
        String id = UUID.randomUUID().toString();
        addAction(id, action);
        return id;
    }

    public boolean removeAction(String id) {
        Action action = actionsMap.remove(id);
        if (action == null) return false;

        List<Action> catList = actionsByCategory.get(action.getCategory());
        if (catList != null) {
            catList.remove(action);
            if (catList.isEmpty()) actionsByCategory.remove(action.getCategory());
        }
        if (activeActions.get(action.getCategory()) == action) {
            activeActions.remove(action.getCategory());
        }
        return true;
    }

    public boolean replaceAction(String id, Action newAction) {
        Action oldAction = actionsMap.get(id);
        if (oldAction == null) return false;

        List<Action> catList = actionsByCategory.get(oldAction.getCategory());
        if (catList != null) {
            int idx = catList.indexOf(oldAction);
            if (idx != -1) catList.set(idx, newAction);
        }
        if (activeActions.get(oldAction.getCategory()) == oldAction) {
            activeActions.put(oldAction.getCategory(), newAction);
        }
        actionsMap.put(id, newAction);
        return true;
    }

    public Action getAction(String id) { return actionsMap.get(id); }

    // ========================================================================
    // MODIFIER API
    // ========================================================================

    public void addModifier(String id, SteeringModifier modifier) {
        if (modifiersMap.containsKey(id)) {
            throw new IllegalArgumentException("Modifier with id '" + id + "' already exists");
        }
        modifiersMap.put(id, modifier);
        modifiersOrder.add(modifier);
    }

    public String addModifier(SteeringModifier modifier) {
        String id = UUID.randomUUID().toString();
        addModifier(id, modifier);
        return id;
    }

    public boolean removeModifier(String id) {
        SteeringModifier mod = modifiersMap.remove(id);
        if (mod == null) return false;
        modifiersOrder.remove(mod);
        return true;
    }

    public boolean replaceModifier(String id, SteeringModifier newModifier) {
        if (!modifiersMap.containsKey(id)) return false;

        SteeringModifier oldMod = modifiersMap.get(id);
        int index = modifiersOrder.indexOf(oldMod);
        if (index != -1) {
            modifiersOrder.set(index, newModifier);
        }
        modifiersMap.put(id, newModifier);
        return true;
    }

    public SteeringModifier getModifier(String id) { return modifiersMap.get(id); }

    // ========================================================================
    // UTILITY & MATHEMATICAL HELPERS
    // ========================================================================

    public double angleToTarget(Vector2 pos) {
        // Optimized to stack-allocated primitives to bypass dyn4j .copy() object generation
        Vector2 selfPos = entity.getTransform().getTranslation();
        return Math.atan2(pos.y - selfPos.y, pos.x - selfPos.x);
    }

    public double angleToPlayer(UniverseModel world) {
        PlayerModel player = world.getPlayer();
        if (player == null) return 0;
        return angleToTarget(player.getTransform().getTranslation());
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public void setSteeringGoal(SteeringGoal goal) { this.currentSteeringGoal = goal; }
    public SteeringGoal getMovementGoal() { return currentSteeringGoal; }
    public SteeringGoal getDefaultGoal() { return defaultSteeringGoal; }

    public void setRotationGoal(RotationGoal goal) { this.currentRotationGoal = goal; }
    public RotationGoal getRotationGoal() { return currentRotationGoal; }
    public RotationGoal getDefaultRotationGoal() { return defaultRotationGoal; }

    public T getEntity() { return entity; }
    public Shooter<T> getShooter() { return shooter; }
}