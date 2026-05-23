package uni.gaben.iscat.game.universe.enemies.iscat_core;

import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.VelocitySettings;

/**
 * Impostazioni centralizzate per l'IscatCore.
 * Definisce le costanti fisiche e i parametri dell'intelligenza artificiale.
 */
public class IscatCoreSettings {

    // ── RILEVAMENTO E RANGE ────────────────────────────────────────────────────
    public static final double DETECTION_RANGE   = 15.0;  // metri — vede il player
    public static final double COMBAT_RANGE      = 10.0;  // metri — entra in combattimento
    public static final double PREFERRED_RANGE   = 7.0;   // metri — distanza ideale di fuoco

    // ── MOVIMENTO ─────────────────────────────────────────────────────────────
    public static final double FORCE             = 15.0;
    public static final double MAX_VELOCITY      = VelocitySettings.CORE_MAX_VELOCITY;
    public static final double DAMPING_LINEARE   = 3.0;

    // ── SEPARAZIONE ───────────────────────────────────────────────────────────
    public static final double SEPARATION_RADIUS_PX = 50.0;

    // ── ROTAZIONE ─────────────────────────────────────────────────────────────
    public static final double ROTATION_SPEED    = 1.5;   // lerp verso l'angolo target
    public static final double ROTATION_INTERVAL = 10.0;  // secondi tra uno scatto di 45°

    // ── COMBATTIMENTO ─────────────────────────────────────────────────────────
    public static final double FIRE_COOLDOWN_S   = 1.2;   // pausa tra un attacco e il successivo
    public static final double BULLET_SPACING_M  = UU.pxToM(24.0); // distanza tra le 3 linee parallele
    public static final int    PROJECTILE_DAMAGE  = 12;

    // ── FISICA / COLLISIONI ───────────────────────────────────────────────────
    public static final int    DIM_SPRITE        = 64;
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;

    // ── SPRITE ────────────────────────────────────────────────────────────────
    public static final int    HP_INIZIALI       = 500;
    public static final int    NUMERO_FRAMES     = 1;
    public static final double SCALE             = 2.0;

    // ── PROIETTILE ────────────────────────────────────────────────────────────
    public static final double VEL_PROIETTILE    = VelocitySettings.ENEMY_BULLET_VELOCITY;

    // ── RICOMPENSA ────────────────────────────────────────────────────────────
    public static final double XP_REWARD         = 50.0;
}