package uni.gaben.iscat.gamenex.universe.eater;

public class EaterSettings {
    /** Lunghezza del raggio per vedere i muri. */
    public static final double AVOIDANCE_RAY_LEN = 60.0;
    /** Forza per sterzare via dai muri (deve essere alta). */
    public static final double AVOIDANCE_FORCE = 250.0;

    // --- LOGICA DI SQUADRA (New) ---
    /** Raggio della "bolla personale" del mob. */
    public static final double SEPARATION_RADIUS_PX = 50.0;
    /** Forza di repulsione tra mob. */
    public static final double SEPARATION_FORCE = 100.0;
    // --- PROPRIETÀ FISICHE ---
    /** Punti vita iniziali del mob. */
    public static final int HP_INIZIALI = 15;
    /** Dimensione base dello sprite in pixel. */
    public static final double DIM_SPRITE = 32.0;
    /** Numero di frame dell'animazione. */
    public static final int NUMERO_FRAMES = 2;
    /** Scala di rendering. */
    public static final double SCALE = 2;
    /** Raggio della collisione fisica. */
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;
    /** Attrito lineare: valori bassi permettono più scivolamento e inerzia. Default consigliato: 2.0 */
    public static final double DAMPING_LINEARE = 3.0;

    // --- MOVIMENTO AI (ChaseBehavior) ---
    /** Distanza minima dal player prima che il mob inizi ad allontanarsi. */
    public static final double DISTANZA_IDEALE_PX = 75.0;
    /** Spazio di frenata/accelerazione per rendere il movimento fluido. */
    public static final double RAMP_UP_PX = 400.0;
    /** Velocità massima in metri al secondo. (25 m/s è molto veloce) */
    public static final double MAX_VELOCITY_MS = 30;
    /** Forza massima di sterzata (maggiore = più reattivo). */
    public static final double FORCE = 15;
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

    public static final int ATTACK_POWER = 30;
}
