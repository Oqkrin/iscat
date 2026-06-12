package uni.gaben.iscat.universe.entity.Data;

import java.util.List;

public record BrainData(
        SteeringRecord steering,
        RotationRecord rotation,
        List<AbilityRecord> abilities,
        List<ModifierRecord> modifiers
) {
    public record SteeringRecord(
            String type,
            double maxPredictionTime,
            double minDistance,
            double maxDistance,
            double safetyDistance
    ) {}

    public record RotationRecord(
            String type,
            double spinSpeedRadPerSec,
            int spinSteps,
            double stepPauseSec,
            String target
    ) {}

    public record AbilityRecord(
            String type,
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
            int attackStateIndex
    ) {}

    public record PatternRecord(
            String type,
            int count,
            double angleStepDeg,
            double intervalSec,
            int repeats,
            String summonedEntityKey,
            double summonRadiusPx,
            String figureType
    ) {}

    public record ModifierRecord(
            String type,
            double radius,
            double weight,
            double maxPredictionTime,
            double avoidRadius
    ) {}
}
