package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.enviroment.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;

public class ObstaclesAvoidanceModifier implements MovementModifier {

    Target enviroment;

    public ObstaclesAvoidanceModifier(Target environment) {
        this.enviroment = environment;
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        Vector2 sum = UU.vector2zero();
        int flockSize = 0;

        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : enviroment.getEntities(universe)) {
            if(body instanceof LivingEntityModel || body == self ) continue;
            Vector2 bodyPos = body.getTransform().getTranslation();
            double distance = selfPos.distance(bodyPos);

            // 1. Calculate vector pointing AWAY from the neighbor
            Vector2 diff = selfPos.copy().subtract(bodyPos);
            // 2. Normalize to get pure direction, then weight by distance
            // (closer neighbors push much harder than distant ones)
            diff.normalize();
            diff.multiply(body instanceof BlackHoleModel bh ? bh.getRadius().m().get()*maxForce : maxForce);
            diff.divide(distance);

            // 3. Accumulate the forces
            sum.add(diff);
            flockSize++;
        }

        if (flockSize > 0) {
            // Average out the accumulated separation vectors
            sum.divide(flockSize);
        }

        return sum;
    }
}