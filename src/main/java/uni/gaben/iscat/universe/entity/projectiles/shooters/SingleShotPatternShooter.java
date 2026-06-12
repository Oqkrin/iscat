package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.GameEntity;


import java.util.function.Consumer;

/** Spara un proiettile */
public class SingleShotPatternShooter implements PatternShooter {

    @Override
    public void execute(Shooter<?> shooter, String type, double angle, Consumer<GameEntity> customizer) {
        shooter.shoot(type, angle, customizer);
    }
}
