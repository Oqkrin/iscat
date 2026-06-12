package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.modules.PhysicsModule;

import java.util.List;

public class GravityAuraAbility extends Ability {

    private final double radius;
    private final double basePullForce;

    public GravityAuraAbility(double radius, double basePullForce) {
        super("GravityAura", AbilityCategory.SPECIAL, java.util.Collections.emptySet()); // always active
        this.radius = radius;
        this.basePullForce = basePullForce;
    }

    @Override
    public boolean canActivate(GameEntity self, UniverseModel world, double dt) {
        return true;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        GameEntity self = brain.getEntity();
        Vector2 selfPos = self.getTransform().getTranslation();
        double currentDensity = self.hasModule(PhysicsModule.class) ? self.getModule(PhysicsModule.class).getFixture().getDensity() : 1.0;

        List<GameEntity> entities = world.getEntities();
        for (GameEntity e : entities) {
            if (e == self) continue;
            
            Vector2 ePos = e.getTransform().getTranslation();
            Vector2 dir = selfPos.copy().subtract(ePos);
            double distSq = dir.getMagnitudeSquared();
            
            if (distSq > 0 && distSq < radius * radius) {
                dir.normalize();
                double forceMag = (basePullForce * currentDensity) / distSq;
                e.applyForce(dir.multiply(forceMag));
            }
        }
        return false; // keep ticking? Actually we want it always active, so return true? 
        // Wait, if it returns false it finishes, and the brain re-activates it next frame if canActivate is true. 
        // Returning false is fine.
    }
}
