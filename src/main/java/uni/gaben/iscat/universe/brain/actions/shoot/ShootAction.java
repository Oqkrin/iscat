package uni.gaben.iscat.universe.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.*;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.projectiles.ProjectileType;

import java.util.function.Function;

public class ShootAction extends AbstractShootAction {
    private final AttackPattern pattern;

    public ShootAction(double combatRange, double cooldownSec,
                       ProjectileType bulletType, AttackPattern pattern,
                       Function<UniverseModel, Vector2> targetSupplier) {
        super("shoot", combatRange, cooldownSec, bulletType, targetSupplier);
        this.pattern = pattern;
    }

    public static ShootAction targetingPlayer(double combatRange, double cooldownSec,
                                              ProjectileType bulletType, AttackPattern pattern) {
        return new ShootAction(combatRange, cooldownSec, bulletType, pattern,
                world -> {
                    var p = world.getPlayer();
                    return p != null ? p.getTransform().getTranslation() : null;
                });
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        double angle = getAimAngle(brain, world);
        pattern.execute(brain.getShooter(), createBullet(), angle, null);
        cooldown.start();
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false; // instant
    }
}