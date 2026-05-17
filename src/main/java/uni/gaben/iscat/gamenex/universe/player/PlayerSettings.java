package uni.gaben.iscat.gamenex.universe.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uni.gaben.iscat.utils.design.ScalareAureo;

/**
 * Centralized settings configuration for the Player entity.
 * All units are documented and scaled properly.
 */
public final class PlayerSettings {
    public static final boolean DEBUG_COLLISION_VISIBLE = true;
    private static final StringProperty playerSkin = new SimpleStringProperty("/uni/gaben/iscat/sprites/player1.png");

    public static StringProperty playerSkinProperty() { return playerSkin; }
    public static String getPlayerSkin() { return playerSkin.getValue(); }
    public static void setPlayerSkin(String skin) { playerSkin.setValue(skin); }

    private PlayerSettings() {}

    // === Core Physics ===
    public static final double MASSA = 1.0;
    public static final double LINEAR_DAMPING = 1.5;
    public static final double VELOCITA_MAX = 120.0; // Meters per second
    public static final double FORZA_SPINTA = 60.0; // Newtons

    // === Dash Mechanical Settings ===
    public static final double IMPULSO_SCATTO = 30.0;
    public static final double LINEAR_DAMPING_SCATTO = 0.7;
    public static final double DURATA_SCATTO_SEC = 1.0/4.0;
    public static final double COOLDOWN_SCATTO_SEC = 0.8;

    // === Combat & Vitals ===
    public static final int HP_MASSIMO = 1000;
    public static final int HP_INIZIALE = 100;
    public static final double COOLDOWN_FUOCO_SEC = 0.16; // Single source of truth for firing speed
    public static final double VELOCITA_PROIETTILE = 800.0;
    public static final double DANNO_PROIETTILE = 10.0;

    // === Visuals & Dimensions ===
    public static final double DIMENSIONE_DA_DISEGNARE = 64.0; // Render box size (pixels)
    public static final double RAGGIO_COLLISIONE = 24.0;       // Collision boundary radius (pixels)
    public static final double HP_BAR_OFFSET_Y = 10.0;
    public static final double HP_BAR_HEIGHT = 4.0;

    // === Engine Particle Thrust Effects ===
    public static final int THRUST_MIN_PARTICLES = 12;              // Raddoppiate le particelle minime (da 12)
    public static final int THRUST_EXTRA_PARTICLES = 48;            // Aumentate le particelle massime (da 48)
    public static final double THRUST_HEIGHT_FACTOR = ScalareAureo.IPHI;         // Coda leggermente più lunga (da 0.65)
    public static final double THRUST_SPREAD_X_FACTOR = 0.45;       // Triplicata la larghezza del cono (da 0.15)
    public static final double THRUST_MIN_PARTICLE_SIZE = 1.0;      // Particelle base più grandi (da 1.0)
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 7.0; // Maggiore escursione di volume (da 7.0)
}