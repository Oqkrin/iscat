package uni.gaben.iscat.universe.entities.projectiles;

import org.dyn4j.collision.CollisionBody;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.utils.EntityAudioManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.function.Consumer;

/**
 * Componente generico per la gestione del sistema di sparo e lancio proiettili dalle entità.
 * Ottimizzato matematicamente per azzerare le allocazioni di vettori temporanei (Garbage Collection Free).
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

    public T getModel() {
        return model;
    }

    private void triggerAttackAudio() {
        if (model instanceof AbstractLivingEntityModel entity) {
            EntityAudioManager.playEventAudio(entity, "attack");
        }
    }

    public void shoot(ProjectileType type) {
        shoot(type, model.getTransform().getRotationAngle(), null);
    }

    public void shoot(ProjectileType type, double angle) {
        shoot(type, angle, null);
    }

    /**
     * Spara un proiettile calcolando la traiettoria e la posizione iniziale inline.
     * Ottimizzato per non allocare vettori temporanei tramite funzioni trigonometriche dirette.
     */
    public void shoot(ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        triggerAttackAudio();
        ProjectileModel bullet = ProjectilePool.acquire(type);

        Vector2 origin = model.getTransform().getTranslation();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        bullet.getTransform().setTranslation(origin.x + (distance * cos), origin.y + (distance * sin));
        bullet.getTransform().setRotation(angle);

        double speed = bullet.getTerminalVelocity();
        bullet.setLinearVelocity(speed * cos, speed * sin);

        if (model instanceof PlayerModel pm) {
            bullet.setDannoDinamico(pm.getProjectileDamage());
        } else if (model instanceof AbstractLivingEntityModel lem && lem.getEntityRecord() != null) {
            bullet.setDannoDinamico(lem.getEntityRecord().dannoProiettile());
        }

        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    public void shoot(ProjectileType type, Vector2 position, double angle) {
        shoot(type, position, angle, null);
    }

    /**
     * Spara un proiettile partendo da una posizione vettoriale personalizzata senza duplicare l'oggetto in memoria.
     */
    public void shoot(ProjectileType type, Vector2 position, double angle, Consumer<ProjectileModel> customizer) {
        triggerAttackAudio();
        ProjectileModel bullet = ProjectilePool.acquire(type);

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        bullet.getTransform().setTranslation(position.x, position.y);
        bullet.getTransform().setRotation(angle);

        double speed = bullet.getTerminalVelocity();
        bullet.setLinearVelocity(speed * cos, speed * sin);

        if (model instanceof PlayerModel pm) {
            bullet.setDannoDinamico(pm.getProjectileDamage());
        } else if (model instanceof AbstractLivingEntityModel lem && lem.getEntityRecord() != null) {
            bullet.setDannoDinamico(lem.getEntityRecord().dannoProiettile());
        }

        if (customizer != null) customizer.accept(bullet);
        setupAndSpawn(bullet);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void setupAndSpawn(AbstractPhysicalProjectileModel p) {
        p.addOnCollision("projectileDamage", otherEntity -> {
            if (p.shouldRemove()) return;
            if (otherEntity instanceof AbstractPhysicalProjectileModel) return;

            if (otherEntity instanceof Alterable target) {
                double damageValue = p.getDannoDinamico();

                if (target instanceof PlayerModel pm) {
                    if (pm.absorbProjectile(damageValue)) {
                        p.extinguish(true);
                        return;
                    }
                }

                p.extinguish(true);

                if (target instanceof AbstractLivingEntityModel lem) {
                    lem.setKilledByProjectile(true);
                }

                target.damage(damageValue);
                if (!(target instanceof PlayerModel)) {
                    SessionScoreTracker.getInstance().addDamageDealt((int) damageValue);
                }
            } else {
                p.extinguish(true);
            }
        });

        UniverseSpawner.getInstance().spawnEntity(p);
    }
}