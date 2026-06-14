package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.ModifierType;
import uni.gaben.iscat.universe.entity.brain.RotationGoalType;
import uni.gaben.iscat.universe.entity.brain.SteeringGoalType;
import uni.gaben.iscat.universe.entity.brain.abilities.AbilityType;
import uni.gaben.iscat.universe.entity.shooters.PatternType;

import java.util.List;

/**
 * Immutable definition of an entity type.
 * Contains all static data: identity, visual, physics, AI, audio.
 */
public record EntityRecord(
        // Identity
        String entityKey,
        String name,
        String description,
        Integer bestiaryOrder,
        ThreatLevel threatLevel,

        // Visual / Animation
        String spritePath,
        int frameW,
        int frameH,
        double scale,
        int[] animationFrames,
        boolean isBoss,
        boolean hasEntranceAnimation,

        // Physical properties
        double initLife,
        double linearDamping,
        double mass,
        double maxVelocity,
        double maxForce,
        double maxAngularVelocity,
        int xpReward,
        ShapeType shapeType,

        // Behavioral ranges (still relevant for AI)
        double detectionRange,
        double combatRange,
        double preferredRange,
        double actionCooldownSec,   // seconds, not ms

        // Audio
        AudioProfile audio,

        // AI configuration
        BrainRecord brain,

        // Player‑only fields (could be null for enemies)
        PlayerRecord player
) {
    public enum ShapeType { CIRCLE, SQUARE, POLYGON }

    public record AudioProfile(
            List<String> attack,
            List<String> idle,
            List<String> hurt,
            List<String> death,
            List<String> spawn
    ) {}

    public record BrainRecord(
            SteeringRecord steering,
            RotationRecord rotation,
            List<AbilityRecord> abilities,
            List<ModifierRecord> modifiers
    ) {}

    public record SteeringRecord(
            SteeringGoalType type,
            double maxPredictionTime,
            double minDistance,
            double maxDistance,
            double safetyDistance
    ) {}

    public record RotationRecord(
            RotationGoalType type,
            double spinSpeedRadPerSec,
            int spinSteps,
            double stepPauseSec,
            String target
    ) {}

    public record AbilityRecord(
            AbilityType type,
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

    public record PatternRecord(
            PatternType type,
            int count,
            double angleStepDeg,
            double intervalSec,
            int repeats,
            PatternRecord innerPattern,
            String summonedEntityKey,
            double summonRadiusPx,
            String figureType
    ) {}

    public record ModifierRecord(
            ModifierType type,
            double radius,
            double weight,
            double maxPredictionTime,
            double avoidRadius
    ) {}

    public record PlayerRecord(
            double dashImpulse,
            double dashDurationSec,
            double dashCooldownSec,
            double stunDurationSec,
            double baseCooldownSec,
            List<LevelAbility> levelAbilities
    ) {}

    public record LevelAbility(
            int minLevel,
            PatternRecord pattern,
            double cooldownSec
    ) {}
}