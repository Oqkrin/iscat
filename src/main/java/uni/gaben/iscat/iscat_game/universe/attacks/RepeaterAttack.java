package uni.gaben.iscat.iscat_game.universe.attacks;

import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Attacco che definisce quanti colpi di fila deve eseguire un determinato attacco.
 */
public class RepeaterAttack implements AttackPattern {

    private final int bursts;
    private final AttackPattern inner;

    public RepeaterAttack(int bursts, AttackPattern inner) {
        this.bursts = bursts;
        this.inner = inner;
    }

    public int getBursts() {
        return bursts;
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