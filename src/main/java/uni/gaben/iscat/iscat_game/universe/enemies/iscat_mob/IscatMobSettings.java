package uni.gaben.iscat.iscat_game.universe.enemies.iscat_mob;

import uni.gaben.iscat.iscat_game.universe.VelocitySettings;

/**
 * Impostazioni centralizzate per l'IscatMob.
 * Definisce le costanti fisiche e i parametri dell'intelligenza artificiale.
 */
public class IscatMobSettings {
    public static final double DETECTION_RANGE  = 15.0;  // metri — vede il player
    public static final double COMBAT_RANGE     = 10.0;  // metri — entra in modalità combattimento
    public static final double PREFERRED_RANGE  =  7.0;  // metri — distanza ideale di fuoco
    public static final double FIRE_COOLDOWN_S  =  1.2;  // secondi tra un colpo e l'altro
    public static final double ROTATION_SPEED   =  5.0;  // velocità rotazione verso il player
    public static final double COOLDOWN_FUOCO_SEC = 1.2;   // tempo tra uno sparo e l'altro
    // --- PROPRIETÀ FISICHE ---
    /** Punti vita iniziali del mob. */
    public static final int HP_INIZIALI = 15;
    /** Dimensione base dello sprite in pixel. */
    public static final int DIM_SPRITE = 32;
    /** Scala di rendering. */
    public static final double SCALE = 2.0;
    /** Attrito lineare: valori bassi permettono più scivolamento e inerzia. Default consigliato: 2.0 */
    public static final double DAMPING_LINEARE = 3.0;

    // --- MOVIMENTO AI (ChaseBehavior) ---
    /** Velocità massima in metri al secondo. (25 m/s è molto veloce) */
    public static final double MAX_VELOCITY_MS = VelocitySettings.MOB_MAX_VELOCITY;
    /** Forza massima di sterzata (maggiore = più reattivo). */
    public static final double FORCE = 15;


    public static final double XP_REWARD = 50.0;
}
