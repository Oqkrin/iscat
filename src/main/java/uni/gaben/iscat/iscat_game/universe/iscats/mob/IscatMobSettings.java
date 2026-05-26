package uni.gaben.iscat.iscat_game.universe.iscats.mob;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatMobSettings {
    public static final BaseEntitySettings ISCATMOB = new BaseEntitySettings();

    static {
        ISCATMOB.initLife = 15.0;
        ISCATMOB.dimSprite       = 32.0;
        ISCATMOB.scale           = 2.0;
        ISCATMOB.dampingLineare  = 3.0;
        ISCATMOB.maxVelocity     = VelocitySettings.MOB_MAX_VELOCITY;
        ISCATMOB.force           = 15.0;
        ISCATMOB.rotationSpeed   = 5.0;
        ISCATMOB.xpReward        = 50;

        ISCATMOB.detectionRange  = 15.0;
        ISCATMOB.combatRange     = 10.0;
        ISCATMOB.preferredRange  = 7.0;
        ISCATMOB.fireCooldownS   = 1.2;
    }
}
