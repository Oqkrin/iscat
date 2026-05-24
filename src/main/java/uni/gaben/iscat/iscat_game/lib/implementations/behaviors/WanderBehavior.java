package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class WanderBehavior implements AiBehavior {

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private double minMagnitude = 1.0;
    private double maxMagnitude = 2.0;
    private final double force;
    private final double rotationSpeed;
    private double priorityValue = 10.0;

    public WanderBehavior(double force, double rotationSpeed) {
        this.force = force;
        this.rotationSpeed = rotationSpeed;
    }

    public WanderBehavior(double force, double rotationSpeed, double minMagnitude, double maxMagnitude, double priorityValue) {
        this.force = force;
        this.rotationSpeed = rotationSpeed;
        this.minMagnitude = minMagnitude;
        this.maxMagnitude = maxMagnitude;
        this.priorityValue = priorityValue;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return priorityValue;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (wanderTarget == null) {
            double currentDir = npc.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI));
        }

        // Rotazione
        npc.setAngularVelocity(0.0);
        double currentAngle = npc.getTransform().getRotationAngle();
        double targetAngle = wanderTarget.getDirection();
        double diff = targetAngle - currentAngle;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(rotationSpeed * dt, 1.0));
        npc.getTransform().setRotation(next);

        // Movimento
        npc.applyForce(wanderTarget.getNormalized().multiply(force));

        if (npc.contains(wanderTarget)) {
            wanderTarget = null;
        }
    }
}