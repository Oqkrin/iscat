package uni.gaben.iscat.universe.lib.behaviurs.strategies;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.player.PlayerModel;

import java.util.LinkedList;

// TrailFollowStrategy.java
public class TrailFollowStrategy implements MovementStrategy {
    private final double speed;
    private final LinkedList<Vector2> trail = new LinkedList<>();
    private final int trailLength, delay;

    public TrailFollowStrategy(double speed, int trailLength, int delay) {
        this.speed = speed;
        this.trailLength = trailLength;
        this.delay = delay;
    }

    @Override
    public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
        PlayerModel player = world.getPlayer();
        if (player == null) return new Vector2();
        Vector2 playerPos = player.getTransform().getTranslation().copy();
        trail.addLast(playerPos);
        if (trail.size() > trailLength) trail.removeFirst();
        if (trail.size() <= delay) return new Vector2();
        int idx = trail.size() - delay - 1;
        Vector2 target = trail.get(idx);
        Vector2 toTarget = target.copy().subtract(entity.getTransform().getTranslation());
        if (toTarget.getMagnitude() < 0.5) return new Vector2();
        return toTarget.getNormalized().multiply(speed);
    }
}