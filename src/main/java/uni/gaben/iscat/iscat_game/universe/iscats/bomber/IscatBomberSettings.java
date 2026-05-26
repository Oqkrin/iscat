package uni.gaben.iscat.iscat_game.universe.iscats.bomber;

import uni.gaben.iscat.iscat_game.lib.abstracts.BaseEntitySettings;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

/**
 * Impostazioni centralizzate per l'IscatBomber.
 */
public final class IscatBomberSettings {
    private IscatBomberSettings() { }

    public static final BaseEntitySettings ISCATBOMBER = new BaseEntitySettings();

    static {
        ISCATBOMBER.initLife = 100.0;
        ISCATBOMBER.dimSprite        = 32.0;
        ISCATBOMBER.scale            = 4.0;
        ISCATBOMBER.dampingLineare   = 3.0;
        ISCATBOMBER.maxVelocity      = VelocitySettings.BOMBER_MAX_VELOCITY;
        ISCATBOMBER.force            = 40.0;
        ISCATBOMBER.xpReward         = 100;
    }

    // Bomber-specific constants (no BaseEntitySettings equivalent)
    public static final int    LUNGHEZZA_TRAIL        = 120;
    public static final int    RITARDO_TRAIL          = 40;
    public static final double DISTANZA_MIN_INSEGUIMENTO = 10.0;
    public static final double SMOOTHING_ROTAZIONE    = 0.12;
    public static final double DURATA_STORDIMENTO_SEC = 0.5;
}
