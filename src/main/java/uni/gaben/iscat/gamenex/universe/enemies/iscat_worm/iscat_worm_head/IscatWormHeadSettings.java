package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head;

public class IscatWormHeadSettings {

    // --- PROPRIETÀ FISICHE ---
    public static final int HP_INIZIALI = 40;
    public static final int DIM_SPRITE = 64;
    public static final int NUMERO_FRAMES = 4;
    public static final double SCALE = 1.8;
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.85;

    public static final double DAMPING_LINEARE = 2.8;

    // --- MOVIMENTO AI ---
    public static final double MAX_VELOCITY_MS = 10;
    public static final double FORCE = 10;           // più forte dell'eater
    public static final double ATTACK_POWER = 25;

    // --- ATTACCO ---
    public static final double ATTACK_RADIUS_MULTIPLIER = 2.8;

    // --- COMPORTAMENTO SERPENTE ---
    public static final double FOLLOW_DISTANCE = 35.0; // distanza ideale dai segmenti che seguono
}