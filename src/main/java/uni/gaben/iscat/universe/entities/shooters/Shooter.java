package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectilePool;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.utils.EntityAudioManager;
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
        this.distance = (model instanceof AbstractPhysicalEntityModel aem)
                ? aem.getHeightMeters() / 2.0
                : 0.1;
    }

    public T getModel() { return model; }

    private void triggerAttackAudio() {
        if (model instanceof AbstractLivingEntityModel entity) {
            EntityAudioManager.playEventAudio(entity, "attack");
        }
    }

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
    public void shoot(ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        triggerAttackAudio();
        ProjectileModel bullet = ProjectilePool.acquire(type);
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
    public void shoot(ProjectileType type, Vector2 position, double angle, Consumer<ProjectileModel> customizer) {
        triggerAttackAudio();
        ProjectileModel bullet = ProjectilePool.acquire(type);
        bullet.getTransform().setTranslation(position.copy());
        bullet.getTransform().setRotation(angle);
        bullet.setLinearVelocity(Vector2.create(bullet.getTerminalVelocity(), angle));
        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void setupAndSpawn(AbstractPhysicalProjectileModel p) {
        p.addOnCollision( "projectileDamage", otherEntity -> {
            if (p.shouldRemove()) return;
            if (otherEntity instanceof AbstractPhysicalProjectileModel) return; // proiettili non si colpiscono tra loro

            if (otherEntity instanceof Alterable target) {
                if (target instanceof AbstractLivingEntityModel lem) {
                    lem.setKilledByProjectile(true);
                }
                target.damage(p.getEndurance());
                if (!(target instanceof PlayerModel)) {
                    SessionScoreTracker.getInstance().addDamageDealt((int) p.getEndurance());
                }
            }
            p.extinguish(true);
        });

        UniverseSpawner.getInstance().spawnEntity(p);
    }
}