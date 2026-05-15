package uni.gaben.iscat.gamenex.universe.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Impostazioni centralizzate per il giocatore.
 */
public final class PlayerSettings {
    /** Player skin path **/
    private static StringProperty playerSkin = new SimpleStringProperty("/uni/gaben/iscat/sprites/player1.png");

    public static StringProperty playerSkinProperty() { return playerSkin; }
    public static String getPlayerSkin() { return playerSkin.getValue(); }
    public static void setPlayerSkin(String playerSkin) { PlayerSettings.playerSkin.setValue(playerSkin); }

    private PlayerSettings() {}

    // === Fisica (adattata per Dyn4j) ===
    public static final double MASSA = 1.0;
    /** Attrito lineare: decelera il corpo nel tempo. Un valore più alto = più controllo ma meno inerzia. */
    public static final double LINEAR_DAMPING = 1.5;
    /** Velocità massima del giocatore (m/s). Aumentata per un gameplay più dinamico. */
    public static final double VELOCITA_MAX = 75.0;
    
    // === Movimento ===
    /** Forza di spinta applicata dai motori (Newton). */
    public static final double FORZA_SPINTA = 30.0;

    // === Dash ===
    /** Impulso istantaneo applicato durante lo scatto. */
    public static final double IMPULSO_SCATTO = 25.0; 
    public static final double LINEAR_DAMPING_SCATTO = 1.0; 
    /** Velocità massima raggiungibile durante lo scatto. */
    public static final double VELOCITA_MAX_SCATTO = 55.0;
    
    // Cooldown e durata dello scatto (in secondi)
    public static final double DURATA_SCATTO_SEC = 0.25; 
    public static final double COOLDOWN_SCATTO_SEC = 0.8; 

    // === Combattimento ===
    public static final int HP_MASSIMO = 1000000000;
    public static int HP_INIZIALE = 100000000;

    // === Collisione ===
    public static final double DIMENSIONE_DA_DISEGNARE = 64.0;
    public static final double RAGGIO_COLLISIONE = 24.0;

    // === Attacco ===
    public static final double COOLDOWN_FUOCO_TICKS = 0.16;
    public static final double COOLDOWN_FUOCO_SEC = 0.16; 
    public static final double VELOCITA_PROIETTILE = 800.0;
    public static final double DANNO_PROIETTILE = 10.0;

    // === Visual ===
    public static final double HP_BAR_OFFSET_Y = 10.0;
    public static final double HP_BAR_HEIGHT = 4.0;

    // === Thrust Effect (Particelle dei motori) ===
    public static final int THRUST_MIN_PARTICLES = 3;
    public static final int THRUST_EXTRA_PARTICLES = 7;
    public static final double THRUST_HEIGHT_FACTOR = 0.65;
    public static final double THRUST_SPREAD_X_FACTOR = 0.15;
    public static final double THRUST_MIN_PARTICLE_SIZE = 2.0;
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 3.0;

    // === Colori Particelle ===
    public static final double PARTICLE_CORE_THRESHOLD = 0.3;
    public static final double PARTICLE_MID_THRESHOLD = 0.7;
    public static final double PARTICLE_CORE_BRIGHTNESS = 1.0;
    public static final double PARTICLE_CORE_ALPHA = 0.9;
    public static final double PARTICLE_MID_BRIGHTNESS = 0.9;
    public static final double PARTICLE_MID_ALPHA = 0.8;
    public static final double PARTICLE_TAIL_BRIGHTNESS = 0.5;
    public static final double PARTICLE_TAIL_ALPHA = 0.7;
}
