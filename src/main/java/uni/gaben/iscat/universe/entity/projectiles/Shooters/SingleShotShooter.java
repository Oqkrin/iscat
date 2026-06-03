package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

/** Spara un proiettile */
public class SingleShotShooter implements ShooterPattern {

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        shooter.shoot(type, angle, customizer);
    }
}