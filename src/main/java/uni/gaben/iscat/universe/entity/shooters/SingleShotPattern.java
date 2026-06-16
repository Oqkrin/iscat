package uni.gaben.iscat.universe.entity.shooters;

import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;

import java.util.function.Consumer;

/** Spara un proiettile */
public class SingleShotPattern implements Pattern {

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        shooter.shoot(type, angle, customizer);
    }
}