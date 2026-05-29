package uni.gaben.iscat.universe.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.*;
import uni.gaben.iscat.universe.brain.actions.Action;
import uni.gaben.iscat.universe.brain.actions.ActionCategory;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Set;

public abstract class AbstractShootAction extends Action {
    protected final Cooldown cooldown;
    protected final double combatRange;
    protected final ProjectileType bulletType;
    protected final Target target;   // <-- was Function<...>

    public AbstractShootAction(String name, double combatRange, double cooldownSec,
                               ProjectileType bulletType, Target target) {
        super(name, ActionCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.bulletType = bulletType;
        this.target = target;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        cooldown.update(dt);
        if (cooldown.isCoolingDown()) return false;
        Vector2 targetPos = target.getPosition(world);
        if (targetPos == null) return false;
        if (combatRange >= 0) {
            double dist = self.getTransform().getTranslation().distance(targetPos);
            if (dist > combatRange) return false;
        }
        return true;
    }

    protected double getAimAngle(Brain<?> brain, UniverseModel world) {
        Vector2 targetPos = target.getPosition(world);
        return targetPos != null ? brain.angleToTarget(targetPos)
                : brain.getEntity().getTransform().getRotationAngle();
    }

    protected Projectile createBullet() { return new Projectile(bulletType); }
}