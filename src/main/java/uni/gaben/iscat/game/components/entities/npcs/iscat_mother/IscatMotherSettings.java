package uni.gaben.iscat.game.components.entities.npcs.iscat_mother;

public final class IscatMotherSettings {
    private IscatMotherSettings() {
    }

    // === Fisica ===
    /**
     * Massa (kg unità di gioco). Molto più pesante del giocatore (1.0).
     */
    public static final double MASSA = 20.0;

    /**
     * Attrito (0-1, più basso = più attrito)
     */
    public static final double ATTRITO = 0.95;

    /**
     * Velocità massima (px/tick)
     */
    public static final double VELOCITA_MAX = 5.0; // lenta e imponente

    // === AI ===
    /**
     * Lunghezza trail posizioni giocatore
     */
    public static final int LUNGHEZZA_TRAIL = 120;

    /**
     * Ritardo trail (tick indietro da seguire)
     */
    public static final int RITARDO_TRAIL = 80;

    /**
     * Velocità inseguimento
     */
    public static final double VELOCITA_INSEGUIMENTO = 20.0;

    /**
     * Distanza minima per inseguire
     */
    public static final double DISTANZA_MIN_INSEGUIMENTO = 10.0;

    /**
     * Fattore smoothing rotazione verso il giocatore (0-1)
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
     * Dimensione sprite (px). IscatMother è grande il doppio del giocatore.
     */
    public static final double DIMENSIONE_SPRITE = 128.0;

    /**
     * Raggio collisione (px).
     * ~75% del raggio visivo dello sprite (DIMENSIONE_SPRITE/2 * 0.75 = 48px).
     */
    public static final double RAGGIO_COLLISIONE = DIMENSIONE_SPRITE / 2.0 * .9; // 48px

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