package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;

import java.util.function.Consumer;

public interface Pattern {
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer);
}
