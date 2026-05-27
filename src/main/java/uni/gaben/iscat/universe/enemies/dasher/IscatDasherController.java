package uni.gaben.iscat.universe.enemies.dasher;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ObstacleAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.lib.behaviurs.strategies.ChaseStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.strategies.OrbitStrategy;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.dasher.IscatDasherSettings.ISCATDASHER;

// IscatDasherController.java
public class IscatDasherController extends AiController {

    private final Cooldown plungeCooldown = new Cooldown();

    public IscatDasherController(IscatDasherModel entity) {
        super(entity, ISCATDASHER.force, ISCATDASHER.maxVelocity, ISCATDASHER.rotationSpeed);
        // Movement strategies: default orbit
        setMovementStrategy(new OrbitStrategy(ISCATDASHER.maxVelocity, IscatDasherSettings.orbitRadius));
        // Modifiers: always apply
        addModifier(new SeparationModifier(2.0, 0.6));
        addModifier(new ObstacleAvoidanceModifier());
        addModifier(new ProjectileAvoidanceModifier());
        // Attacks
        addAttack(new PlungeAttack(IscatDasherSettings.plungeTriggerRadius, IscatDasherSettings.plungeForce, 1.5));
    }

    // Plunge attack (both AttackBehavior and also changes movement strategy temporarily)
    private class PlungeAttack implements AttackBehavior {
        private final double triggerRadius, force, cooldown;
        public PlungeAttack(double triggerRadius, double force, double cooldown) {
            this.triggerRadius = triggerRadius;
            this.force = force;
            this.cooldown = cooldown;
        }
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (plungeCooldown.isCoolingDown()) return 0;
            PlayerModel p = world.getPlayer();
            if (p == null) return 0;
            double dist = entity.getTransform().getTranslation().distance(p.getTransform().getTranslation());
            return dist < triggerRadius ? 100 : 0;
        }
        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel p = world.getPlayer();
            if (p == null) return;
            Vector2 dir = p.getTransform().getTranslation().copy()
                    .subtract(entity.getTransform().getTranslation()).getNormalized();
            entity.applyImpulse(dir.multiply(force));
            plungeCooldown.start(cooldown);
            // Temporarily set movement to direct chase for the plunge duration
            setMovementStrategy(new ChaseStrategy(ISCATDASHER.maxVelocity));
            // After 0.5 seconds, revert to orbit
            new java.util.Timer().schedule(new java.util.TimerTask() {
                @Override public void run() {
                    setMovementStrategy(new OrbitStrategy(ISCATDASHER.maxVelocity, IscatDasherSettings.orbitRadius));
                }
            }, 500);
        }
        @Override public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            plungeCooldown.update(dt);
        }
    }
}