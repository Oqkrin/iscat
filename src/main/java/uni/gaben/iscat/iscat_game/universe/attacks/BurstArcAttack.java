package uni.gaben.iscat.iscat_game.universe.attacks;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

//---------------------------------------------------------------------
//  Questo attacco è un attacco a raffiche di archi
//---------------------------------------------------------------------

public class BurstArcAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {
    private final int totalBursts;
    private final double delay;
    private final double spreadRadians;

    private int burstsLeft;
    private double timer = 0.0;
    private boolean initialized = false;

    public BurstArcAttack(int totalBursts, double delay, double spreadDegrees) {
        this.totalBursts = totalBursts;
        this.delay = delay;
        this.spreadRadians = Math.toRadians(spreadDegrees);
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt) {
        if (!initialized) {
            this.burstsLeft = totalBursts;
            this.timer = delay; // Fa partire il primo arco subito
            this.initialized = true;
        }

        timer += dt;
        if (timer >= delay && burstsLeft > 0) {
            timer -= delay;

            // Salva l'angolo originale del boss
            double originalAngle = entity.getTransform().getRotationAngle();

            // Spara i 3 proiettili ad arco modificando temporaneamente la rotazione dell'entità
            for (int i = -1; i <= 1; i++) {
                entity.getTransform().setRotation(targetAngle + (i * spreadRadians));
                shooter.shoot(template);
            }

            // Ripristina l'angolo grafico originale
            entity.getTransform().setRotation(originalAngle);

            burstsLeft--;
        }

        return burstsLeft <= 0;
    }

    public void reset() {
        this.burstsLeft = totalBursts;
        this.timer = 0.0;
        this.initialized = false;
    }
    @Override
    public void onStart(AbstractEntityModel entity) {
        if (entity instanceof IscatMasterModel m)
            m.setAnimationState(IscatMasterModel.AnimationState.ATTACK2);
    }
}