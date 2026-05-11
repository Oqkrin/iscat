package uni.gaben.iscat.gamenex.universe.iscat_mob;

/**
 * Impostazioni centralizzate per l'IscatMob.
 * Definisce le costanti fisiche e i parametri dell'intelligenza artificiale.
 */
public class IscatMobSettings {
    // --- PROPRIETÀ FISICHE ---
    /** Punti vita iniziali del mob. */
    public static final int HP_INIZIALI = 30;
    /** Dimensione base dello sprite in pixel. */
    public static final double DIM_SPRITE = 32.0;
    /** Numero di frame dell'animazione. */
    public static final int NUMERO_FRAMES = 1;
    /** Scala di rendering. */
    public static final double SCALE = 1.5;
    /** Raggio della collisione fisica. */
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;
    /** Attrito lineare: valori bassi permettono più scivolamento e inerzia. Default consigliato: 2.0 */
    public static final double DAMPING_LINEARE = 3.0;

    // --- MOVIMENTO AI (ChaseBehavior) ---
    /** Distanza minima dal player prima che il mob inizi ad allontanarsi. */
    public static final double DISTANZA_IDEALE_PX = 150.0;
    /** Spazio di frenata/accelerazione per rendere il movimento fluido. */
    public static final double RAMP_UP_PX = 400.0;
    /** Velocità massima in metri al secondo. (25 m/s è molto veloce) */
    public static final double MAX_VELOCITY_MS = 25.0;
    /** Forza massima di sterzata (maggiore = più reattivo). */
    public static final double MAX_FORCE = 150.0;
    /** Guadagno di sterzata per correggere la traiettoria. */
    public static final double STEERING_GAIN = 10.0;

    // --- PARAMETRI DINAMICI (Speed Scaling) ---
    /** Moltiplicatore minimo di velocità (quando il mob è molto vicino). */
    public static final double MIN_DIST_MULT = 0.5;
    /** Moltiplicatore massimo di velocità (quando il mob deve recuperare terreno). */
    public static final double MAX_DIST_MULT = 2.5;

    // --- ROTAZIONE AI (LookAtBehavior) ---
    /** Rigidità della rotazione verso il target. */
    public static final double ROTATION_STIFFNESS = 150.0;
    /** Smorzamento della rotazione per evitare oscillazioni. */
    public static final double ROTATION_DAMPING = 1.0;
    /** Precisione della rotazione: 0.5 crea un leggero jitter/wobble. */
    public static final double AI_ACCURACY = 0.7;

    // --- COMBATTIMENTO ---
    /** Ritardo tra un colpo e l'altro. */
    public static final int COOLDOWN_SPARO_TICKS = 10;
    /** Dimensione del proiettile sparato. */
    public static final double DIM_PROIETTILE = 10;
    /** Velocità del proiettile. */
    public static final double VEL_PROIETTILE = 20.0;
}
