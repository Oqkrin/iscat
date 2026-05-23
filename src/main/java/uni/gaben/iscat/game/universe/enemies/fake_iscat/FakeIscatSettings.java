package uni.gaben.iscat.game.universe.enemies.fake_iscat;

import uni.gaben.iscat.game.universe.VelocitySettings;

public class FakeIscatSettings {

    // ── RILEVAMENTO E RANGE ────────────────────────────────────────────────────
    public static final double DETECTION_RANGE   = 15.0;
    public static final double COMBAT_RANGE      = 10.0;
    public static final double PREFERRED_RANGE   = 7.0;

    // ── MOVIMENTO ─────────────────────────────────────────────────────────────
    public static final double FORCE             = 15.0;
    public static final double MAX_VELOCITY      = VelocitySettings.FAKE_ISCAT_MAX_VELOCITY;
    public static final double ROTATION_SPEED    = 0;
    public static final double DAMPING_LINEARE   = 3.0;

    // ── COMBATTIMENTO ─────────────────────────────────────────────────────────
    public static final double FIRE_COOLDOWN_S   = 3.5;

    // ── SPRITE ────────────────────────────────────────────────────────────────
    public static final int    HP_INIZIALI       = 30;
    public static final int    DIM_SPRITE        = 32;
    public static final double SCALE             = 2.0;

    // ── RICOMPENSE ────────────────────────────────────────────────────────────
    public static final double XP_REWARD         = 50.0;
}
