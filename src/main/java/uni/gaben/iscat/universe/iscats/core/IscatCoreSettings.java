package uni.gaben.iscat.universe.iscats.core;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatCoreSettings {
    public static final EntitySettings ISCATCORE = new EntitySettings();

    static {
        ISCATCORE.initLife = 500.0;
        ISCATCORE.dimSprite      = 64.0;
        ISCATCORE.scale          = 2.0;
        ISCATCORE.dampingLineare = 3.0;
        ISCATCORE.maxVelocity    = UniverseVelocitySettings.CORE_MAX_VELOCITY;
        ISCATCORE.force          = 15.0;
        ISCATCORE.rotationSpeed  = 1.5;
        ISCATCORE.xpReward       = 50;

        ISCATCORE.detectionRange = 15.0;
        ISCATCORE.combatRange    = 10.0;
        ISCATCORE.preferredRange = 7.0;
        ISCATCORE.fireCooldownS  = 1.2;
    }

    // Core-specific: controls its rotation snapping interval and bullet spread
    public static final double ROTATION_INTERVAL = 10.0;
    public static final double BULLET_SPACING_M  = UU.pxToM(24.0);
}
