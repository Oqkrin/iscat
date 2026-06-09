package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.EntityModel;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;
import uni.gaben.iscat.utils.EnemyAudioManager;

import java.util.function.Consumer;

public interface PatternShooter {
    /**
     * Esegue l'attacco custom.
     */
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer);

    /**
     * Helper di default per riprodurre automaticamente l'audio di attacco se a sparare è un nemico.
     */
    default void playAttackAudio(Shooter<?> shooter) {
        if (shooter != null && shooter.getModel() instanceof EntityModel entity) {
            EnemyAudioManager.playEventAudio(entity, "attack");
        }
    }
}