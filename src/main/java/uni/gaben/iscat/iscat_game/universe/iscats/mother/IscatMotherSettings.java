package uni.gaben.iscat.iscat_game.universe.iscats.mother;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public final class IscatMotherSettings {
    private IscatMotherSettings() {}

    public static final BaseEntitySettings ISCATMOTHER = new BaseEntitySettings();

    static {
        ISCATMOTHER.initLife = 500.0;
        ISCATMOTHER.dimSprite      = 128.0;
        ISCATMOTHER.scale          = 4.0;
        ISCATMOTHER.dampingLineare = 3.0;
        ISCATMOTHER.maxVelocity    = VelocitySettings.MOTHER_MAX_VELOCITY;
        ISCATMOTHER.force          = 13.0;
        ISCATMOTHER.rotationSpeed  = 5.0;
        ISCATMOTHER.xpReward       = 50;

        ISCATMOTHER.detectionRange = 15.0;
        ISCATMOTHER.fireCooldownS  = 1.0;
    }

    // Mother-specific constants
    public static final double DISTANZA_IDEALE_M           = UU.pxToM(250.0);
    public static final double DISTANZA_TOLLERANZA_VICINO  = UU.pxToM(20.0);
    public static final double DISTANZA_TOLLERANZA_LONTANO = UU.pxToM(30.0);
    public static final double COMBAT_RANGE_MIN            = UU.pxToM(90.0);
    public static final double COMBAT_RANGE_MAX            = UU.pxToM(420.0);
    public static final double SPREAD_ANGLE_DEG            = 15.0;

    public static final double MINION_SPAWN_HP_THRESHOLD   = 0.5;
    public static final int    MINION_ISCAT_COUNT          = 5;
    public static final int    MINION_EATER_COUNT          = 2;
    public static final double MINION_SPAWN_RADIUS         = UU.pxToM(80.0);

    public static final int    HORDE_SIZE                  = 40;
    public static final double HORDE_RADIUS                = UU.pxToM(130.0);
    public static final double HORDE_RADIUS_VARIANCE       = UU.pxToM(60.0);
}
