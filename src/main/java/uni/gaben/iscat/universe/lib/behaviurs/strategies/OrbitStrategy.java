package uni.gaben.iscat.universe.lib.behaviurs.strategies;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.player.PlayerModel;

public class OrbitStrategy implements MovementStrategy {
    private final double speed, radius;
    private double angle = 0;
    private double changeTimer = 0;

    public OrbitStrategy(double speed, double radius) {
        this.speed = speed;
        this.radius = radius;
    }

    @Override
    public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
        PlayerModel player = world.getPlayer();
        if (player == null) return new Vector2();
        Vector2 center = player.getTransform().getTranslation();
        Vector2 pos = entity.getTransform().getTranslation();
        Vector2 toCenter = center.copy().subtract(pos);
        double dist = toCenter.getMagnitude();

        // Adjust angle periodically to weave left/right
        changeTimer -= dt;
        if (changeTimer <= 0) {
            angle += (Math.random() - 0.5) * Math.toRadians(60);
            changeTimer = 1.5 + Math.random();
        }

        double radialCorrection = (dist - radius) * 0.5; // gentle pull
        Vector2 radial = toCenter.getNormalized().multiply(radialCorrection);
        Vector2 tangent = new Vector2(-Math.sin(angle), Math.cos(angle)).multiply(speed);
        return radial.add(tangent);
    }
}