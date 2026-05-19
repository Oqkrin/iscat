package uni.gaben.iscat.game.universe.enemies.iscat_worm.iscat_worm_tail;

public class IscatWormTailSettings {

    // --- PROPRIETÀ VISIVE ---
    public static final int DIM_SPRITE = 64;
    public static final int NUMERO_FRAMES = 4;
    public static final double SCALE = 1.65;
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.75;
    public static final int HP_INIZIALI = 20;

    // --- FISICA ---
    public static final double DAMPING_LINEARE = 4.5;
    public static final double MAX_VELOCITY_MS = 60;

    // --- FOLLOW BEHAVIOUR ---
    public static final double FOLLOW_DISTANCE = 16.0;     // distanza ideale dal segmento precedente
    public static final double FOLLOW_FORCE = 1;
    public static final double ROTATION_SPEED = 0.25;        // coda più "floscia" (meno reattiva)
}