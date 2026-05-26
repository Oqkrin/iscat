package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

public class HideBehindEntitiesBehavior implements AiBehavior {

    private final double force;
    private final double maxVelocity;
    private final double hideDistance;

    public HideBehindEntitiesBehavior(double force, double maxVelocity, double hideDistance) {
        this.force = force;
        this.maxVelocity = maxVelocity;
        this.hideDistance = hideDistance;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 45.0; 
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        // Find nearest ally that is NOT a Healer
        AbstractEntityModel nearestAlly = null;
        double minDist = Double.MAX_VALUE;

        for (AbstractEntityModel e : universe.getEntitiesOfType(LivingEntityModel.class)) {
            if (e != npc && e != player && !e.getClass().getSimpleName().contains("Healer")) {
                double dist = e.getTransform().getTranslation().distance(npcPos);
                if (dist < minDist) {
                    minDist = dist;
                    nearestAlly = e;
                }
            }
        }

        Vector2 targetPos;
        if (nearestAlly != null) {
            // Position behind the ally, away from the player
            Vector2 allyPos = nearestAlly.getTransform().getTranslation();
            Vector2 playerToAlly = allyPos.copy().subtract(playerPos).getNormalized();
            targetPos = allyPos.copy().add(playerToAlly.multiply(hideDistance));
        } else {
            // Run away from player if no ally found
            Vector2 playerToNpc = npcPos.copy().subtract(playerPos).getNormalized();
            targetPos = npcPos.copy().add(playerToNpc.multiply(10.0)); 
        }

        Vector2 desiredVelocity = targetPos.subtract(npcPos).getNormalized().multiply(maxVelocity);
        Vector2 steering = desiredVelocity.subtract(npc.getLinearVelocity());
        
        if (desiredVelocity.getMagnitudeSquared() > 0.1) {
            double targetRot = desiredVelocity.getDirection();
            double cur = npc.getTransform().getRotationAngle();
            double diff = targetRot - cur;
            while(diff < -Math.PI) diff += Math.PI*2;
            while(diff > Math.PI) diff -= Math.PI*2;
            npc.getTransform().setRotation(cur + diff * dt * 3.0);
        }

        npc.setAtRest(false);
        npc.applyForce(steering.getNormalized().multiply(force));
        
        if (npc.getLinearVelocity().getMagnitude() > maxVelocity) {
            npc.setLinearVelocity(npc.getLinearVelocity().getNormalized().multiply(maxVelocity));
        }
    }
}
