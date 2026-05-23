package uni.gaben.iscat.game.universe.enemies.iscat_bomber;

import uni.gaben.iscat.game.universe.VelocitySettings;

/**
 * Impostazioni centralizzate per l'IscatBomber.
 */
public final class IscatBomberSettings {
    private IscatBomberSettings() { }

    // --- PROPRIETA FISICHE E VITA ---
    public static final int HP_INIZIALI = 100;
    public static final double MASSA = 8.0;
    public static final double DAMPING_LINEARE = 3.0;
    
    // --- RENDERING ---
    public static final int DIM_SPRITE = 32;
    public static final double SCALE = 4.0;
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.80; // 24px

    // --- AI e INSEGUIMENTO ---
    public static final int LUNGHEZZA_TRAIL = 120;
    public static final int RITARDO_TRAIL = 40;
    public static final double VELOCITA_INSEGUIMENTO = 50.0;
    public static final double DISTANZA_MIN_INSEGUIMENTO = 10.0;
    public static final double SMOOTHING_ROTAZIONE = 0.12;

    public static final double MAX_VELOCITY_MS = VelocitySettings.BOMBER_MAX_VELOCITY;
    public static final double FORCE = 40.0; // Maggiore forza dato che e' piu' pesante
    
    // --- STORDIMENTO ---
    public static final double DURATA_STORDIMENTO_SEC = 0.5; // 30 tick a 60fps

    public static final double XP_REWARD = 100.0;
}
