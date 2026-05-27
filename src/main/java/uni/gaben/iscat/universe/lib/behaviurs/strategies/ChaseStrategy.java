package uni.gaben.iscat.universe.lib.behaviurs.strategies;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.player.PlayerModel;

public class ChaseStrategy implements MovementStrategy {
    private final double speed;
    public ChaseStrategy(double speed) { this.speed = speed; }

    @Override
    public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
        PlayerModel player = world.getPlayer();
        if (player == null) return new Vector2();
        Vector2 toPlayer = player.getTransform().getTranslation().copy()
                .subtract(entity.getTransform().getTranslation());
        if (toPlayer.getMagnitude() < 0.5) return new Vector2();
        return toPlayer.getNormalized().multiply(speed);
    }
}