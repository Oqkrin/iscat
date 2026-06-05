package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

/**
 * Fires the inner pattern {@code times} times, with {@code intervalSeconds} between each burst.
 * <p>
 * The first shot fires immediately. Remaining shots are tracked via game-loop delta time
 * through {@link #update(double)} so that all physics interactions stay on the main thread.
 * <p>
 * When used inside {@link uni.gaben.iscat.universe.entity.brain.actions.shoot.RandomizedShootAction},
 * the burst state is managed by the action itself — {@link #getTimes()} and {@link #getInner()} are
 * exposed for that purpose.
 */
public class RepeaterPatternShooter implements PatternShooter {

    private final int times;
    private final PatternShooter inner;
    private final double intervalSeconds;

    public RepeaterPatternShooter(int times, double intervalSeconds, PatternShooter inner) {
        this.times = times;
        this.intervalSeconds = intervalSeconds;
        this.inner = inner;
    }

    public int getTimes() { return times; }
    public double getIntervalSeconds() { return intervalSeconds; }
    public PatternShooter getInner() { return inner; }

    /**
     * Executes the first burst immediately. Subsequent bursts are expected to be
     * driven externally (e.g. by {@link uni.gaben.iscat.universe.entity.brain.actions.shoot.RandomizedShootAction}).
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        if (inner == null || times <= 0) return;
        inner.execute(shooter, type, angle, customizer);
    }
}