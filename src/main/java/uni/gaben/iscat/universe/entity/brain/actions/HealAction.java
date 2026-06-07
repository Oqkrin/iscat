package uni.gaben.iscat.universe.entity.brain.actions;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.universe.entity.GenericEntityModel;
import java.util.Collections;

public class HealAction extends Action {
    private final Cooldown healCooldown;
    private final Cooldown visualHealCooldown;
    private final double range;
    private final double amount;
    
    public HealAction(double cooldownSec, double range, double amount) {
        super("HealAllies", ActionCategory.ATTACK, Collections.emptySet());
        this.healCooldown = new Cooldown(cooldownSec);
        this.visualHealCooldown = new Cooldown(cooldownSec != 0 ? cooldownSec : 3);
        this.range = range;
        this.amount = amount;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        healCooldown.update(dt);
        visualHealCooldown.update(dt);
        if (healCooldown.isCoolingDown()) return false;
        
        for (LivingEntityModel l : world.getEntitiesOfType(LivingEntityModel.class)) {
            if (l == self || l instanceof PlayerModel) continue;
            if (l.getLife() < l.getMaxLife() &&
                self.getTransform().getTranslation().distance(l.getTransform().getTranslation()) <= range) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractEntityModel entity = brain.getEntity();
        for (LivingEntityModel l : world.getEntitiesOfType(LivingEntityModel.class)) {
            if (l == entity || l instanceof PlayerModel) continue;
            if (entity.getTransform().getTranslation()
                    .distance(l.getTransform().getTranslation()) <= range) {
                l.setLife(Math.min(l.getLife() + amount, l.getMaxLife()));
            }
        }
        healCooldown.start();
        if(visualHealCooldown.isReady()) {
            visualHealCooldown.start();
            if (entity instanceof GenericEntityModel ge) {
                ge.shockwave().trigger(visualHealCooldown.getDefaultDuration(), UU.mToPx(range / 2), UU.mToPx(range) / 10);
            }
        }
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        // Instant action
        return false;
    }
}
