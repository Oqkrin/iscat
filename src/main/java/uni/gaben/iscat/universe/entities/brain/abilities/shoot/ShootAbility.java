package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;

public class ShootAbility extends AbstractShootAbility {
    private final Pattern pattern;

    public ShootAbility(double combatRange, double cooldownSec,
                        ProjectileType bulletType, Pattern pattern,
                        Target target, boolean aimAtTarget, double nerfPrediction) {
        super("shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget, nerfPrediction);
        this.pattern = pattern;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        double angle = getAimAngle(brain, world, bulletType.terminalVelocity);
        pattern.execute(brain.getShooter(), bulletType, angle, null);
        cooldown.start();
    }

    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return false; // instant
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}
}