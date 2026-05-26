package uni.gaben.iscat.iscat_game.universe.iscats.master;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatMasterSettings {
    public static final BaseEntitySettings ISCATMASTER = new BaseEntitySettings();

    static {
        ISCATMASTER.initLife = 15000.0;
        ISCATMASTER.dimSprite      = 128.0;
        ISCATMASTER.scale          = 5.0;
        ISCATMASTER.dampingLineare = 3.0;
        ISCATMASTER.maxVelocity    = VelocitySettings.ISCAT_MASTER_MAX_VELOCITY;
        ISCATMASTER.force          = 1500.0;
        ISCATMASTER.rotationSpeed  = 0.0;
        ISCATMASTER.xpReward       = 15000;

        ISCATMASTER.detectionRange = 9999.9;
        ISCATMASTER.combatRange    = 100.0;
        ISCATMASTER.preferredRange = 5.0;
        ISCATMASTER.fireCooldownS  = 2.0;
    }
}
