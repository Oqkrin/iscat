package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.brain.actions.shoot.RandomizedShootAction;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

/**
 * Fires the inner pattern {@code times} times, with {@code intervalSeconds} between each burst.
 * <p>
 * The first shot fires immediately. Remaining shots are tracked via game-loop delta time
 * through {@link uni.gaben.iscat.universe.UniverseController.(double)} so that all physics interactions stay on the main thread.
 * <p>
 * When used inside {@link RandomizedShootAction},
 * the burst state is managed by the action itself — {@link #times ()} and {@link #inner ()} are
 * exposed for that purpose.
 */
public record RepeaterPatternShooter(int times, double intervalSeconds,
                                     PatternShooter inner) implements PatternShooter {

    /**
     * Executes the first burst immediately. Subsequent bursts are expected to be
     * driven externally (e.g. by {@link RandomizedShootAction}).
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        if (inner == null || times <= 0) return;
        inner.execute(shooter, type, angle, customizer);
    }
}