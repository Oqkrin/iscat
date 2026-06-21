package uni.gaben.iscat.universe.entities.parsed;

import uni.gaben.iscat.universe.entities.EntityType;
import uni.gaben.iscat.universe.entities.ThreatLevel;

import java.util.List;

/**
 * Definizione immutabile di un tipo di entità.
 * Contiene tutti i dati statici: identità, grafica, fisica, IA e profili audio.
 */
public record EntityRecord(
        String entityKey,
        String name,
        EntityType type,
        String description,
        Integer bestiaryOrder,
        ThreatLevel threatLevel,

        String spritePath,
        int frameW,
        int frameH,
        double scale,
        List<AnimationRecord> animations,
        boolean isBoss,
        boolean hasEntranceAnimation,

        double initLife,
        double linearDamping,
        double mass,
        double maxVelocity,
        double maxForce,
        double maxAngularVelocity,
        double angularOffsetDeg,
        int xpReward,
        ShapeType shapeType,

        double detectionRange,
        double combatRange,
        double preferredRange,
        double actionCooldownSec,

        double dannoProiettile,

        AudioProfile audio,

        BrainRecord brain,

        PlayerRecord player
) {
    /** Tipologia di primitiva geometrica usata per la fixture fisica. */
    public enum ShapeType { CIRCLE, SQUARE, POLYGON }

    /**
     * Configurazione di una riga d'animazione all'interno dello spritesheet.
     */
    public record AnimationRecord(
            String type,
            int row,
            int frames,
            double durationSec
    ) {}

    /**
     * Mappatura degli ID degli eventi audio alle rispettive liste di file riproducibili.
     */
    public record AudioProfile(
            List<String> attack,
            List<String> idle,
            List<String> hurt,
            List<String> death,
            List<String> spawn
    ) {}

    /**
     * Configurazione dell'albero decisionale, dello steering e delle abilità dell'IA.
     */
    public record BrainRecord(
            SteeringRecord steering,
            RotationRecord rotation,
            List<AbilityRecord> abilities,
            List<ModifierRecord> modifiers
    ) {}

    /**
     * Parametri di movimento e distanze di ingaggio dell'algoritmo di Steering.
     */
    public record SteeringRecord(
            SteeringGoalIndex type,
            double maxPredictionTime,
            double minDistance,
            double maxDistance,
            double safetyDistance
    ) {}

    /**
     * Logica e velocità di rotazione del corpo verso i target o in modalità spin.
     */
    public record RotationRecord(
            RotationGoalIndex type,
            double spinSpeedRadPerSec,
            int spinSteps,
            double stepPauseSec,
            String target
    ) {}

    /**
     * Configurazione dei requisiti fisici e dei parametri di esecuzione di una specifica abilità.
     */
    public record AbilityRecord(
            AbilityIndex type,
            double combatRange,
            double cooldownSec,
            String bulletType,
            boolean aimAtTarget,
            double nerfPrediction,
            List<PatternRecord> patterns,
            PatternRecord pattern,
            double healAmount,
            String summonEntityKey,
            int summonCount,
            double summonRadiusPx,
            double meleeDamage,
            int attackStateIndex,
            double dashCooldownMS,
            double dashDurationMS,
            double dashPrediction,
            double dashAvoidRange,
            double dashImpulse,
            double plungeCooldownMS
    ) {}

    /**
     * Struttura geometrica, ripetizioni ed entità spawnate da un pattern di proiettili o evocazioni.
     */
    public record PatternRecord(
            PatternIndex type,
            int count,
            double angleStepDeg,
            double intervalSec,
            int repeats,
            PatternRecord innerPattern,
            String summonedEntityKey,
            double summonRadiusPx,
            String figureType
    ) {}

    /**
     * Pesi e parametri di comportamento per i modificatori di movimento secondari (es. Ostacoli).
     */
    public record ModifierRecord(
            ModifierIndex type,
            double radius,
            double weight,
            double maxPredictionTime,
            double avoidRadius
    ) {}

    /**
     * Statistiche base, costanti fisiche e abilità sbloccabili esclusive del giocatore.
     */
    public record PlayerRecord(
            double dashImpulse,
            double dashDurationSec,
            double dashCooldownSec,
            double stunDurationSec,
            double baseCooldownSec,
            double meleeDamage,
            double meleeCooldownSec,
            double baseSpeed,
            double baseThrustForce,
            double baseXPNeeded,
            double dannoProiettile,
            List<LevelAbility> levelAbilities
    ) {}

    /**
     * Associazione tra un livello minimo richiesto e il pattern di attacco sbloccato dal giocatore.
     */
    public record LevelAbility(
            int minLevel,
            PatternRecord pattern,
            double cooldownSec
    ) {}
}