package uni.gaben.iscat.universe.lib.behaviurs.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

// SeparationModifier.java
public class SeparationModifier implements AvoidanceModifier {
    private final double radius, weight;

    public SeparationModifier(double radius, double weight) {
        this.radius = radius;
        this.weight = weight;
    }

    @Override
    public Vector2 modify(Vector2 desired, AbstractEntityModel entity, UniverseModel world, double dt) {
        Vector2 pos = entity.getTransform().getTranslation();
        Vector2 steer = new Vector2();
        int count = 0;
        for (AbstractEntityModel other : world.getEntitiesOfType(entity.getClass())) {
            if (other == entity) continue;
            Vector2 diff = pos.copy().subtract(other.getTransform().getTranslation());
            double dist = diff.getMagnitude();
            if (dist < radius && dist > 0) {
                steer.add(diff.getNormalized().divide(dist));
                count++;
            }
        }
        if (count > 0) {
            steer.divide(count);
            steer.getNormalized().multiply(weight);
            return desired.copy().add(steer);
        }
        return desired;
    }
}
