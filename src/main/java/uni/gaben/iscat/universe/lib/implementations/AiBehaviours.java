package uni.gaben.iscat.universe.lib.implementations;

import java.util.ArrayList;
import java.util.List;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.SteeringController;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiController;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * Orchestrates NPC AI using three independent behavior tracks that never
 * interfere with each other:
 *
 * <ol>
 *   <li><b>Movement track</b> — highest-priority {@link MovementBehavior} wins.
 *       It returns a {@link MovementRequest}; the {@link SteeringController}
 *       applies it as a single physics call. No two movement behaviors ever
 *       fight over forces.</li>
 *   <li><b>Attack track</b> — highest-priority {@link AttackBehavior} wins.
 *       Runs fully independently; an NPC can orbit <em>and</em> shoot at the
 *       same time.</li>
 *   <li><b>Passive track</b> — all {@link PassiveBehavior}s run every frame,
 *       additively (separation, healing, rotation, etc.).</li>
 * </ol>
 *
 * <h3>Attack behaviors that seize movement (e.g. PlungeAttack)</h3>
 * <p>If an attack needs to suppress normal movement, implement both
 * {@link AttackBehavior} and {@link MovementBehavior} on the same class and
 * return {@link MovementRequest#locked} when active. The orchestrator will
 * detect the lock and skip the normal movement selection.</p>
 *
 * @param <T> The concrete entity type controlled by this AI.
 */
public class AiBehaviours<T extends AbstractEntityModel> implements AiController {

    protected final T aiEntity;

    private final List<MovementBehavior> movementBehaviors = new ArrayList<>();
    private final List<AttackBehavior>   attackBehaviors   = new ArrayList<>();
    private final List<PassiveBehavior>  passiveBehaviors  = new ArrayList<>();

    private final SteeringController steering;

    // ─────────────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * @param aiEntity  The entity this AI controls.
     * @param maxForce  Maximum steering force applied per frame.
     * @param maxVelocity Maximum linear speed allowed.
     * @param rotationSpeed Rotation speed in radians per second.
     */
    public AiBehaviours(T aiEntity, double maxForce, double maxVelocity, double rotationSpeed) {
        this.aiEntity  = aiEntity;
        this.steering  = new SteeringController(maxForce, maxVelocity, rotationSpeed);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Behavior registration
    // ─────────────────────────────────────────────────────────────────────────

    public AiBehaviours<T> addMovement(MovementBehavior b) {
        if (!movementBehaviors.contains(b)) movementBehaviors.add(b);
        return this;
    }

    public AiBehaviours<T> addAttack(AttackBehavior b) {
        if (!attackBehaviors.contains(b)) attackBehaviors.add(b);
        return this;
    }

    public AiBehaviours<T> addPassive(PassiveBehavior b) {
        if (!passiveBehaviors.contains(b)) passiveBehaviors.add(b);
        return this;
    }

    /** Convenience: if a behavior implements multiple interfaces, register it on all tracks. */
    public AiBehaviours<T> add(Object behavior) {
        if (behavior instanceof MovementBehavior m) addMovement(m);
        if (behavior instanceof AttackBehavior   a) addAttack(a);
        if (behavior instanceof PassiveBehavior  p) addPassive(p);
        return this;
    }

    public void removeMovement(MovementBehavior b) { movementBehaviors.remove(b); }
    public void removeAttack(AttackBehavior   b) { attackBehaviors.remove(b);   }
    public void removePassive(PassiveBehavior  b) { passiveBehaviors.remove(b);  }

    // ─────────────────────────────────────────────────────────────────────────
    // Main update loop
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void aiUpdate(UniverseModel universe, double dt) {
        if (aiEntity.shouldRemove()) return;

        // ── 1. Tick every behavior (timers, cooldowns) ──────────────────────
        for (MovementBehavior b : movementBehaviors) b.tick(aiEntity, universe, dt);
        for (AttackBehavior   b : attackBehaviors)   b.tick(aiEntity, universe, dt);
        for (PassiveBehavior  b : passiveBehaviors)  b.tick(aiEntity, universe, dt);

        // ── 2. Attack track ─────────────────────────────────────────────────
        // Run the highest-priority attack (if any is ready).
        AttackBehavior activeAttack = selectHighestPriority(attackBehaviors, aiEntity, universe);
        if (activeAttack != null) {
            activeAttack.execute(aiEntity, universe, dt);
        }

        // ── 3. Movement track ────────────────────────────────────────────────
        // Check if the active attack has locked movement (e.g. plunge dash).
        MovementRequest moveRequest = null;

        if (activeAttack instanceof MovementBehavior attackMover) {
            // The attack itself is driving movement (e.g. PlungeAttackBehavior).
            MovementRequest attackMove = attackMover.computeRequest(aiEntity, universe, dt);
            if (attackMove != null && attackMove.lockMovement()) {
                moveRequest = attackMove; // attack owns movement this frame
            }
        }

        if (moveRequest == null) {
            // Normal path: pick the highest-priority movement behavior.
            MovementBehavior activeMover = selectHighestPriority(movementBehaviors, aiEntity, universe);
            if (activeMover != null) {
                moveRequest = activeMover.computeRequest(aiEntity, universe, dt);
            }
        }

        steering.apply(aiEntity, moveRequest, dt);

        // ── 4. Passive track — always runs, fully additive ──────────────────
        for (PassiveBehavior b : passiveBehaviors) {
            b.execute(aiEntity, universe, dt);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Finds the behavior with the highest priority > 0, or null if none qualify. */
    private <B> B selectHighestPriority(List<B> behaviors, AbstractEntityModel npc, UniverseModel universe) {
        B   best        = null;
        double bestPrio = 0.0; // must beat 0 to run

        for (B b : behaviors) {
            double prio = getPriority(b, npc, universe);
            if (prio > bestPrio) {
                bestPrio = prio;
                best     = b;
            }
        }
        return best;
    }

    private double getPriority(Object b, AbstractEntityModel npc, UniverseModel universe) {
        if (b instanceof MovementBehavior m) return m.getPriority(npc, universe);
        if (b instanceof AttackBehavior   a) return a.getPriority(npc, universe);
        return 0.0;
    }
}
