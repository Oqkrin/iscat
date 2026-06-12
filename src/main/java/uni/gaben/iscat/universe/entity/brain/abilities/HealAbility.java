package uni.gaben.iscat.universe.entity.brain.abilities;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Collections;

public class HealAbility extends Ability {
    private final Cooldown healCooldown;
    private final Cooldown visualHealCooldown;
    private final double range;
    private final double amount;
    
    public HealAbility(double cooldownSec, double range, double amount) {
        super("HealAllies", AbilityCategory.ATTACK, Collections.emptySet());
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
        
        for (AbstractLivingEntityModel l : world.getEntitiesOfType(AbstractLivingEntityModel.class)) {
            if (l == self || l instanceof PlayerModel) continue;
            if (l.getEndurance() < l.getMaxEndurance() &&
                self.getTransform().getTranslation().distance(l.getTransform().getTranslation()) <= range) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        AbstractEntityModel entity = brain.getEntity();
        for (AbstractLivingEntityModel l : world.getEntitiesOfType(AbstractLivingEntityModel.class)) {
            if (l == entity || l instanceof PlayerModel) continue;
            if (entity.getTransform().getTranslation()
                    .distance(l.getTransform().getTranslation()) <= range) {
                l.setEndurance(Math.min(l.getEndurance() + amount, l.getMaxEndurance()));
            }
        }
        healCooldown.start();
        if(visualHealCooldown.isReady()) {
            visualHealCooldown.start();
            if (entity instanceof EntityModel ge) {
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
