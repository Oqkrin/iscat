package uni.gaben.iscat.universe.entity.brain.actions.shoot;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.projectiles.shooters.PatternShooter;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;

import java.util.Collections;

public class ShootAction extends AbstractShootAction {
    private final PatternShooter pattern;

    public ShootAction(double combatRange, double cooldownSec,
                       ProjectileType bulletType, PatternShooter pattern,
                       Target target, boolean aimAtTarget) {
        super("shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget);
        this.pattern = pattern;
    }

    public static ShootAction targetingPlayer(double combatRange, double cooldownSec,
                                              ProjectileType bulletType, PatternShooter pattern,
                                              boolean aimAtTarget) {
        return new ShootAction(combatRange, cooldownSec, bulletType, pattern,
                universe -> Collections.singletonList(universe.getPlayer()), aimAtTarget);
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