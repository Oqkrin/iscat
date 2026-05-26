package uni.gaben.iscat.universe.iscats.master;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatMasterSettings {
    public static final EntitySettings ISCATMASTER = new EntitySettings();

    static {
        ISCATMASTER.initLife = 15000.0;
        ISCATMASTER.dimSprite      = 128.0;
        ISCATMASTER.scale          = 5.0;
        ISCATMASTER.dampingLineare = 3.0;
        ISCATMASTER.maxVelocity    = UniverseVelocitySettings.ISCAT_MASTER_MAX_VELOCITY;
        ISCATMASTER.force          = 1500.0;
        ISCATMASTER.rotationSpeed  = 0.0;
        ISCATMASTER.xpReward       = 15000;

        ISCATMASTER.detectionRange = 9999.9;
        ISCATMASTER.combatRange    = 100.0;
        ISCATMASTER.preferredRange = 5.0;
        ISCATMASTER.fireCooldownS  = 2.0;
    }
}
