package uni.gaben.iscat.game.universe.enemies.iscat_master;

import uni.gaben.iscat.game.universe.VelocitySettings;

public class IscatMasterSettings {

    // ── RILEVAMENTO E RANGE ────────────────────────────────────────────────────
    public static final double DETECTION_RANGE   = 150;
    public static final double COMBAT_RANGE      = 100;
    public static final double PREFERRED_RANGE   = 100;

    // ── MOVIMENTO ─────────────────────────────────────────────────────────────
    public static final double FORCE             = 50.0;
    public static final double MAX_VELOCITY      = VelocitySettings.ISCAT_MASTER_MAX_VELOCITY;
    public static final double ROTATION_SPEED    = 0;
    public static final double DAMPING_LINEARE   = 3.0;

    // ── COMBATTIMENTO ─────────────────────────────────────────────────────────
    public static final double FIRE_COOLDOWN_S   = 0.5;

    // ── SPRITE ────────────────────────────────────────────────────────────────
    public static final int    HP_INIZIALI       = 10000;
    public static final int    DIM_SPRITE        = 128;
    public static final int    NUMERO_FRAMES     = 4;
    public static final double SCALE             = 5.0;

    // ── RICOMPENSE ────────────────────────────────────────────────────────────
    public static final double XP_REWARD         = 5000.0;
}