package uni.gaben.iscat.game.enemies.iscat_bomber;

public final class IscatBomberSettings {
    private IscatBomberSettings() {
    }

    // === Fisica ===
    /**
     * Fattore massa rispetto al giocatore
     */
    public static final double FATTORE_MASSA = 8.0;

    /**
     * Attrito
     */
    public static final double ATTRITO = 0.95;

    /**
     * Fattore velocità massima rispetto al giocatore
     */
    public static final double FATTORE_VELOCITA_MAX = 0.8;

    // === AI ===
    /**
     * Lunghezza trail posizioni giocatore
     */
    public static final int LUNGHEZZA_TRAIL = 120;

    /**
     * Ritardo trail (tick indietro da seguire)
     */
    public static final int RITARDO_TRAIL = 40;

    /**
     * Velocità inseguimento
     */
    public static final double VELOCITA_INSEGUIMENTO = 80.0;

    /**
     * Distanza minima per inseguire
     */
    public static final double DISTANZA_MIN_INSEGUIMENTO = 10.0;

    /**
     * Fattore smoothing rotazione (0-1)
     */
    public static final double SMOOTHING_ROTAZIONE = 0.12;

    // === Collisione ===
    /**
     * Forza rimbalzo collisione
     */
    public static final double FORZA_RIMBALZO = 300.0;

    /**
     * Durata stordimento dopo collisione (tick)
     */
    public static final int DURATA_STORDIMENTO = 30;

    /**
     * Cooldown collisione per evitare ripetizioni (tick)
     */
    public static final int COOLDOWN_COLLISIONE = 10;

    /**
     * Raggio collisione (px)
     */
    public static final double RAGGIO_COLLISIONE = 24.0;

    /**
     * Dimensione sprite (px)
     */
    public static final double DIMENSIONE_SPRITE = 64.0;

    // === Combattimento ===
    /**
     * HP
     */
    public static final int HP = 100;

    /**
     * Valore in punti quando ucciso
     */
    public static final int VALORE_PUNTI = 100;
}
