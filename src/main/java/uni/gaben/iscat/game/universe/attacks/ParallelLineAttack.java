package uni.gaben.iscat.game.universe.attacks;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.Shooter;

// spara n proiettili paralleli distanziati in una direzione

public class ParallelLineAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {

    private final int lines;
    private final double spacingMeters;

    public ParallelLineAttack(int lines, double spacingMeters) {
        this.lines = lines;
        this.spacingMeters = spacingMeters;
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt) {
        // Vettore perpendicolare alla direzione di sparo, per distanziare le linee
        Vector2 perp = new Vector2(-Math.sin(targetAngle), Math.cos(targetAngle));

        // Offset simmetrico: es. lines=3 → i = -1, 0, 1
        int half = lines / 2;
        for (int i = -half; i <= half; i++) {
            Vector2 origin = entity.getTransform().getTranslation().copy()
                    .add(perp.copy().multiply(i * spacingMeters));
            shooter.shoot(template, origin, targetAngle);
        }

        return true; // Istantaneo, termina subito
    }

    @Override
    public void reset() {} // Stateless, niente da resettare
}