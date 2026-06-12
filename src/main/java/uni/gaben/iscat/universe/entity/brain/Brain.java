package uni.gaben.iscat.universe.entity.brain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.abilities.Ability;
import uni.gaben.iscat.universe.entity.brain.abilities.AbilityCategory;
import uni.gaben.iscat.universe.entity.modules.MovementModule;
import uni.gaben.iscat.universe.entity.modules.SpriteModule;
import uni.gaben.iscat.universe.entity.projectiles.shooters.Shooter;

import java.util.*;

public class Brain<T extends GameEntity> implements IEntityController {

    // Cache the Enum array globally to prevent allocation on every single values() call
    protected static final AbilityCategory[] CATEGORIES = AbilityCategory.values();

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

    // Weight property for the primary SteeringGoal
    public final DoubleProperty goalWeight = new SimpleDoubleProperty(1.0);

    // Ability Registries & State Management
    private final Map<String, Ability> actionsMap = new HashMap<>();
    private final Map<AbilityCategory, List<Ability>> actionsByCategory = new EnumMap<>(AbilityCategory.class);
    private final Map<AbilityCategory, Ability> activeActions = new EnumMap<>(AbilityCategory.class);
    private final Set<AbilityCategory> blockedCategories = new HashSet<>();
    private final List<AbilityCategory> finishedCategoriesList = new ArrayList<>(CATEGORIES.length);

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

        this.defaultRotationGoal = entity.hasModule(MovementModule.class) && entity.getModule(MovementModule.class).getMaxAngularVelocity() > 0 ? RotationGoal.movement() : RotationGoal.idle();
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
            Ability ability = activeActions.get(CATEGORIES[i]);
            if (ability != null) {
                blockedCategories.add(ability.getCategory());
                blockedCategories.addAll(ability.getBlockedCategories());
            }
        }

        // 2. Tick current active actions, gather completed categories
        for (int i = 0; i < CATEGORIES.length; i++) {
            AbilityCategory cat = CATEGORIES[i];
            Ability ability = activeActions.get(cat);
            if (ability != null) {
                if (!ability.update(this, universe, dt)) {
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
            AbilityCategory cat = CATEGORIES[i];
            if (blockedCategories.contains(cat) || activeActions.containsKey(cat)) {
                continue;
            }
            List<Ability> catAbilities = actionsByCategory.get(cat);
            if (catAbilities == null || catAbilities.isEmpty()) {
                continue;
            }
            for (int j = 0; j < catAbilities.size(); j++) {
                Ability ability = catAbilities.get(j);
                if (ability.canActivate(entity, universe, dt)) {
                    activeActions.put(cat, ability);
                    ability.onActivate(this, universe);
                    break;
                }
            }
        }
    }

    /**
     * Weighted Linear Summation Steering:
     * Calculates all forces, scales them strictly by (maxForce * weight), and sums them directly.
     */
    private void computeAndApplySteering(UniverseModel universe, double dt) {
        // 1. Get primary goal force (It returns a Force normalized to maxForce)
        Vector2 primaryForce = currentSteeringGoal.computeDesiredVelocity(entity, universe, dt);

        if (primaryForce == null || primaryForce.isZero()) {
            steerForce.set(0, 0);
        } else {
            // Scale primary force by its configurable weight
            steerForce.set(primaryForce).multiply(goalWeight.get());
        }

        double maxForce = entity.hasModule(MovementModule.class) ? entity.getRecord().movement().maxForce() : 0;

        // 2. Sum Modifiers directly (No averaging, no strict budget clamping)
        if (!modifiersOrder.isEmpty()) {
            for (int i = 0; i < modifiersOrder.size(); i++) {
                modifierSteer.set(0, 0);
                // Modifiers scale themselves by (maxForce * their own weight) internally
                modifiersOrder.get(i).computeSteer(entity, universe, maxForce, dt, modifierSteer);
                steerForce.add(modifierSteer);
            }
        }

        // 3. Apply physical mass
        double currentMass = entity.getMass().getMass();
        if (currentMass > 0.0 && currentMass != 1.0) {
            steerForce.divide(currentMass);
        }

        // 4. Apply physical force (Velocity constraints handled by UniverseController)
        entity.applyForce(steerForce);
    }

    private void processRotation(UniverseModel universe, double dt) {
        double maxAngularVelocity = entity.hasModule(MovementModule.class) ? entity.getRecord().movement().maxAngularVelocity() : 0;
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

    public void addAction(String id, Ability ability) {
        if (actionsMap.containsKey(id)) {
            throw new IllegalArgumentException("Ability with id '" + id + "' already exists");
        }
        actionsMap.put(id, ability);
        actionsByCategory.computeIfAbsent(ability.getCategory(), k -> new ArrayList<>()).add(ability);
    }

    public String addAction(Ability ability) {
        String id = UUID.randomUUID().toString();
        addAction(id, ability);
        return id;
    }

    public boolean removeAction(String id) {
        Ability ability = actionsMap.remove(id);
        if (ability == null) return false;

        List<Ability> catList = actionsByCategory.get(ability.getCategory());
        if (catList != null) {
            catList.remove(ability);
            if (catList.isEmpty()) actionsByCategory.remove(ability.getCategory());
        }
        if (activeActions.get(ability.getCategory()) == ability) {
            activeActions.remove(ability.getCategory());
        }
        return true;
    }

    public boolean replaceAction(String id, Ability newAbility) {
        Ability oldAbility = actionsMap.get(id);
        if (oldAbility == null) return false;

        List<Ability> catList = actionsByCategory.get(oldAbility.getCategory());
        if (catList != null) {
            int idx = catList.indexOf(oldAbility);
            if (idx != -1) catList.set(idx, newAbility);
        }
        if (activeActions.get(oldAbility.getCategory()) == oldAbility) {
            activeActions.put(oldAbility.getCategory(), newAbility);
        }
        actionsMap.put(id, newAbility);
        return true;
    }

    public Ability getAction(String id) { return actionsMap.get(id); }

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
        GameEntity player = world.getPlayer();
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
