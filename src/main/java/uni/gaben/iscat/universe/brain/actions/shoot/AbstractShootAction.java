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
import java.util.function.Function;

public abstract class AbstractShootAction extends Action {
    protected final Cooldown cooldown;
    protected final double combatRange;          // < 0 = unlimited
    protected final ProjectileType bulletType;
    protected final Function<UniverseModel, Vector2> targetSupplier;

    public AbstractShootAction(String name, double combatRange, double cooldownSec,
                               ProjectileType bulletType,
                               Function<UniverseModel, Vector2> targetSupplier) {
        super(name, ActionCategory.ATTACK, Set.of());
        this.combatRange = combatRange;
        this.cooldown = new Cooldown(cooldownSec);
        this.bulletType = bulletType;
        this.targetSupplier = targetSupplier;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        cooldown.update(dt);
        if (cooldown.isCoolingDown()) return false;

        Vector2 target = targetSupplier.apply(world);
        if (target == null) return false;

        if (combatRange >= 0) {
            double dist = self.getTransform().getTranslation().distance(target);
            if (dist > combatRange) return false;
        }
        return true;
    }

    /** Helper for subclasses: returns the current aim angle. */
    protected double getAimAngle(Brain<?> brain, UniverseModel world) {
        Vector2 target = targetSupplier.apply(world);
        return target != null ? brain.angleToTarget(target)
                : brain.getEntity().getTransform().getRotationAngle();
    }

    /** Helper for subclasses: creates a projectile of the configured type. */
    protected Projectile createBullet() {
        return new Projectile(bulletType);
    }
}