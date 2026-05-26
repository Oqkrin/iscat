package uni.gaben.iscat.universe.lib.implementations.attacks;

import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.Shooter;

import java.util.function.Consumer;

/** Spara un proiettile */
public class SingleShotAttack implements AttackPattern {

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        shooter.shoot(template, angle, customizer);
    }
}