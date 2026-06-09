package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.entity.player.PlayerModelAbstract;

import java.util.function.Predicate;

public class EntityFilters {
    
    private EntityFilters() {}

    public static final Predicate<Body> IS_PLAYER = body -> {
        if (body instanceof PlayerModelAbstract) return true;
        return body.getUserData() instanceof PlayerModelAbstract;
    };

    public static final Predicate<Body> IS_ENEMY = body -> {
        // Assume anything that is a EntityModel is an enemy for now.
        // If there are other enemy types, they should be added here.
        if (body instanceof EntityModel) return true;
        return body.getUserData() instanceof EntityModel;
    };
    
    public static Predicate<Body> isNot(Body self) {
        return body -> body != self && body.getUserData() != self;
    }
}
