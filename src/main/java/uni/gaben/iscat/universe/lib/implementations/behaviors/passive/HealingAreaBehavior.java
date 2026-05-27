package uni.gaben.iscat.universe.lib.implementations.behaviors.passive;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.utils.Cooldown;

/**
 * Periodically heals all nearby allied living entities within a given radius.
 * Runs as a {@link PassiveBehavior} — always active regardless of movement/attack state.
 */
public class HealingAreaBehavior implements PassiveBehavior {

    private final double   healingRadius;
    private final double   healingAmount;
    private final double   pulseInterval;
    private final Cooldown pulseCooldown = new Cooldown();

    public HealingAreaBehavior(double healingRadius, double healingAmount, double pulseIntervalSeconds) {
        this.healingRadius = healingRadius;
        this.healingAmount = healingAmount;
        this.pulseInterval = pulseIntervalSeconds;
        this.pulseCooldown.start(pulseIntervalSeconds);
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        pulseCooldown.update(dt);
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (pulseCooldown.isCoolingDown()) return;

        Vector2 npcPos = npc.getTransform().getTranslation();

        for (AbstractEntityModel e : universe.getEntitiesOfType(LivingEntityModel.class)) {
            if (!(e instanceof LivingEntityModel living)) continue;
            if (living == universe.getPlayer()) continue;
            if (living.getTransform().getTranslation().distance(npcPos) > healingRadius) continue;
            if (living.getLife() >= living.getMaxLife()) continue;

            double overheal = Math.max(0, living.getLife() + healingAmount - living.getMaxLife());
            living.deltaToLife(healingAmount - overheal);
        }

        pulseCooldown.start(pulseInterval);
    }
}
