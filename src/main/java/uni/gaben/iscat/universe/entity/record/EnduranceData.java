package uni.gaben.iscat.universe.entity.record;

public record EnduranceData(
        double initLife,
        double maxLife,
        double collisionDamageScale // Factor for ramming damage
) {}
