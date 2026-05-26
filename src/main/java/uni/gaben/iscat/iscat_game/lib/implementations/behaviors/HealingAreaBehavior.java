package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.utils.Cooldown;

public class HealingAreaBehavior implements AiBehavior {

    private final double healingRadius;
    private final double healingAmount;
    private final Cooldown pulseCooldown = new Cooldown();
    
    public HealingAreaBehavior(double healingRadius, double healingAmount, double cooldownSeconds) {
        this.healingRadius = healingRadius;
        this.healingAmount = healingAmount;
        this.pulseCooldown.start(cooldownSeconds); // start cooled down or wait? Let's use start to wait initially
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        // Runs passively without overriding movement (returns 0 or low) if it just pulses
        return -1;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        pulseCooldown.update(dt);
        if (!pulseCooldown.isCoolingDown()) {
            Vector2 npcPos = npc.getTransform().getTranslation();
            
            for (AbstractEntityModel e : universe.getEntitiesOfType(LivingEntityModel.class)) {
                if (e instanceof LivingEntityModel living && living != universe.getPlayer()) {
                    if (living.getTransform().getTranslation().distance(npcPos) <= healingRadius) {
                        // Cura l'alleato
                        if (living.getLife() < living.getMaxLife()) {
                            living.deltaToLife(healingAmount);
                            // Se supera la vita max viene clippato da deltaToLife o in update?
                            if (living.getLife() > living.getMaxLife()) {
                                // Clamp manually just in case
                                living.deltaToLife(living.getMaxLife() - living.getLife());
                            }
                        }
                    }
                }
            }
            pulseCooldown.start(3.0);
        }
    }
}
