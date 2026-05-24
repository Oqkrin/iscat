package uni.gaben.iscat.iscat_game.universe.enemies.iscat_eater;

import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

public class IscatEaterSettings {
    /** Punti vita iniziali del mob. */
    public static final int HP_INIZIALI = 30;
    /** Dimensione base dello sprite in pixel. */
    public static final int DIM_SPRITE = 32;
    /** Scala di rendering. */
    public static final double SCALE = 2.5;
    /** Raggio della collisione fisica. */
    public static final double DAMPING_LINEARE = 3.0;

    /** Velocità massima in metri al secondo. (25 m/s è molto veloce) */
    public static final double MAX_VELOCITY_MS = VelocitySettings.EATER_MAX_VELOCITY;
    /** Forza massima di sterzata (maggiore = più reattivo). */
    public static final double FORCE = 60;
    /** Se il nemico puo rotarsi verso il player */
    public static final boolean ROTATION_TOWARDS_PLAYER = false;

    public static final int ATTACK_POWER = 30;

    public static final double XP_REWARD = 50.0;
}
