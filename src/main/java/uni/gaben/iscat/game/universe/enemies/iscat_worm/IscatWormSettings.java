package uni.gaben.iscat.game.universe.enemies.iscat_worm;

import uni.gaben.iscat.game.universe.VelocitySettings;

public class IscatWormSettings {
    public static final int    INITIAL_SEGMENTS     = 11;
    public static final double DIM_SPRITE           = 64.0;

    // --- DISTANZE FISICHE BASE ---
    // Distanza base di 50 pixel che verrà poi moltiplicata per la scala
    public static final double FOLLOW_DISTANCE_PX   = 50.0;

    // Spaziatura di spawn iniziale (moltiplicata per la scala della testa per coerenza)
    public static final int    SEGMENT_SPACING_PX   = (int)(50.0 * 2.0);

    // --- IMPOSTAZIONI TESTA (HEAD) ---
    public static final int    HEAD_HP              = 65;
    public static final double HEAD_SCALE           = 2.0;
    public static final double HEAD_MAX_SPEED       = VelocitySettings.WORM_HEAD_MAX_SPEED;
    public static final double HEAD_FORCE           = 800.0;
    public static final double HEAD_ROTATION_SPEED  = 0.45;

    // --- ATTACCO ---
    public static final double HEAD_ATTACK_RADIUS   = 55.0;
    public static final int    HEAD_ATTACK_POWER    = 25;
    public static final double HEAD_ATTACK_COOLDOWN = 1.2;

    // --- IMPOSTAZIONI CORPO (BODY) ---
    public static final int    BODY_HP              = 28;
    public static final double BODY_SCALE           = 2.0;
    public static final double BODY_FOLLOW_FORCE    = 950.0;

    // --- IMPOSTAZIONI CODA (TAIL) ---
    public static final int    TAIL_HP              = 22;
    public static final double TAIL_SCALE           = 2.0;
    public static final double TAIL_FOLLOW_FORCE    = 950.0;

    public static final double XP_REWARD = 50.0;
}