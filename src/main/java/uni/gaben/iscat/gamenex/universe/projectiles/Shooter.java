package uni.gaben.iscat.gamenex.universe.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractShooterController;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;

public class Shooter<T extends HasProjectile & CollisionBody> extends AbstractShooterController<T> {

    public Shooter(T model) {
        super(model);
    }

    @Override
    protected AbstractProjectileModel[] shootingLogic(AbstractProjectileModel projectile) {
        AbstractProjectileModel blueprint = projectile.blueprint();
        blueprint.setTransform(model.getTransform());
        blueprint.translate(Vector2.create(1,
                model.getTransform().getRotationAngle()));
        blueprint.setLinearVelocity(
                Vector2.create(projectile.getTerminalVelocity(),model.getTransform().getRotationAngle()));
        return new AbstractProjectileModel[]{ blueprint };
    }


}
