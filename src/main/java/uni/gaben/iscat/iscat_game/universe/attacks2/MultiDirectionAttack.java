package uni.gaben.iscat.iscat_game.universe.attacks2;

import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

/** spara un tipo di attacco in n direzioni  */
public class MultiDirectionAttack implements AttackPattern {

    private final int directions;
    private final double angleOffset;
    private final AttackPattern inner;

    /**
     * @param directions  Numero di direzioni in cui replicare l'attacco (es. 4 -> Croce).
     * @param angleOffset Offset angolare iniziale in radianti rispetto alla mira.
     * @param inner       Il pattern di attacco da eseguire in ogni singola direzione.
     */
    public MultiDirectionAttack(int directions, double angleOffset, AttackPattern inner) {
        this.directions = directions;
        this.angleOffset = angleOffset;
        this.inner = inner;
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        if (directions <= 0 || inner == null) return;

        double angleStep = (2.0 * Math.PI) / directions;

        for (int i = 0; i < directions; i++) {
            // Calcola l'angolo per questa specifica direzione basandosi sull'asse di puntamento attuale
            double currentAngle = angle + angleOffset + (i * angleStep);

            // Inietta l'angolo calcolato nell'attacco interno
            inner.execute(shooter, template, currentAngle, customizer);
        }
    }
}