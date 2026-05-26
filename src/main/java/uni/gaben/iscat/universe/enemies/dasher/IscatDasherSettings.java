package uni.gaben.iscat.universe.enemies.dasher;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatDasherSettings {
    public static final EntitySettings ISCATDASHER = new EntitySettings();

    static {
        ISCATDASHER.initLife = 40.0;
        ISCATDASHER.dimSprite      = 32.0;
        ISCATDASHER.scale          = 3.0;
        ISCATDASHER.dampingLineare = 2.5;
        ISCATDASHER.maxVelocity    = UniverseVelocitySettings.EATER_MAX_VELOCITY + UniverseVelocitySettings.WORM_HEAD_MAX_SPEED;
        ISCATDASHER.force          = ISCATDASHER.maxVelocity*5;
        ISCATDASHER.rotationSpeed  = 30.0;
        ISCATDASHER.xpReward       = 30;
    }

    // Dasher-specific: tick damage on contact
    public static final double BASE_TICK_DAMAGE = 2.0;
}

