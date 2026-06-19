package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import java.util.function.Consumer;

public class ShootAbility extends AbstractShootAbility {
    private final Pattern pattern;
    private final double dannoProiettile;

    public ShootAbility(double combatRange, double cooldownSec,
                        ProjectileType bulletType, Pattern pattern,
                        Target target, boolean aimAtTarget, double nerfPrediction,
                        double dannoProiettile, int attackStateIndex) {

        super("shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget, nerfPrediction, attackStateIndex);
        this.pattern = pattern;
        this.dannoProiettile = dannoProiettile;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        super.onActivate(brain, world);

        double angle = getAimAngle(brain, world, bulletType.terminalVelocity);

        // Definiamo il customizer per applicare il tipo e il danno dinamico
        Consumer<ProjectileModel> customizer = bullet -> {
            bullet.setType(bulletType);
            bullet.setEnergyDirect(dannoProiettile);
        };

        // Passiamo il customizer al pattern invece di null
        pattern.execute(brain.getShooter(), bulletType, angle, customizer);
        cooldown.start();
    }

    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return false; // instant
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}
}