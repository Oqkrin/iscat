package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.LifeDeath;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.function.Consumer;

/**
 * Gestisce la creazione, la configurazione fisica e lo spawn dei proiettili.
 * Supporta molteplici modalità di tiro (angolo, posizione arbitraria, direzione del modello).
 * Tutti i proiettili vengono acquisiti dal {@link ProjectilePool} per evitare allocazioni eccessive.
 */
public class Shooter<T extends CollisionBody> {

    protected final T model;
    private final double distance;

    public Shooter(T model) {
        this.model = model;
        this.distance = (model instanceof AbstractEntityModel aem)
                ? aem.getHeightMeters() / 2.0
                : 0.1;
    }

    public T getModel() { return model; }

    // -------------------------------------------------------------------------
    // Public shoot API
    // -------------------------------------------------------------------------

    /** Spara nella direzione corrente del modello. */
    public void shoot(ProjectileType type) {
        shoot(type, model.getTransform().getRotationAngle(), null);
    }

    /** Spara nell'angolo specificato. */
    public void shoot(ProjectileType type, double angle) {
        shoot(type, angle, null);
    }

    /** Spara nell'angolo specificato, applicando un customizer al proiettile. */
    public void shoot(ProjectileType type, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = ProjectilePool.acquire(type);
        bullet.getTransform().setTranslation(model.getTransform().getTranslation().copy());
        bullet.getTransform().setRotation(angle);
        bullet.translate(Vector2.create(distance, angle));
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    /** Spara da una posizione arbitraria nell'angolo specificato. */
    public void shoot(ProjectileType type, Vector2 position, double angle) {
        shoot(type, position, angle, null);
    }

    /** Spara da una posizione arbitraria nell'angolo specificato, con customizer. */
    public void shoot(ProjectileType type, Vector2 position, double angle, Consumer<Projectile> customizer) {
        Projectile bullet = ProjectilePool.acquire(type);
        bullet.getTransform().setTranslation(position.copy());
        bullet.getTransform().setRotation(angle);
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void setupAndSpawn(AbstractProjectileModel p) {
        p.setOnCollision(otherEntity -> {
            if (p.shouldRemove()) return;
            if (otherEntity instanceof AbstractProjectileModel) return; // proiettili non si colpiscono tra loro

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
        });

        AudioManager.getInstance().playSFX("shoot");
        UniverseSpawner.getInstance().spawnEntity(p);
    }
}