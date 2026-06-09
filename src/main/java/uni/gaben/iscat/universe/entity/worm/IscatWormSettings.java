package uni.gaben.iscat.universe.entity.worm;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatWormSettings {

    // --- CONFIGURAZIONE DI SISTEMA & REWARD ---
    public static final int    INITIAL_SEGMENTS     = 11;
    public static final double DIM_SPRITE           = 64.0;
    public static final int XP_REWARD            = 150;

    // --- DISTANZE FISICHE E SPAZIATURA ESPRESSA IN PIXEL ---
    public static final double FOLLOW_DISTANCE_PX   = 50.0;
    public static final int    SEGMENT_SPACING_PX   = (int)(50.0 * 2.0);

    // --- IMPOSTAZIONI TESTA (HEAD) ---
    public static final int    HEAD_HP              = 1500;
    public static final double HEAD_SCALE           = 3;
    public static final double HEAD_MAX_SPEED       = UniverseVelocitySettings.WORM_HEAD_MAX_SPEED;
    public static final double HEAD_FORCE           = 800.0;
    public static final double HEAD_ROTATION_SPEED  = 2.5;

    // --- MECCANICHE DI ATTACCO DELLA TESTA ---
    public static final int    HEAD_ATTACK_POWER         = 3;
    public static final double HEAD_ATTACK_COOLDOWN      = 5;
    public static final double PLUNGE_THRESHOLD_MULT     = 1.5; // Soglia di velocità (moltiplicatore) per attivare l'attacco in carica
    public static final double PLUNGE_DAMAGE_MULT        = 1.5; // Moltiplicatore del danno durante il plunge attack

    // --- IMPOSTAZIONI CORPO (BODY) ---
    public static final int    BODY_HP              = 750;
    public static final double BODY_SCALE           = 3;
    public static final double BODY_FOLLOW_FORCE    = 100;
    public static final double BODY_CONTACT_DAMAGE  = 100.0;

    // --- IMPOSTAZIONI CODA (TAIL) ---
    public static final int    TAIL_HP              = 2500;
    public static final double TAIL_SCALE           = 3;
    public static final double TAIL_FOLLOW_FORCE    = 100;
    public static final double TAIL_COMBAT_RANGE    = 100.0;
    public static final double TAIL_PREFERRED_RANGE = 10.0;
    public static final double TAIL_FORCE           = 15.0;
    public static final double TAIL_FIRE_COOLDOWN   = 1.0;

    public static final double JOINT_FREQUENCY = 8.0;   // spring stiffness
    public static final double JOINT_DAMPING = 0.6;     // damping
    public static final double SEGMENT_SPACING_M = UU.pxToM(SEGMENT_SPACING_PX);

}