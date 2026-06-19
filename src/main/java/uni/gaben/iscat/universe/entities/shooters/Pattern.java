package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.Shooter;

import java.util.function.Consumer;

public interface Pattern {
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer);
}
