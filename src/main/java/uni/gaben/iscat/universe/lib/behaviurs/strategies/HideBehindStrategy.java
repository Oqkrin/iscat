package uni.gaben.iscat.universe.lib.behaviurs.strategies;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;

// HideBehindStrategy.java
public class HideBehindStrategy implements MovementStrategy {
    private final double speed, hideDistance;

    public HideBehindStrategy(double speed, double hideDistance) {
        this.speed = speed;
        this.hideDistance = hideDistance;
    }

    @Override
    public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
        PlayerModel player = world.getPlayer();
        if (player == null) return new Vector2();
        // Find nearest ally (excluding healers)
        AbstractEntityModel bestAlly = null;
        double bestDist = Double.MAX_VALUE;
        for (AbstractEntityModel e : world.getEntitiesOfType(LivingEntityModel.class)) {
            if (e == entity || e == player) continue;
            if (e.getClass().getSimpleName().contains("Healer")) continue;
            double d = e.getTransform().getTranslation().distance(entity.getTransform().getTranslation());
            if (d < bestDist) {
                bestDist = d;
                bestAlly = e;
            }
        }
        if (bestAlly == null) {
            // Flee directly away
            Vector2 away = entity.getTransform().getTranslation().copy().subtract(player.getTransform().getTranslation());
            return away.getNormalized().multiply(speed);
        }
        Vector2 allyPos = bestAlly.getTransform().getTranslation();
        Vector2 toAlly = allyPos.copy().subtract(player.getTransform().getTranslation()).getNormalized();
        Vector2 target = allyPos.copy().add(toAlly.multiply(hideDistance));
        Vector2 toTarget = target.copy().subtract(entity.getTransform().getTranslation());
        if (toTarget.getMagnitude() < 0.1) return new Vector2();
        return toTarget.getNormalized().multiply(speed);
    }
}