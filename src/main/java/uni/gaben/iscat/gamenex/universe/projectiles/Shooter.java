package uni.gaben.iscat.gamenex.universe.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractShooterController;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Lifecycle;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_core.IscatCoreSettings;

import java.util.Random;

public class Shooter<T extends HasProjectile & CollisionBody> extends AbstractShooterController<T> {

    private double distance;
    private final Random random = new Random();
    public Shooter(T model) {
        super(model);
        if(model instanceof AbstractEntityModel aem){
            distance = aem.getHeightMeters()/2;
        } else {
            distance = .1;
        }
    }

    public void shoot(AbstractProjectileModel template) {
        AbstractProjectileModel[] projectiles = shootingLogic(template);
        


        for (AbstractProjectileModel p : projectiles) {
            // Riproduce il suono di sparo
           IscatAudioManager.getInstance().playSFX("shoot");
           UniverseSpawner.getInstance().spawnEntity(p);
        }
    }

    @Override
    protected AbstractProjectileModel[] shootingLogic(AbstractProjectileModel template) {
        Projectile bullet = (Projectile) template.blueprint();
        bullet.setTransform(model.getTransform());
        bullet.translate(Vector2.create(distance, model.getTransform().getRotationAngle()));
        bullet.setLinearVelocity(
                Vector2.create(template.getTerminalVelocity(),
                        model.getTransform().getRotationAngle())
        );

        // QUANDO IL PROIETTILE COLLIDE
        bullet.setOnCollision(otherEntity -> {
            if (bullet.shouldRemove()) return; // guard: evita doppia collisione nello stesso tick

            if (otherEntity instanceof AbstractProjectileModel) {
                bullet.getTransform().setRotation(otherEntity.getTransform().getRotation().getRotated(random.nextDouble(-Math.PI, Math.PI)));
            } else if (otherEntity instanceof Lifecycle target) {
                target.deltaToLife(-bullet.getDamage());

                bullet.kill();
                bullet.setShouldRemove(true);
            }
            // Applica danno solo a entità con vita (Player, Enemy, ecc.)
            // Il proiettile si auto-distrugge sempre al primo impatto
        });

        return new AbstractProjectileModel[]{ bullet };
    }
}
