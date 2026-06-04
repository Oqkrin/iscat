package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Attacco che definisce quanti colpi di fila deve eseguire un determinato attacco.
 */
public class RepeaterPatternShooter implements PatternShooter {

    private final int times;
    private final PatternShooter inner;

    public RepeaterPatternShooter(int times, PatternShooter inner) {
        this.times = times;
        this.inner = inner;
    }

    public int getTimes() {
        return times;
    }

    public PatternShooter getInner() {
        return inner;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        // Delega l'esecuzione immediata all'attacco interno
        if (inner != null) {
            inner.execute(shooter, type, angle, customizer);
        }
    }
}