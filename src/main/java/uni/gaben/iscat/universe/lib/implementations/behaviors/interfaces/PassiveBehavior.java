package uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * A behavior that runs every frame unconditionally, layered on top of whatever
 * movement and attack behaviors are active.
 * <p>
 * Passive behaviors are additive and never suppress each other. They are
 * appropriate for forces that should always be present: crowd separation,
 * idle rotation, area healing, etc.
 * </p>
 * <p>
 * Passive behaviors <em>may</em> apply forces directly, but should keep them
 * small relative to steering forces so they don't meaningfully interfere with
 * movement intent. If a passive needs to temporarily take over movement
 * (e.g. emergency dodge), implement {@link MovementBehavior} instead and give
 * it a high conditional priority.
 * </p>
 */
public interface PassiveBehavior {

    void execute(AbstractEntityModel npc, UniverseModel universe, double dt);

    default void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {}
}
