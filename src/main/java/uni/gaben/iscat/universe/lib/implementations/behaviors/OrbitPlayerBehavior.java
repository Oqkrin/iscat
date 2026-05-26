package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

public class OrbitPlayerBehavior implements AiBehavior {

    private final double force;
    private final double maxVelocity;
    private final double orbitRadius;
    private final boolean rotateToPlayer;

    public OrbitPlayerBehavior(double force, double maxVelocity, double orbitRadius, boolean rotateToPlayer) {
        this.force = force;
        this.maxVelocity = maxVelocity;
        this.orbitRadius = orbitRadius;
        this.rotateToPlayer = rotateToPlayer;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 45.0; // Lower than Chase (50.0) or Plunge (90.0)
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        double dist = playerPos.distance(npcPos);
        
        Vector2 toPlayer = playerPos.copy().subtract(npcPos).getNormalized();
        Vector2 desiredVelocity;

        if (dist > orbitRadius * 1.2) {
            // Move towards player
            desiredVelocity = toPlayer.multiply(maxVelocity);
        } else if (dist < orbitRadius * 0.8) {
            // Move away from player
            desiredVelocity = toPlayer.multiply(-maxVelocity);
        } else {
            // Orbit around player
            Vector2 orbitDir = new Vector2(-toPlayer.y, toPlayer.x);
            desiredVelocity = orbitDir.multiply(maxVelocity);
        }

        Vector2 steering = desiredVelocity.subtract(npc.getLinearVelocity());
        

        if (desiredVelocity.getMagnitudeSquared() > 0.1 && rotateToPlayer) {
            double targetRot = desiredVelocity.getDirection();
            double cur = npc.getTransform().getRotationAngle();
            double diff = targetRot - cur;
            while(diff < -Math.PI) diff += Math.PI*2;
            while(diff > Math.PI) diff -= Math.PI*2;
            npc.getTransform().setRotation(cur + diff * dt * 5.0);
        }

        npc.setAtRest(false);
        npc.applyForce(steering.getNormalized().multiply(force));
        
        if (npc.getLinearVelocity().getMagnitude() > maxVelocity) {
            npc.setLinearVelocity(npc.getLinearVelocity().getNormalized().multiply(maxVelocity));
        }
    }
}
