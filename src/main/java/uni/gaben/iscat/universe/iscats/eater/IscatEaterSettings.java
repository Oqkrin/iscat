package uni.gaben.iscat.universe.iscats.eater;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatEaterSettings {
    public static final EntitySettings ISCATEATER = new EntitySettings();

    static {
        ISCATEATER.initLife = 30.0;
        ISCATEATER.dimSprite      = 32.0;
        ISCATEATER.scale          = 2.5;
        ISCATEATER.dampingLineare = 3.0;
        ISCATEATER.maxVelocity    = UniverseVelocitySettings.EATER_MAX_VELOCITY;
        ISCATEATER.force          = 45.0;
        ISCATEATER.xpReward       = 50;
    }

    // Eater-specific: damage dealt on contact, not a "combat" stat
    public static final int ATTACK_POWER = 30;
}
