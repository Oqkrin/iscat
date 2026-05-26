package uni.gaben.iscat.iscat_game.universe.iscats.eater;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatEaterSettings {
    public static final BaseEntitySettings ISCATEATER = new BaseEntitySettings();

    static {
        ISCATEATER.initLife = 30.0;
        ISCATEATER.dimSprite      = 32.0;
        ISCATEATER.scale          = 2.5;
        ISCATEATER.dampingLineare = 3.0;
        ISCATEATER.maxVelocity    = VelocitySettings.EATER_MAX_VELOCITY;
        ISCATEATER.force          = 45.0;
        ISCATEATER.xpReward       = 50;
    }

    // Eater-specific: damage dealt on contact, not a "combat" stat
    public static final int ATTACK_POWER = 30;
}
