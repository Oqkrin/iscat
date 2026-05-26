package uni.gaben.iscat.iscat_game.lib.implementations.attacks;


import uni.gaben.iscat.iscat_game.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

/** Spara n proiettili in ogni direzione a farma d'anello */
public class RingAttack implements AttackPattern {

    private final int count;

    /**
     * @param count Numero totale di proiettili distribuiti nell'anello a 360°.
     */
    public RingAttack(int count) {
        this.count = count;
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double currentAngle = angle + (i * angleStep);
            shooter.shoot(template, currentAngle, customizer);
        }
    }
}