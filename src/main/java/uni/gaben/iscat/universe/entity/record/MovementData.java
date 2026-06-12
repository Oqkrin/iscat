package uni.gaben.iscat.universe.entity.record;

public record MovementData(
        double maxVelocity,
        double maxForce,
        double maxAngularVelocity,
        double terminalVelocity,
        double actionCooldownSec // Used for dash, etc.
) {}
