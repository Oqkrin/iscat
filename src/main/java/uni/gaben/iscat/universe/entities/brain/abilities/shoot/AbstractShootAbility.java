package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.brain.abilities.Ability;
import uni.gaben.iscat.universe.entities.brain.abilities.AbilityCategory;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Set;

public abstract class AbstractShootAbility extends Ability {
    protected final Cooldown cooldown;
    protected final double combatRange;
    protected final ProjectileType bulletType;
    protected final Target target;
    protected Vector2 targetPos = UU.vector2zero();
    protected final boolean aimAtTarget;

    private double nerfPrediction;

    protected AbstractShootAbility(String name, double combatRange, double cooldownSec,
                                   ProjectileType bulletType, Target target, boolean aimAtTarget, double nerfPrediction) {
        super(name, AbilityCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.bulletType = bulletType;
        this.target = target;
        this.aimAtTarget = aimAtTarget;
        this.nerfPrediction = nerfPrediction;
    }

    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
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
            target.predictedPosition(universe, brain.getEntity().getTransform().getTranslation(), velocity*(1+nerfPrediction), targetPos);
            return targetPos != null ? brain.angleToTarget(targetPos) : brain.getEntity().getTransform().getRotationAngle();
        } else {
            return brain.getEntity().getTransform().getRotationAngle();
        }
    }

    public void setCooldown(double v) {
        cooldown.setDefaultDuration(v);
    }
}