package uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * A behavior that handles offensive actions (shooting, slamming, charging).
 * <p>
 * Attack behaviors run on a <em>separate track</em> from movement — the
 * highest-priority attack executes independently of whichever movement
 * behavior is active. This means a boss can orbit the player AND fire
 * simultaneously without conflict.
 * </p>
 *
 * <h3>Movement during attacks</h3>
 * <p>If an attack needs to seize movement control (e.g. a plunge dash),
 * implement {@link MovementBehavior} on the same class and return
 * {@link uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest#locked}
 * so the steering controller suppresses other movement.</p>
 *
 * <h3>Priority contract</h3>
 * <ul>
 *   <li>{@code > 0} — eligible (highest wins)</li>
 *   <li>{@code <= 0} — inactive</li>
 * </ul>
 */
public interface AttackBehavior {

    double getPriority(AbstractEntityModel npc, UniverseModel universe);

    void execute(AbstractEntityModel npc, UniverseModel universe, double dt);

    /** Called every frame for timer/cooldown updates. */
    default void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {}
}
