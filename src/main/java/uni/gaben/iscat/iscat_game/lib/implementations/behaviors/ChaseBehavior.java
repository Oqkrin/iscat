package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;

public class ChaseBehavior implements AiBehavior {

    private final double force;
    private final double maxVelocity;
    private double detectionRange = 15.0;
    private double priorityValue = 50.0;
    private double rotationSpeed = 5.0;

    public ChaseBehavior(double force, double maxVelocity) {
        this.force = force;
        this.maxVelocity = maxVelocity;
    }

    public ChaseBehavior(double force, double maxVelocity, double detectionRange, double priorityValue, double rotationSpeed) {
        this.force = force;
        this.maxVelocity = maxVelocity;
        this.detectionRange = detectionRange;
        this.priorityValue = priorityValue;
        this.rotationSpeed = rotationSpeed;
    }

    public ChaseBehavior(double distanzaIdealePx, double maxForce, double rampUpPx, double maxVelocityMs, double steeringGain, double minDistMult, double maxDistMult) {
        this.force = maxForce;
        this.maxVelocity = maxVelocityMs;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = player.getTransform().getTranslation().distance(npc.getTransform().getTranslation());
        return dist <= detectionRange ? priorityValue : 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        Vector2 direction = playerPos.copy().subtract(npcPos);
        double distance = direction.getMagnitude();

        // Rotazione
        npc.setAngularVelocity(0.0);
        double currentAngle = npc.getTransform().getRotationAngle();
        double targetAngle = direction.getDirection();
        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        double next = uni.gaben.iscat.utils.Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(rotationSpeed * dt, 1.0));
        npc.getTransform().setRotation(next);

        // Movimento
        if (distance > 0.5) {
            Vector2 desiredVelocity = direction.getNormalized().multiply(maxVelocity);
            Vector2 steeringForce = desiredVelocity.subtract(npc.getLinearVelocity()).getNormalized().multiply(force);
            npc.applyForce(steeringForce);
        }
    }
}