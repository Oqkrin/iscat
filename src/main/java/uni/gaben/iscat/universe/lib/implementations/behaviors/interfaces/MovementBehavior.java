package uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * A behavior that controls where/how an entity moves.
 * <p>
 * Only the highest-priority {@code MovementBehavior} runs per frame.
 * It does <em>not</em> apply forces directly; instead it returns a
 * {@link MovementRequest} that {@code SteeringController} translates into
 * exactly one physics call.
 * </p>
 *
 * <h3>Priority contract</h3>
 * <ul>
 *   <li>{@code > 0} — eligible to run (highest wins)</li>
 *   <li>{@code <= 0} — inactive this frame</li>
 * </ul>
 */
public interface MovementBehavior {

    /**
     * Returns the priority of this behavior given the current world state.
     * Higher value = more likely to be selected.
     */
    double getPriority(AbstractEntityModel npc, UniverseModel universe);

    /**
     * Computes the desired movement for this frame.
     * <strong>Must not call {@code applyForce} or modify physics here.</strong>
     */
    MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt);

    /** Called every frame regardless of priority, for updating timers/cooldowns. */
    default void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {}
}
