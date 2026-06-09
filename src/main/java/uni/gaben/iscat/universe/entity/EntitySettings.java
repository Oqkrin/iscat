package uni.gaben.iscat.universe.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO specifico per il caricamento da JSON.
 * Estende PhysicalEntitySettings, ereditando automaticamente tutti i parametri fisici,
 * di movimento, di range IA e le ricompense (XP), evitando duplicazioni di memoria.
 */
public class EntitySettings extends PhysicalEntitySettings {

    // 1. CAMPI IDENTITÀ (Specifici di questa sottoclasse)
    public String entityKey = "";
    public String name = "";
    public String description = "";

    // 2. PROPRIETÀ VISIVE / ANIMAZIONE (Specifici di questa sottoclasse)
    public String spritePath = ""; // Mappato dinamicamente da 'SpriteName' nel factory
    public int frameW = 32;
    public int frameH = 32;

    // 3. ADATTAMENTO PARAMETRI JSON VS CLASSE MADRE
    // Nota: 'shapeType', 'scale', 'initLife', 'mass', 'maxVelocity', 'detectionRange', ecc.
    // NON SONO SCRITTI QUI perché sono già ereditati da PhysicalEntitySettings.

    /** * Il JSON fornisce il cooldown in secondi (es. 800), mentre la classe madre
     * espone 'actionCooldownMS'. Teniamo questo campo per catturare il dato del JSON.
     */
    public double actionCooldownMS = 0.0;

    // 4. STRUTTURA AUDIO (Specifica di questa sottoclasse)
    public AudioProfile audio = new AudioProfile();

    // Boss logic
    public boolean isBoss;
    public boolean hasEntranceAnimation;
    public int[] animationFrames;

    /**
     * Sottoclasse interna per mappare l'oggetto 'audio' e le sue liste di file Wave/MP3.
     */
    public static class AudioProfile {
        public List<String> attack = new ArrayList<>();
        public List<String> idle = new ArrayList<>();
        public List<String> hurt = new ArrayList<>();
        public List<String> death = new ArrayList<>();
        public List<String> spawn = new ArrayList<>();
    }

    // Inside EntitySettings.java

    public static class BrainSettings {
        public SteeringSettings steering = new SteeringSettings();
        public RotationSettings rotation = new RotationSettings();
        public List<AbilitySettings> abilities = new ArrayList<>();
        public List<ModifierSettings> modifiers = new ArrayList<>();
    }

    public static class SteeringSettings {
        public String type = "idle";                // idle, pursuit, evade, pursuitWithRange, evadeWithRange
        public double maxPredictionTime = 2.5;
        public double minDistance = 2.5;
        public double maxDistance = 10.0;
        public double safetyDistance = 5.0;
    }

    public static class RotationSettings {
        public String type = "idle";                // idle, movement, target, continuesSpin, intervalSpin
        public double spinSpeedRadPerSec = 0.0;
        public int spinSteps = 8;
        public double stepPauseSec = 0.1;
        public String target = "player";
    }

    public static class AbilitySettings {
        public String type;                         // shoot, randomizedShoot, heal, summon, melee, gravityPull
        public double combatRange = 10.0;
        public double cooldownSec = 0.8;
        public String bulletType = "ENEMY_BULLET";
        public boolean aimAtTarget = true;
        public double nerfPrediction = 0.0;
        public List<PatternSettings> patterns = new ArrayList<>(); // for randomizedShoot
        public PatternSettings pattern;                // for single shoot / summon / melee
        public double healAmount = 0.0;              // for heal
        public String summonEntityKey;               // for summon
        public int summonCount = 1;
        public double summonRadiusPx = 100.0;
        public double meleeDamage = 0.0;             // for melee
        public int attackStateIndex = 4;         // for melee
    }

    public static class PatternSettings {
        public String type;                          // singleShot, spread, multiDirection, ring, repeater, parallelLine, summon, figure
        public int count = 1;
        public double angleStepDeg = 30.0;
        public double intervalSec = 0.15;
        public int repeats = 1;
        public String summonedEntityKey;
        public double summonRadiusPx = 100.0;
        public String figureType = "CIRCLE";
    }

    public static class ModifierSettings {
        public String type;                          // separation, alignment, cohesion, collisionAvoidance
        public double radius = 2.0;
        public double weight = 1.0;
        public double maxPredictionTime = 1.0;
        public double avoidRadius = 2.0;
    }

    public BrainSettings brain = new BrainSettings();

}