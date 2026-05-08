package uni.gaben.iscat.game.utils.settings;

public class PerformanceSettings {
    private PerformanceSettings() {}

    /** Target FPS */
    public static final int TARGET_FPS = 60;

    /** Tick rate fisica (Hz) */
    public static final int TICK_RATE_FISICA = 60;

    /** Distanza massima rendering entità (px, 0 = infinito) */
    public static double DISTANZA_MAX_RENDERING = 2000.0;

    /** Distanza massima aggiornamento AI (px, 0 = infinito) */
    public static double DISTANZA_MAX_AI = 1500.0;
}