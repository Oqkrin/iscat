package uni.gaben.iscat.iscat_game.universe.enemies.iscat_master;

import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatMasterSettings {

    // ── RILEVAMENTO E RANGE ────────────────────────────────────────────────────
    public static final double DETECTION_RANGE   = 9999.9;
    public static final double COMBAT_RANGE      = 100.0;
    public static final double PREFERRED_RANGE   = 5.0;

    // ── MOVIMENTO ─────────────────────────────────────────────────────────────
    public static final double FORCE             = 1500.0;
    public static final double MAX_VELOCITY      = VelocitySettings.ISCAT_MASTER_MAX_VELOCITY;
    public static final double ROTATION_SPEED    = 0;
    public static final double DAMPING_LINEARE   = 3.0;

    // ── COMBATTIMENTO ─────────────────────────────────────────────────────────
    public static final double FIRE_COOLDOWN_S   = 2.0;

    // ── SPRITE ────────────────────────────────────────────────────────────────
    public static final int    HP_INIZIALI       = 15000;
    public static final int    DIM_SPRITE        = 128;
    public static final double SCALE             = 5.0;

    // ── RICOMPENSE ────────────────────────────────────────────────────────────
    public static final double XP_REWARD         = 15000.0;
}