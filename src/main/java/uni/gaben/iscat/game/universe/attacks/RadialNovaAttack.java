package uni.gaben.iscat.game.universe.attacks;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.enemies.iscat_master.IscatMasterModel;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.Shooter;

//---------------------------------------------------------------------
//  Questo attacco è un attacco ad anello radiale, spara proiettili in tutte le direzioni tipo a cerchio 360 gradi
//---------------------------------------------------------------------

public class RadialNovaAttack<T extends AbstractEntityModel & HasProjectile<?>> implements AttackPattern<T> {
    private final int totalDirections;

    public RadialNovaAttack(int totalDirections) {
        this.totalDirections = totalDirections;
    }

    @Override
    public boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt) {
        double originalAngle = entity.getTransform().getRotationAngle();
        double increment = (2.0 * Math.PI) / totalDirections;

        for (int i = 0; i < totalDirections; i++) {
            entity.getTransform().setRotation(i * increment);
            shooter.shoot(template);
        }

        entity.getTransform().setRotation(originalAngle);
        return true;
    }

    @Override
    public void reset() {}

    @Override
    public void onStart(AbstractEntityModel entity) {
        if (entity instanceof IscatMasterModel m)
            m.setAnimationState(IscatMasterModel.AnimationState.ATTACK3);
    }
}