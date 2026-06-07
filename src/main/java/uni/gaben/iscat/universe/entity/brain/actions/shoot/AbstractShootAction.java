package uni.gaben.iscat.universe.entity.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.actions.Action;
import uni.gaben.iscat.universe.entity.brain.actions.ActionCategory;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Set;

public abstract class AbstractShootAction extends Action {
    protected final Cooldown cooldown;
    protected final double combatRange;
    protected final ProjectileType bulletType;
    protected final Target target;
    protected Vector2 targetPos = UU.vector2zero();
    protected final boolean aimAtTarget;

    protected AbstractShootAction(String name, double combatRange, double cooldownSec,
                               ProjectileType bulletType, Target target, boolean aimAtTarget) {
        super(name, ActionCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.bulletType = bulletType;
        this.target = target;
        this.aimAtTarget = aimAtTarget;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        targetPos.set(0, 0);
        cooldown.update(dt);
        if (cooldown.isCoolingDown()) return false;
        targetPos = target.getPosition(world);
        if (targetPos == null) return false;
        if (combatRange >= 0) {
            double dist = self.getTransform().getTranslation().distance(targetPos);
            if (dist > combatRange) return false;
        }
        return true;
    }

    protected double getAimAngle(Brain<?> brain, UniverseModel universe, double velocity) {
        targetPos.set(0, 0);
        if (aimAtTarget) {
            target.predictedPosition(universe, brain.getEntity().getTransform().getTranslation(), velocity, targetPos);
            return targetPos != null ? brain.angleToTarget(targetPos) : brain.getEntity().getTransform().getRotationAngle();
        } else {
            return brain.getEntity().getTransform().getRotationAngle();
        }
    }

    public void setCooldown(double v) {
        cooldown.setDefaultDuration(v);
    }
}