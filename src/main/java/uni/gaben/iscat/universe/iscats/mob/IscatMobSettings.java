package uni.gaben.iscat.universe.iscats.mob;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatMobSettings {
    public static final EntitySettings ISCATMOB = new EntitySettings();

    static {
        ISCATMOB.initLife = 15.0;
        ISCATMOB.dimSprite       = 32.0;
        ISCATMOB.scale           = 2.0;
        ISCATMOB.dampingLineare  = 3.0;
        ISCATMOB.maxVelocity     = UniverseVelocitySettings.MOB_MAX_VELOCITY;
        ISCATMOB.force           = 15.0;
        ISCATMOB.rotationSpeed   = 5.0;
        ISCATMOB.xpReward        = 50;

        ISCATMOB.detectionRange  = 15.0;
        ISCATMOB.combatRange     = 10.0;
        ISCATMOB.preferredRange  = 7.0;
        ISCATMOB.fireCooldownS   = 1.2;
    }
}
