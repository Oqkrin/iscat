package uni.gaben.iscat.iscat_game.lib.implementations.attacks;

import uni.gaben.iscat.iscat_game.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

/** Spara un proiettile */
public class SingleShotAttack implements AttackPattern {

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        shooter.shoot(template, angle, customizer);
    }
}