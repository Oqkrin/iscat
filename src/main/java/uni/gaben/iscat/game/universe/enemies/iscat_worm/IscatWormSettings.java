package uni.gaben.iscat.game.universe.enemies.iscat_worm;

public class IscatWormSettings {
    public static final int    INITIAL_SEGMENTS     = 11;
    public static final double DIM_SPRITE           = 64.0;

    // --- DISTANZE FISICHE (Sincronizzate) ---
    // Distanza di riposo tra i segmenti (espressa in pixel)
    public static final double FOLLOW_DISTANCE_PX   = 38.0;

    // --- IMPOSTAZIONI TESTA (HEAD) ---
    public static final int    HEAD_HP              = 65;
    public static final double HEAD_SCALE           = 2.0;

    // Incrementate le forze fisiche per vincere l'inerzia e scattare di colpo
    public static final double HEAD_MAX_SPEED       = 18.0;   // Prima era 10
    public static final double HEAD_FORCE           = 35.0;   // Spinta incrementata (prima era 10)

    // Velocità di rotazione (Alpha dell'interpolatore per la testa)
    public static final double HEAD_ROTATION_SPEED  = 0.35;   // Più del doppio più veloce a girarsi (era 0.17 implicitamente)

    // --- ATTACCO ---
    // Raggio d'attacco aumentato per compensare l'alta velocità ed evitare che lo manchi passandogli sopra
    public static final double HEAD_ATTACK_RADIUS   = 55.0;
    public static final int    HEAD_ATTACK_POWER    = 25;
    public static final double HEAD_ATTACK_COOLDOWN = 1.2;    // Attacchi leggermente più rapidi

    // --- IMPOSTAZIONI CORPO (BODY) ---
    public static final int    BODY_HP              = 28;
    public static final double BODY_SCALE           = 2.0;
    public static final double BODY_FOLLOW_FORCE    = 25.0;   // Aumentata per seguire la testa senza sfilacciarsi

    // --- IMPOSTAZIONI CODA (TAIL) ---
    public static final int    TAIL_HP              = 22;
    public static final double TAIL_SCALE           = 2.0;
    public static final double TAIL_FOLLOW_FORCE    = 25.0;   // Aumentata per fluidità complessiva

    public static final int SEGMENT_SPACING_PX = 0;

}