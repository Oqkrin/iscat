package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.LifeDeath;
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
    public void shoot(ProjectileType type) {
        shoot(type, null);
    }

    public void shoot(ProjectileType type, Consumer<Projectile> customizer) {
        Projectile bullet = new Projectile(type);
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

    public void shoot(ProjectileType type, double angle) {
        shoot(type, angle, null);
    }

    public void shoot(ProjectileType type, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = new Projectile(type);
        if (customizer != null) {
            customizer.accept(bullet);
        }
        bullet.getTransform().setTranslation(model.getTransform().getTranslation().copy());
        bullet.getTransform().setRotation(angle);
        bullet.translate(Vector2.create(distance, angle));
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        setupProjectileAndSpawn(bullet);
    }

    public void shoot(ProjectileType type, Vector2 position, double angle) {
        shoot(type, position, angle, null);
    }

    public void shoot(ProjectileType type, Vector2 position, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = new Projectile(type);
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
    protected AbstractProjectileModel shootingLogic(ProjectileType type) {
        Projectile bullet = new Projectile(type);
        bullet.setTransform(model.getTransform());
        bullet.translate(Vector2.create(distance, model.getTransform().getRotationAngle()));
        bullet.setLinearVelocity(
                Vector2.create(type.terminalVelocity,
                        model.getTransform().getRotationAngle())
        );
        return bullet;
    }
}