package uni.gaben.iscat.game.components.entities.player;

public final class PlayerSettings {
    private PlayerSettings() {
    }

    // === Fisica ===
    /**
     * Massa del giocatore (kg unità di gioco)
     */
    public static final double MASSA = 1.0;

    /**
     * Attrito normale (0-1, più basso = più attrito)
     */
    public static final double ATTRITO = 0.92;

    /**
     * Velocità massima normale (px/tick)
     */
    public static final double VELOCITA_MAX = 9.0;

    /**
     * Soglia dead-zone per fermare micro-movimenti
     */
    public static final double ZONA_MORTA = 0.01;

    // === Movimento ===
    /**
     * Forza di spinta per movimento
     */
    public static final double FORZA_SPINTA = 4.0;

    // === Dash ===
    /**
     * Impulso istantaneo del dash
     */
    public static final double IMPULSO_SCATTO = 15.0;

    /**
     * Attrito ridotto durante dash
     */
    public static final double ATTRITO_SCATTO = 0.98;

    /**
     * Velocità massima durante dash
     */
    public static final double VELOCITA_MAX_SCATTO = 20.0;

    /**
     * Durata fase dash (tick)
     */
    public static final int DURATA_SCATTO_TICK = 15;

    /**
     * Cooldown dash (tick)
     */
    public static final int COOLDOWN_SCATTO_TICK = 60;

    // === Combattimento ===
    /**
     * HP massimo
     */
    public static final int HP_MASSIMO = 100;

    /**
     * HP iniziale
     */
    public static final int HP_INIZIALE = 100;

    /**
     * Durata invulnerabilità dopo danno (tick)
     */
    public static final int DURATA_INVULNERABILITA = 60;

    // === Collisione ===
    /**
     * Raggio collisione (px)
     */
    public static final double RAGGIO_COLLISIONE = 24.0;

    /**
     * Dimensione sprite (px)
     */
    public static final double DIMENSIONE_SPRITE = 64.0;

    // === Attacco ===
    public static final int COOLDOWN_FUOCO_TICK = 10; // Un colpo ogni 10 frame (6 al secondo a 60fps)
    public static final double VELOCITA_PROIETTILE = 15.0;
    public static final double DANNO_PROIETTILE = 10.0;
}

