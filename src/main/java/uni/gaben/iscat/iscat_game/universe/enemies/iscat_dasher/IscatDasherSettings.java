package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;

public class IscatDasherSettings {
    public static final BaseEntitySettings ISCATDASHER = new BaseEntitySettings();

    static {
        ISCATDASHER.initLife = 40.0;
        ISCATDASHER.dimSprite      = 64.0;
        ISCATDASHER.scale          = 1.0;
        ISCATDASHER.dampingLineare = 2.5;
        ISCATDASHER.maxVelocity    = 15.0;
        ISCATDASHER.force          = 35.0;
        ISCATDASHER.rotationSpeed  = 8.0;
        ISCATDASHER.xpReward       = 15;
    }

    // Dasher-specific: tick damage on contact
    public static final double BASE_TICK_DAMAGE = 2.0;
}

