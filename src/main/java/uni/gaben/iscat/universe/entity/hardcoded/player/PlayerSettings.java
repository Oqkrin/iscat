package uni.gaben.iscat.universe.entity.hardcoded.player;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public final class PlayerSettings {

    // === Sistema di Skin (Centralizzato & Data-Driven) ===
    private static final StringProperty playerSkin = new SimpleStringProperty("/uni/gaben/iscat/sprites/players/player1.png");
    private static final StringProperty playerSkinKey = new SimpleStringProperty("player1");

    public static StringProperty playerSkinProperty() { return playerSkin; }
    public static String getPlayerSkin() { return playerSkin.getValue(); }
    public static void setPlayerSkin(String skin) { playerSkin.setValue(skin); }

    public static StringProperty playerSkinKeyProperty() { return playerSkinKey; }
    public static String getPlayerSkinKey() { return playerSkinKey.getValue(); }
    public static void setPlayerSkinKey(String key) { playerSkinKey.setValue(key); }

    private PlayerSettings() {}

    // === Core Physics (BULLET HELL REWORK) ===
    public static final double MASSA = 1.0;
    public static final double VELOCITA_MAX = UniverseVelocitySettings.PLAYER_MAX_VELOCITY;

    // === Combat & Vitals ===
    public static double COOLDOWN_FUOCO_SEC = 0.3;
    public static final double FORZA_SPINTA = UniverseVelocitySettings.PLAYER_MAX_VELOCITY * 3;
    public static final double DANNO_PROIETTILE = 30.0;

    // === Visuals & Dimensions ===
    public static final double DIMENSIONE_DA_DISEGNARE = 64.0;
    public static final double HP_BAR_OFFSET_Y = 10.0;
    public static final double HP_BAR_HEIGHT = 4.0;

    // === Engine Particle Thrust Effects ===
    public static final int THRUST_MIN_PARTICLES = 12;
    public static final int THRUST_EXTRA_PARTICLES = 48;
    public static final double THRUST_SPREAD_X_FACTOR = 1.0;
    public static final double THRUST_MIN_PARTICLE_SIZE = 1.0;
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 7.0;

    // === Progression System ===
    public static final double XP_BASE_NECESSARIA = 100.0;
}