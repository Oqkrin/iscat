package uni.gaben.iscat.universe.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractShooterController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.Random;
import java.util.function.Consumer;

public class Shooter<T extends CollisionBody> extends AbstractShooterController<T> {
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

    public T getModel() { return this.model; }

    @Override
    public void shoot(AbstractProjectileModel template) {
        shoot(template, null);
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
            if (p.shouldRemove()) return;

            if (otherEntity instanceof AbstractProjectileModel otherProj) {
            } else {
                if (otherEntity instanceof LifeDeath target) {
                    if (target instanceof LivingEntityModel lem) {
                        lem.setKilledByProjectile(true);
                    }
                    target.deltaToLife(-p.getLife());
                    if (!(target instanceof PlayerModel)) {
                        SessionScoreTracker.getInstance().addDamageDealt((int) p.getLife());
                    }
                }
                p.kill(true);
            }
        });
        AudioManager.getInstance().playSFX("shoot");
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