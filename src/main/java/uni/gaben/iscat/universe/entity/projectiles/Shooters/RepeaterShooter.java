package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Attacco che definisce quanti colpi di fila deve eseguire un determinato attacco.
 */
public class RepeaterShooter implements ShooterPattern {

    private final int times;
    private final ShooterPattern inner;

    public RepeaterShooter(int times, ShooterPattern inner) {
        this.times = times;
        this.inner = inner;
    }

    public int getTimes() {
        return times;
    }

    public ShooterPattern getInner() {
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