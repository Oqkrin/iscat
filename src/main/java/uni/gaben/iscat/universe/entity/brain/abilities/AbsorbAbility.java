package uni.gaben.iscat.universe.entity.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.universe.entity.modules.PhysicsModule;

public class AbsorbAbility extends Ability {

    private static final double MAX_DENSITY = 40.0;
    private static final double GROWTH_FACTOR = 0.1;
    private static final double RADIUS_GROWTH_BASE = 0.002;
    private static final double RADIATION_IDLE_TIME = 2.0;
    private static final double RADIATION_DENSITY_DECAY = 0.01;

    private boolean initialized = false;
    private double timeSinceLastAbsorption = 0.0;
    private double initialRadiusM = 1.0;
    private double maxRadiusM = 7.5;

    public AbsorbAbility() {
        super(AbilityCategory.DEFENSE, 0);
    }

    @Override
    public boolean canActivate(GameEntity self, UniverseModel world, double dt) {
        return !initialized;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        GameEntity self = brain.getEntity();
        initialized = true;

        if (self.hasModule(PhysicsModule.class)) {
            // Assume circle shape for blackhole
            initialRadiusM = self.getModule(PhysicsModule.class).getFixture().getShape().getRadius();
            maxRadiusM = initialRadiusM * 7.5;
        }

        self.setOnCollision(other -> absorbEntity(self, other));
    }

    private void absorbEntity(GameEntity self, GameEntity other) {
        if (other == null || other.shouldRemove()) return;
        
        // Don't absorb other black holes (we can check by checking if they have AbsorbAbility, or their identity)
        if (other.getRecord().identity().entityKey().contains("blackhole")) return;
        
        if (other instanceof AbstractProjectileModel) {
            other.setShouldRemove(true);
            return;
        }

        if (other.getRecord().identity().entityKey().contains("player")) {
            Vector2 pushDir = other.getTransform().getTranslation().subtract(self.getTransform().getTranslation());
            double dist = pushDir.getMagnitude();
            if (dist > 0.01) {
                pushDir.normalize();
                double currentDensity = self.hasModule(PhysicsModule.class) ? self.getModule(PhysicsModule.class).getFixture().getDensity() : 1.0;
                double impulseMag = 800.0 * Math.min(currentDensity, MAX_DENSITY);
                other.applyImpulse(pushDir.multiply(impulseMag));
            }
            return;
        }

        double absorbedMass = other.getMass().getMass();

        if (self.hasModule(PhysicsModule.class)) {
            PhysicsModule pm = self.getModule(PhysicsModule.class);
            double currentRadius = pm.getFixture().getShape().getRadius();
            
            if (currentRadius < maxRadiusM) {
                double progress = (currentRadius - initialRadiusM) / (maxRadiusM - initialRadiusM);
                double growth = absorbedMass * RADIUS_GROWTH_BASE * (1.0 - progress);
                double newRadius = Math.min(currentRadius + growth, maxRadiusM);
                // Currently PhysicsModule doesn't support dynamically changing radius easily,
                // We'd have to recreate the fixture. For now we will update density.
                // In a fuller implementation, PhysicsModule should expose `setRadius`.
                
                double currentDensity = pm.getFixture().getDensity();
                double newDensity = Math.min(currentDensity + absorbedMass * GROWTH_FACTOR, MAX_DENSITY);
                pm.getFixture().setDensity(newDensity);
                self.setMass(org.dyn4j.geometry.MassType.NORMAL);
            }
        }

        if (other.hasModule(EnduranceModule.class)) {
            other.getModule(EnduranceModule.class).completeDeath();
        } else {
            other.setShouldRemove(true);
        }

        timeSinceLastAbsorption = 0.0;
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        timeSinceLastAbsorption += dt;
        if (timeSinceLastAbsorption > RADIATION_IDLE_TIME) {
            GameEntity self = brain.getEntity();
            if (self.hasModule(PhysicsModule.class)) {
                PhysicsModule pm = self.getModule(PhysicsModule.class);
                double currentDensity = pm.getFixture().getDensity();
                if (currentDensity > 1.0) {
                    double newDensity = Math.max(1.0, currentDensity - RADIATION_DENSITY_DECAY * dt);
                    pm.getFixture().setDensity(newDensity);
                    self.setMass(org.dyn4j.geometry.MassType.NORMAL);
                }
            }
        }
        return false;
    }
}
