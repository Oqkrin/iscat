package uni.gaben.iscat.universe.entity.Data;

public record DynamicsData(
        double maxVelocity,
        double maxForce,
        double maxAngularVelocity,
        double terminalVelocity,
        double actionCooldownSec // Used for dash, etc.
) {}
