package uni.gaben.iscat.gamenex.universe.enemies.iscat_mother;

import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public final class IscatMotherSettings {
    private IscatMotherSettings() {}

    // --- VITA ---
    public static final int HP_INIZIALI = 500;

    // --- RENDERING ---
    public static final int DIM_SPRITE = 128;
    public static final double SCALE = 4.0;

    // --- FISICA ---
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;
    public static final double DAMPING_LINEARE = 3.0;
    public static final double MAX_VELOCITY_MS = 20.0;
    public static final double FORCE = 13.0;

    // --- AI: DISTANZE ---
    public static final double DISTANZA_IDEALE_M = 250.0 / UniverseSettings.SCALE;
    public static final double DISTANZA_TOLLERANZA_VICINO = 20.0 / UniverseSettings.SCALE;
    public static final double DISTANZA_TOLLERANZA_LONTANO = 30.0 / UniverseSettings.SCALE;
    public static final double DETECTION_RANGE = 15.0;  // metri
    public static final double COMBAT_RANGE_MIN = 90.0 / UniverseSettings.SCALE;
    public static final double COMBAT_RANGE_MAX = 420.0 / UniverseSettings.SCALE;
    public static final double ROTATION_SPEED = 5.0;

    // --- SPARO ---
    public static final double COOLDOWN_SPARO_SEC = 1.0;
    public static final double SPREAD_ANGLE_DEG = 15.0;
    public static final int BURST_COUNT = 3; // spara 3 proiettili a ventaglio

    // --- MINIONI ---
    public static final double MINION_SPAWN_HP_THRESHOLD = 0.5; // al 50% HP
    public static final int MINION_ISCAT_COUNT = 5;
    public static final int MINION_EATER_COUNT = 2;
    public static final double MINION_SPAWN_RADIUS = 80.0 / UniverseSettings.SCALE;

    // --- ORDA ALLA MORTE ---
    public static final int HORDE_SIZE = 40;
    public static final double HORDE_RADIUS = 130.0 / UniverseSettings.SCALE;
    public static final double HORDE_RADIUS_VARIANCE = 60.0 / UniverseSettings.SCALE;
}
