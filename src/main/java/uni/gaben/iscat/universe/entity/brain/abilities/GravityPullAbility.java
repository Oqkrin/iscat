package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.Collections;


public class GravityPullAbility extends Ability {
    private final Target inRange;
    
    public GravityPullAbility(Target target) {
        super("GravityPull", AbilityCategory.SPECIAL, Collections.emptySet());
        this.inRange = target;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        return true;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
    }



    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        if (world == null) return false;

        Vector2 myPos = brain.getEntity().getTransform().getTranslation();
        double myMass = brain.getEntity().getMass().getMass();

        // 3. Apply gravitational force to each nearby entity
        for (AbstractEntityModel item : inRange.getEntities(world)) {
            if (item == brain.getEntity() || item == null) continue;

            Vector2 otherPos = item.getTransform().getTranslation();
            Vector2 direction = myPos.copy().subtract(otherPos);
            double distanceSq = direction.getMagnitudeSquared();
            if (distanceSq < 0.01) continue;  // avoid division by zero

            // Force magnitude = G * (M * m) / r²
            double otherMass = item.getMass().getMass();

            double forceMagnitude = (UU.UniversalGravitationalConstant.get() * myMass * otherMass) / distanceSq;
            direction.normalize();
            item.applyForce(direction.multiply(forceMagnitude));
        }

        return true;
    }
}
