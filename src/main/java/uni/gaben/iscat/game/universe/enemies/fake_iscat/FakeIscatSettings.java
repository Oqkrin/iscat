package uni.gaben.iscat.game.universe.enemies.fake_iscat;

public class FakeIscatSettings {

    public static final double AVOIDANCE_RAY_LEN = 60.0;
    public static final double AVOIDANCE_FORCE = 250.0;

    public static final double DETECTION_RANGE = 15.0;
    public static final double COMBAT_RANGE = 10.0;
    public static final double PREFERRED_RANGE = 7.0;

    public static final double FIRE_COOLDOWN_S = 1.2;
    public static final double ROTATION_SPEED = 5.0;
    public static final double COOLDOWN_FUOCO_SEC = 1.2;

    public static final int PROJECTILE_DAMAGE = 12;

    public static final double SEPARATION_RADIUS_PX = 50.0;
    public static final double SEPARATION_FORCE = 100.0;

    public static final int HP_INIZIALI = 30;
    public static final int DIM_SPRITE = 32;
    public static final int NUMERO_FRAMES = 19;
    public static final double SCALE = 2.0;

    public static final double RAGGIO_COLLISIONE_PX =
            (DIM_SPRITE / 2.0) * 0.9;

    public static final double DAMPING_LINEARE = 3.0;

    public static final double DISTANZA_IDEALE_PX = 75.0;
    public static final double RAMP_UP_PX = 400.0;

    public static final double MAX_VELOCITY_MS = 30;
    public static final double FORCE = 15;
    public static final double STEERING_GAIN = 10.0;

    public static final boolean ROTATION_TOWARDS_PLAYER = true;

    public static final double MIN_DIST_MULT = 0.5;
    public static final double MAX_DIST_MULT = 2.5;

    public static final double ROTATION_STIFFNESS = 150.0;
    public static final double ROTATION_DAMPING = 1.0;
    public static final double AI_ACCURACY = 0.7;

    public static final int COOLDOWN_SPARO_TICKS = 10;

    public static final double DIM_PROIETTILE = 10;
    public static final double VEL_PROIETTILE = 20.0;
}