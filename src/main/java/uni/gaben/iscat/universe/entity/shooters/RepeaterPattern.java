package uni.gaben.iscat.universe.entity.shooters;

import uni.gaben.iscat.universe.entity.brain.abilities.shoot.RandomizedShootAbility;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;

import java.util.function.Consumer;

/**
 * Fires the inner pattern {@code times} times, with {@code intervalSeconds} between each burst.
 * <p>
 * The first shot fires immediately. Remaining shots are tracked via game-loop delta time
 * through {@link uni.gaben.iscat.universe.UniverseController.(double)} so that all physics interactions stay on the main thread.
 * <p>
 * When used inside {@link RandomizedShootAbility},
 * the burst state is managed by the action itself — {@link #times ()} and {@link #inner ()} are
 * exposed for that purpose.
 */
public record RepeaterPattern(int times, double intervalSeconds,
                              Pattern inner) implements Pattern {

    /**
     * Executes the first burst immediately. Subsequent bursts are expected to be
     * driven externally (e.g. by {@link RandomizedShootAbility}).
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (inner == null || times <= 0) return;
        inner.execute(shooter, type, angle, customizer);
    }
}