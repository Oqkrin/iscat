package uni.gaben.iscat.game.universe.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractShooterController;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.game.universe.UniverseSpawner;

import java.util.Random;
import java.util.function.Consumer;

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

    @Override
    public void shoot(AbstractProjectileModel template) {
        shoot(template, (Consumer<Projectile>) null);
    }

    public void shoot(AbstractProjectileModel template, Consumer<Projectile> customizer) {
        Projectile bullet = (Projectile) template.blueprint();
        if (customizer != null) {
            customizer.accept(bullet);
        }
        bullet.setTransform(model.getTransform());
        bullet.translate(Vector2.create(distance, model.getTransform().getRotationAngle()));
        bullet.setLinearVelocity(
                Vector2.create(bullet.getTerminalVelocity(),
                        model.getTransform().getRotationAngle())
        );
        setupProjectileAndSpawn(bullet);
    }

    public void shoot(AbstractProjectileModel template, double angle) {
        shoot(template, angle, null);
    }

    public void shoot(AbstractProjectileModel template, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = (Projectile) template.blueprint();
        if (customizer != null) {
            customizer.accept(bullet);
        }
        bullet.getTransform().setTranslation(model.getTransform().getTranslation().copy());
        bullet.getTransform().setRotation(angle);
        bullet.translate(Vector2.create(distance, angle));
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        setupProjectileAndSpawn(bullet);
    }

    public void shoot(AbstractProjectileModel template, Vector2 position, double angle) {
        shoot(template, position, angle, null);
    }

    public void shoot(AbstractProjectileModel template, Vector2 position, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = (Projectile) template.blueprint();
        if (customizer != null) {
            customizer.accept(bullet);
        }
        bullet.getTransform().setTranslation(position.copy());
        bullet.getTransform().setRotation(angle);
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        setupProjectileAndSpawn(bullet);
    }

    private void setupProjectileAndSpawn(AbstractProjectileModel p) {
        p.setOnCollision(otherEntity -> {
            if (p.shouldRemove()) return; // guard: evita doppia collisione nello stesso tick

            if (otherEntity instanceof AbstractProjectileModel otherProj) {
                // Symmetrical projectile-to-projectile collision:
                // Damage is calculated proportionally, allowing stronger projectiles to damage weaker ones
                // while both bounce/reflect without instant annihilation.
                double selfLife = p.getLife();
                double otherLife = otherProj.getLife();

                // Compute self-damage based on the other's life relative to self life
                double damageToSelf = 1.0 * (otherLife / Math.max(0.1, selfLife));
                p.deltaToLife(-damageToSelf);

                // Symmetrical bounce reflection: reflect the incoming angle over the other's angle
                double incoming = p.getTransform().getRotationAngle();
                double otherAngle = otherProj.getTransform().getRotationAngle();
                double reflected = 2 * otherAngle - incoming;
                p.getTransform().setRotation(reflected);
                p.setLinearVelocity(Vector2.create(p.getTerminalVelocity(), reflected));
            } else {
                // Standard projectile-to-non-projectile collision:
                if (otherEntity instanceof LifeDeath target) {
                    target.deltaToLife(-p.getLife());
                }
                p.kill(true);
            }
        });
        IscatAudioManager.getInstance().playSFX("shoot");
        UniverseSpawner.getInstance().spawnEntity(p);
    }

    @Override
    protected AbstractProjectileModel shootingLogic(AbstractProjectileModel template) {
        Projectile bullet = (Projectile) template.blueprint();
        bullet.setTransform(model.getTransform());
        bullet.translate(Vector2.create(distance, model.getTransform().getRotationAngle()));
        bullet.setLinearVelocity(
                Vector2.create(template.getTerminalVelocity(),
                        model.getTransform().getRotationAngle())
        );
        return bullet;
    }
}
