package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

@FunctionalInterface
public interface SteeringModifier {
    /**
     * Computes an independent steering force (in Newtons) and writes it into outForce.
     * The force will be added to the total steering force accumulator and clamped to maxForce.
     */
    void computeSteer(AbstractEntityModel self, UniverseModel world, double maxForce, double dt, Vector2 outForce);


    static SteeringModifier separation(Target flock, double range) {



        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);


        };

    }
}