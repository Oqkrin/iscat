package uni.gaben.iscat.iscat_game.lib.implementations.attacks;

import uni.gaben.iscat.iscat_game.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Attacco che definisce quanti colpi di fila deve eseguire un determinato attacco.
 */
public class RepeaterAttack implements AttackPattern {

    private final int times;
    private final AttackPattern inner;

    public RepeaterAttack(int times, AttackPattern inner) {
        this.times = times;
        this.inner = inner;
    }

    public int getTimes() {
        return times;
    }

    public AttackPattern getInner() {
        return inner;
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        // Delega l'esecuzione immediata all'attacco interno
        if (inner != null) {
            inner.execute(shooter, template, angle, customizer);
        }
    }
}