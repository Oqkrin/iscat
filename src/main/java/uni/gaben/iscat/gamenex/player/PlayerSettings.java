package uni.gaben.iscat.gamenex.player;

public final class PlayerSettings {
    private PlayerSettings() {}

    // === Fisica (adattata per Dyn4j) ===
    public static final double MASSA = 1.0;
    // La damping lineare decelera il corpo nel tempo. Un valore più alto = più attrito.
    public static final double LINEAR_DAMPING = 5.0;
    public static final double VELOCITA_MAX = 12.0; // 12 m/s = 768 px/s
    
    // === Movimento ===
    public static final double FORZA_SPINTA = 60.0; // Forza contenuta (in Newton)

    // === Dash ===
    public static final double IMPULSO_SCATTO = 15.0; // Impulso realistico per MKS
    public static final double LINEAR_DAMPING_SCATTO = 1.0; // Meno attrito durante lo scatto
    public static final double VELOCITA_MAX_SCATTO = 35.0;
    
    // Cooldown in secondi per gamenex (invece dei vecchi tick)
    public static final double DURATA_SCATTO_SEC = 0.25; 
    public static final double COOLDOWN_SCATTO_SEC = 1.0; 

    // === Combattimento ===
    public static final int HP_MASSIMO = 100;
    public static final int HP_INIZIALE = 100;

    // === Collisione ===
    public static final double DIMENSIONE_SPRITE = 64.0;
    public static final double RAGGIO_COLLISIONE = 24.0;

    // === Attacco ===
    public static final double COOLDOWN_FUOCO_SEC = 0.16; // Circa 10 tick a 60fps
    public static final double VELOCITA_PROIETTILE = 800.0;
    public static final double DANNO_PROIETTILE = 10.0;

    // === Visual ===
    public static final double HP_BAR_OFFSET_Y = 10.0;
    public static final double HP_BAR_HEIGHT = 4.0;

    // === Thrust Effect ===
    public static final int THRUST_MIN_PARTICLES = 3;
    public static final int THRUST_EXTRA_PARTICLES = 7;
    public static final double THRUST_HEIGHT_FACTOR = 0.65;
    public static final double THRUST_SPREAD_X_FACTOR = 0.15;
    public static final double THRUST_MIN_PARTICLE_SIZE = 2.0;
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 3.0;

    // === Particle Colors ===
    public static final double PARTICLE_CORE_THRESHOLD = 0.3;
    public static final double PARTICLE_MID_THRESHOLD = 0.7;
    public static final double PARTICLE_CORE_BRIGHTNESS = 1.0;
    public static final double PARTICLE_CORE_ALPHA = 0.9;
    public static final double PARTICLE_MID_BRIGHTNESS = 0.9;
    public static final double PARTICLE_MID_ALPHA = 0.8;
    public static final double PARTICLE_TAIL_BRIGHTNESS = 0.5;
    public static final double PARTICLE_TAIL_ALPHA = 0.7;
}
