package uni.gaben.iscat.universe.entity.brain.abilities;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.utils.AudioManager;

public class HealOnCollisionAbility extends Ability {

    private final double healAmount;
    private boolean initialized = false;
    private boolean collected = false;

    public HealOnCollisionAbility(double healAmount) {
        super("HealOnCollision", AbilityCategory.SPECIAL, java.util.Collections.emptySet());
        this.healAmount = healAmount;
    }

    @Override
    public boolean canActivate(GameEntity self, UniverseModel world, double dt) {
        return !initialized;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        GameEntity self = brain.getEntity();
        initialized = true;

        self.setOnCollision(other -> {
            if (collected) return;
            if (other.getRecord().identity().entityKey().contains("player")) {
                if (other.hasModule(EnduranceModule.class)) {
                    other.getModule(EnduranceModule.class).alter(healAmount);
                    AudioManager.getInstance().playSFX("powerup");
                    collected = true;
                    self.setShouldRemove(true);
                }
            }
        });
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }
}
