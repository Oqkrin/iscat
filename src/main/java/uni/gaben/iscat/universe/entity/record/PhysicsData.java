package uni.gaben.iscat.universe.entity.record;

import uni.gaben.iscat.universe.entity.EntityRecord;

public record PhysicsData(
        EntityRecord.ShapeType shapeType,
        double mass,
        double density,
        double linearDamping,
        boolean isSensor,
        long collisionFilter,
        boolean isProjectile,
        double radius,         // collision radius override (0 = derive from sprite)
        double terminalVelocity // for projectiles: their max speed
) {
    /** Compact constructor for entities that don't need radius or terminalVelocity overrides. */
    public PhysicsData(
            EntityRecord.ShapeType shapeType,
            double mass,
            double density,
            double linearDamping,
            boolean isSensor,
            long collisionFilter,
            boolean isProjectile) {
        this(shapeType, mass, density, linearDamping, isSensor, collisionFilter, isProjectile, 0.0, 0.0);
    }
}
