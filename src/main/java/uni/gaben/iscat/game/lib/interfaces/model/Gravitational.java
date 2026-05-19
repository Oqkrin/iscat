package uni.gaben.iscat.game.lib.interfaces.model;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

public interface Gravitational {
    double getGravitationalConstant();
    double getGravityRadius();
    double getMassValue();
    Vector2 getPosition();

    default void applyGravityTo(Body target) {
        Vector2 myPos = getPosition();
        Vector2 tPos = target.getTransform().getTranslation();
        double dx = myPos.x - tPos.x;
        double dy = myPos.y - tPos.y;
        double distSq = dx * dx + dy * dy;

        if (distSq < 1.0) return;
        double dist = Math.sqrt(distSq);
        if (dist > getGravityRadius()) return;

        double targetMass = target.getMass().getMass();
        double f = getGravitationalConstant() * getMassValue() * targetMass / distSq;
        target.applyForce(new Vector2((dx / dist) * f, (dy / dist) * f));
    }
}
