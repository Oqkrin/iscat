package uni.gaben.iscat.game.universe.attacks;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;

//---------------------------------------------------------------------
//  Questo attacco fa una raffica (burst) di colpi in linea retta.
//---------------------------------------------------------------------

public class SingleBurstAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {
    private int shotsLeft;
    private final int totalShots;
    private final double delay;
    private Cooldown timer = new Cooldown();

    private Double lockedAngle = null;

    public SingleBurstAttack(int totalShots, double delay) {
        this.totalShots = totalShots;
        this.shotsLeft = totalShots;
        this.delay = delay;
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt) {
        // Blocca la mira verso il player al momento del primo colpo
        if (lockedAngle == null) {
            lockedAngle = targetAngle;
            timer.start(0); // Spara il primo colpo subito
        }

        timer.update(dt);

        if (!timer.isCoolingDown() && shotsLeft > 0) {
            double originalAngle = entity.getTransform().getRotationAngle();
            entity.getTransform().setRotation(lockedAngle);
            shooter.shoot(template);
            entity.getTransform().setRotation(originalAngle);

            shotsLeft--;
            timer.start(delay); // Fa ripartire l'attesa per il prossimo colpo
        }

        return shotsLeft <= 0; // Termina quando finisce i colpi
    }

    @Override
    public void reset() {
        this.shotsLeft = totalShots; // totalShots deve diventare un campo final
        this.lockedAngle = null;
        this.timer = new Cooldown();
    }
}