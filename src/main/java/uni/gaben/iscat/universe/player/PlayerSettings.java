package uni.gaben.iscat.universe.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uni.gaben.iscat.universe.UniverseVelocitySettings;
import uni.gaben.iscat.utils.design.ScalareAureo;

public final class PlayerSettings {
    public static final boolean DEBUG_COLLISION_VISIBLE = true;
    private static final StringProperty playerSkin = new SimpleStringProperty("/uni/gaben/iscat/sprites/players/player1.png");

    public static StringProperty playerSkinProperty() { return playerSkin; }
    public static String getPlayerSkin() { return playerSkin.getValue(); }
    public static void setPlayerSkin(String skin) { playerSkin.setValue(skin); }

    private PlayerSettings() {}

    // === Core Physics (BULLET HELL REWORK) ===
    public static final double MASSA = 1.0;
    // Damping only matters during a dash now, normal movement is instant
    public static final double LINEAR_DAMPING = 1.0;
    public static final double VELOCITA_MAX = UniverseVelocitySettings.PLAYER_MAX_VELOCITY;

    // === Dash Mechanical Settings ===
    public static final double IMPULSO_SCATTO = UniverseVelocitySettings.PLAYER_DASH_IMPULSE;
    public static final double LINEAR_DAMPING_SCATTO = .1 ; // Higher friction during dash so it doesn't slide forever
    public static final double DURATA_SCATTO_SEC = 1.0 / 1.5; // Slightly shorter, snappier dash
    public static final double COOLDOWN_SCATTO_SEC = 0.8;

    // === Combat & Vitals ===
    public static final int HP_MASSIMO = 100;
    public static final int HP_INIZIALE = 100;
    public static double COOLDOWN_FUOCO_SEC = 1/2.0;
    public static final double VELOCITA_PROIETTILE = UniverseVelocitySettings.PLAYER_BULLET_VELOCITY;
    public static final double FORZA_SPINTA = UniverseVelocitySettings.PLAYER_MAX_VELOCITY*3;
    public static final double DANNO_PROIETTILE = 10.0;

    // === Visuals & Dimensions ===
    public static final double DIMENSIONE_DA_DISEGNARE = 64.0;
    // BULLET HELL HITBOX: Extremely small center radius for dodging tight patterns
    public static final double RAGGIO_COLLISIONE = 28.0;
    public static final double HP_BAR_OFFSET_Y = 10.0;
    public static final double HP_BAR_HEIGHT = 4.0;

    // === Engine Particle Thrust Effects ===
    public static final int THRUST_MIN_PARTICLES = 12;
    public static final int THRUST_EXTRA_PARTICLES = 48;
    public static final double THRUST_HEIGHT_FACTOR = ScalareAureo.IPHI;
    public static final double THRUST_SPREAD_X_FACTOR = 1;
    public static final double THRUST_MIN_PARTICLE_SIZE = 1.0;
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 7.0;

    public static final double XP_BASE_NECESSARIA = 100.0;
}