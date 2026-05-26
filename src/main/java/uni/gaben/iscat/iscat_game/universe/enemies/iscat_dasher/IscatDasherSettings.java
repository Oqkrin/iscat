package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatDasherSettings {
    public static final BaseEntitySettings ISCATDASHER = new BaseEntitySettings();

    static {
        ISCATDASHER.initLife = 40.0;
        ISCATDASHER.dimSprite      = 32.0;
        ISCATDASHER.scale          = 3.0;
        ISCATDASHER.dampingLineare = 2.5;
        ISCATDASHER.maxVelocity    = VelocitySettings.EATER_MAX_VELOCITY + VelocitySettings.WORM_HEAD_MAX_SPEED;
        ISCATDASHER.force          = ISCATDASHER.maxVelocity*5;
        ISCATDASHER.rotationSpeed  = 30.0;
        ISCATDASHER.xpReward       = 30;
    }

    // Dasher-specific: tick damage on contact
    public static final double BASE_TICK_DAMAGE = 2.0;
}

