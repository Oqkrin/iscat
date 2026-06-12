package uni.gaben.iscat.universe.entity.projectiles.shooters;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.Alterable;

import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.projectiles.ProjectilePool;
import uni.gaben.iscat.utils.EnemyAudioManager;
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
        this.distance = (model instanceof GameEntity aem)
                ? aem.physicsModule.getHeightMeters() / 2.0
                : 0.1;
    }

    public T getModel() { return model; }

    private void triggerAttackAudio() {
        if (model instanceof GameEntity entity) {
            EnemyAudioManager.playEventAudio(entity, "attack");
        }
    }

    // -------------------------------------------------------------------------
    // Public shoot API
    // -------------------------------------------------------------------------

    /** Spara nella direzione corrente del modello. */
    public void shoot(String type) {
        shoot(type, model.getTransform().getRotationAngle(), null);
    }

    /** Spara nell'angolo specificato. */
    public void shoot(String type, double angle) {
        shoot(type, angle, null);
    }

    /** Spara nell'angolo specificato, applicando un customizer al proiettile. */
    public void shoot(String type, double angle, Consumer<GameEntity> customizer) {
        GameEntity bullet = ProjectilePool.acquire(type, null);
        bullet.getTransform().setTranslation(model.getTransform().getTranslation().copy());
        bullet.getTransform().setRotation(angle);
        bullet.translate(Vector2.create(distance, angle));
        // Propagate ownerId from shooter to bullet for collision layer selection
        applyOwnerId(bullet);
        double terminalVel = resolveTerminalVelocity(bullet);
        bullet.setLinearVelocity(Vector2.create(terminalVel, angle));
        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    /** Spara da una posizione arbitraria nell'angolo specificato. */
    public void shoot(String type, Vector2 position, double angle) {
        shoot(type, position, angle, null);
    }

    /** Spara da una posizione arbitraria nell'angolo specificato, con customizer. */
    public void shoot(String type, Vector2 position, double angle, Consumer<GameEntity> customizer) {
        GameEntity bullet = ProjectilePool.acquire(type, null);
        bullet.getTransform().setTranslation(position.copy());
        bullet.getTransform().setRotation(angle);
        applyOwnerId(bullet);
        double terminalVel = resolveTerminalVelocity(bullet);
        bullet.setLinearVelocity(Vector2.create(terminalVel, angle));
        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /** Resolves terminal velocity from physics or dynamics data, with a sensible fallback. */
    private double resolveTerminalVelocity(GameEntity bullet) {
        if (bullet.getRecord() != null) {
            if (bullet.getRecord().physics() != null && bullet.getRecord().physics().terminalVelocity() > 0)
                return bullet.getRecord().physics().terminalVelocity();
            if (bullet.getRecord().dynamics() != null && bullet.getRecord().dynamics().terminalVelocity() > 0)
                return bullet.getRecord().dynamics().terminalVelocity();
        }
        return 400.0;
    }

    /**
     * Propagates the shooter's identity as the bullet's ownerId so that
     * PhysicsModule applies the correct collision filter (player vs enemy projectile).
     * Because EntityRecord is immutable we rebuild the identity wrapper.
     */
    private void applyOwnerId(GameEntity bullet) {
        if (bullet.getRecord() == null || bullet.getRecord().identity() == null) return;
        if (model instanceof GameEntity shooter && shooter.getRecord() != null && shooter.getRecord().identity() != null) {
            String shooterKey = shooter.getRecord().identity().entityKey();
            // Rebuild identity with ownerId; PhysicsModule reads this to pick the filter
            var oldId = bullet.getRecord().identity();
            var newId = new uni.gaben.iscat.universe.entity.Data.IdentityData(
                    oldId.entityKey(), oldId.name(), oldId.description(),
                    oldId.isBoss(), oldId.type(), shooterKey
            );
            // Patch the collision filter on the existing fixture via PhysicsModule
            if (bullet.getPhysicsModule() != null) {
                // Temporarily swap the identity and recreate fixture filter
                bullet.getPhysicsModule().applyOwnerFilter(newId);
            }
        }
    }

    private void setupAndSpawn(GameEntity p) {
        p.setOnCollision(otherEntity -> {
            if (p.shouldRemove()) return;
            if (otherEntity.getRecord() != null && otherEntity.getRecord().physics() != null && otherEntity.getRecord().physics().isProjectile()) return;

            if (otherEntity instanceof Alterable target) {
                target.alter(-p.getEndurance());
                otherEntity.setKilledByProjectile(true);
                if (otherEntity.getRecord() == null || otherEntity.getRecord().identity() == null || !otherEntity.getRecord().identity().entityKey().contains("player")) {
                    SessionScoreTracker.getInstance().addDamageDealt((int) p.getEndurance());
                }
            }
            p.extinguish(true);
        });

        UniverseSpawner.getInstance().spawnEntity(p);
    }
}
