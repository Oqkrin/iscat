package uni.gaben.iscat.game.universe.enemies.iscat_worm;

public class IscatWormSettings {
    public static final int    INITIAL_SEGMENTS     = 11;
    public static final double SEGMENT_SPACING_PX   = 42.0;
    public static final double DIM_SPRITE           = 64.0;   // era mancante
    public static final double HEAD_ATTACK_RADIUS   = 30.0;   // era mancante, in px

    public static final int    HEAD_HP              = 65;
    public static final double HEAD_SCALE           = 2.0;
    public static final double HEAD_MAX_SPEED       = 10;
    public static final double HEAD_FORCE           = 10;
    public static final int    HEAD_ATTACK_POWER    = 25;
    public static final double HEAD_ATTACK_COOLDOWN = 1.6;

    public static final int    BODY_HP              = 28;
    public static final double BODY_SCALE           = 2.0;
    public static final double BODY_FOLLOW_FORCE    = 10;

    public static final int    TAIL_HP              = 22;
    public static final double TAIL_SCALE           = 2.0;
    public static final double TAIL_FOLLOW_FORCE    = 10;

    public static final double FOLLOW_DISTANCE_PX   = 9.0;
}