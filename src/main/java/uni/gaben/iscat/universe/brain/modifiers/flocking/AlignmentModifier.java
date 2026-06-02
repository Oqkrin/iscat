package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;

public class AlignmentModifier extends AbstractFlockingModifier {

    public AlignmentModifier(Target flock, double multiplier) {
        super(flock, multiplier);
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        Vector2 averageVelocity = UU.vector2zero();
        int flockSize = 0;

        for (var body : flock.getEntities(universe)) {
            if(!(body instanceof LivingEntityModel) || body == self ) continue;
            averageVelocity.add(body.getLinearVelocity());
            flockSize++;
        }

        if (flockSize > 0) {
            // 1. Find the average heading/velocity of the flock
            averageVelocity.divide(flockSize);

            if (averageVelocity.getMagnitudeSquared() > 0) {
                averageVelocity.normalize();
                averageVelocity.multiply(maxForce*multiplier);
            }
        }

        return averageVelocity;
    }
}