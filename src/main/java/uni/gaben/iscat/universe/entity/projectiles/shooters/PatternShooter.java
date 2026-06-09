package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

public interface PatternShooter {
    /**
     * Esegue l'attacco custom.
     */
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer);
}