package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class SeparationModifier extends AbstractFlockingModifier {

    double range;
    // Workspace vectors (reused per frame, zero allocation)
    private final Vector2 diff = new Vector2();
    private final Vector2 sum = new Vector2();

    public SeparationModifier(Target flock, double range, double multiplier) {
        super(flock, multiplier);
        this.range = range;
    }

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        // Reset workspace
        sum.x = 0;
        sum.y = 0;
        int flockSize = 0;
        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : flock.getEntities(universe)) {
            if (body == self) continue;

            Vector2 bodyPos = body.getTransform().getTranslation();
            double dx = selfPos.x - bodyPos.x;
            double dy = selfPos.y - bodyPos.y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            if (distance < 0.01 || distance > range) continue;

            // Vector pointing AWAY from neighbor
            diff.x = dx;
            diff.y = dy;

            double diffMagSq = diff.x * diff.x + diff.y * diff.y;
            if (diffMagSq < 0.0001) {
                // If too close, push in random direction
                diff.x = (Math.random() - 0.5) * 2.0;
                diff.y = (Math.random() - 0.5) * 2.0;
                diffMagSq = diff.x * diff.x + diff.y * diff.y;
            }

            // Weight by inverse distance: normalize then scale by (1/distance)
            double mag = Math.sqrt(diffMagSq);
            double invDistance = 1.0 / Math.max(distance, 0.01);
            double scale = invDistance / mag;  // Combined normalize + 1/r weighting
            
            diff.x *= scale;
            diff.y *= scale;

            sum.x += diff.x;
            sum.y += diff.y;
            flockSize++;
        }

        if (flockSize > 0) {
            // Average the separation vectors
            double avgScale = 1.0 / flockSize;
            sum.x *= avgScale;
            sum.y *= avgScale;
            
            // Normalize and scale by maxForce * multiplier
            double magSq = sum.x * sum.x + sum.y * sum.y;
            if (magSq > 0.0001) {
                double mag = Math.sqrt(magSq);
                sum.x *= multiplier;
                sum.y *= multiplier;
            } else {
                // Zero magnitude, reset to zero
                sum.x = 0;
                sum.y = 0;
            }
        }
        return sum;
    }
}