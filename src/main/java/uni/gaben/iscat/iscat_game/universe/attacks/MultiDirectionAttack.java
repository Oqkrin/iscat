package uni.gaben.iscat.iscat_game.universe.attacks;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

// wrappa qualsiasi AttackPattern e lo esegue in N direzioni

public class MultiDirectionAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {

    private final int directions;
    private final AttackPattern<T> inner;
    private double baseAngleOffset;

    private int currentDir = 0;
    private boolean started = false;

    /**
     * @param directions  numero di direzioni (es. 4 → 0°, 90°, 180°, 270°)
     * @param angleOffset offset iniziale in radianti (es. rotazione del boss)
     * @param inner       il pattern da eseguire per ogni direzione
     */
    public MultiDirectionAttack(int directions, double angleOffset, AttackPattern<T> inner) {
        this.directions = directions;
        this.baseAngleOffset = angleOffset;
        this.inner = inner;
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt) {
        if (!started) {
            this.baseAngleOffset = entity.getTransform().getRotationAngle();
            inner.reset();
            started = true;
        }

        double angleForDir = baseAngleOffset + (currentDir * (2.0 * Math.PI / directions));

        boolean dirDone = inner.updateAndExecute(entity, shooter, template, angleForDir, dt);

        if (dirDone) {
            currentDir++;
            if (currentDir < directions) {
                inner.reset(); // Resetta l'inner per la direzione successiva
            }
        }

        return currentDir >= directions;
    }

    @Override
    public void reset() {
        this.currentDir = 0;
        this.started = false;
        inner.reset();
    }
}