package uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;

public class IscatHealerSettings {
    public static final BaseEntitySettings ISCATHEALER = new BaseEntitySettings();

    static {
        ISCATHEALER.initLife = 60.0;
        ISCATHEALER.dimSprite      = 32.0;
        ISCATHEALER.scale          = 2.0;
        ISCATHEALER.dampingLineare = 3.0;
        ISCATHEALER.maxVelocity    = 6.0;
        ISCATHEALER.force          = 20.0;
        ISCATHEALER.rotationSpeed  = 4.0;
        ISCATHEALER.xpReward       = 20;
    }

    // Healer-specific ability parameters
    public static final double HEAL_AMOUNT    = 5.0;
    public static final double HEAL_RADIUS_M  = 15.0;
    public static final double HEAL_COOLDOWN_S = 3.0;
}

