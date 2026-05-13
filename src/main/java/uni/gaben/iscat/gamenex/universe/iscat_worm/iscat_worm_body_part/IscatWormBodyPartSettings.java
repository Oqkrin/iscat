package uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part;

public class IscatWormBodyPartSettings {

    // --- PROPRIETÀ VISIVE ---
    public static final int DIM_SPRITE = 64;
    public static final int NUMERO_FRAMES = 4;
    public static final double SCALE = 1.7;
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.75;

    public static final int HP_INIZIALI = 20;

    // --- FISICA ---
    public static final double DAMPING_LINEARE = 4.5;
    public static final double MAX_VELOCITY_MS = 10;

    // --- FOLLOW BEHAVIOUR ---
    public static final double FOLLOW_DISTANCE = 32.0;     // distanza ideale dal segmento precedente
    public static final double FOLLOW_FORCE = 10;
    public static final double ROTATION_SPEED = 0.22;     // quanto è reattivo nel seguire la rotazione
}