package uni.gaben.iscat.universe.entity.brain.abilities.shoot;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.projectiles.shooters.PatternShooter;
import uni.gaben.iscat.universe.entity.brain.*;


public class ShootAbility extends AbstractShootAbility {
    private final PatternShooter pattern;

    public ShootAbility(double combatRange, double cooldownSec,
                        String bulletType, PatternShooter pattern,
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
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false; // instant
    }
}
