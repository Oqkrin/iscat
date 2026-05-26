package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

public class SeekLineOfSightBehavior implements AiBehavior {

    private final double force;
    private final double maxVelocity;
    private final double priorityValue = 45.0; // Slightly lower than chase/combat
    private double strafeDirection = 1.0;
    private double changeDirectionTimer = 0;

    public SeekLineOfSightBehavior(double force, double maxVelocity) {
        this.force = force;
        this.maxVelocity = maxVelocity;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return priorityValue;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        changeDirectionTimer -= dt;
        if (changeDirectionTimer <= 0) {
            strafeDirection = Math.random() > 0.5 ? 1.0 : -1.0;
            changeDirectionTimer = 2.0 + Math.random() * 2.0; // Change direction every 2-4 seconds
        }

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        Vector2 directionToPlayer = playerPos.copy().subtract(npcPos).getNormalized();

        // Move perpendicularly to the player to find an angle around the obstacle
        Vector2 strafeVec = new Vector2(-directionToPlayer.y, directionToPlayer.x).multiply(strafeDirection);

        if (npc.getLinearVelocity().getMagnitude() <= maxVelocity) {
            npc.applyForce(strafeVec.multiply(force));
        }
        
        // Also rotate towards where we are going
        double targetAngle = strafeVec.getDirection();
        double currentAngle = npc.getTransform().getRotationAngle();
        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        double next = uni.gaben.iscat.utils.Interpolator.lerp(currentAngle, currentAngle + diff, Math.min(5.0 * dt, 1.0));
        npc.getTransform().setRotation(next);
    }

}
