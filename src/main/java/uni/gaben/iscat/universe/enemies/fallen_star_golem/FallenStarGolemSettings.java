package uni.gaben.iscat.universe.enemies.fallen_star_golem;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class FallenStarGolemSettings {
    public static final EntitySettings FALLENSTARGOLEM = new EntitySettings();

    static {
        FALLENSTARGOLEM.initLife = 250.0;
        FALLENSTARGOLEM.dimSprite      = 64.0;
        FALLENSTARGOLEM.scale          = 2.0;
        FALLENSTARGOLEM.dampingLineare = 3.0;
        FALLENSTARGOLEM.maxVelocity    = UniverseVelocitySettings.GOLEM_MAX_VELOCITY;
        FALLENSTARGOLEM.force          = 15.0;
        FALLENSTARGOLEM.rotationSpeed  = 5.0;
        FALLENSTARGOLEM.xpReward       = 50;

        FALLENSTARGOLEM.detectionRange = 15.0;
        FALLENSTARGOLEM.combatRange    = 10.0;
        FALLENSTARGOLEM.preferredRange = 7.0;
        FALLENSTARGOLEM.fireCooldownS  = 1.2;
    }
}
