package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.Body;


import java.util.function.Predicate;

public class EntityFilters {
    
    private EntityFilters() {}

    public static final Predicate<Body> IS_PLAYER = body -> {
        if (body instanceof GameEntity ge && ge.getRecord().identity().entityKey().contains("player")) return true;
        if (body.getUserData() instanceof GameEntity ge && ge.getRecord().identity().entityKey().contains("player")) return true;
        return false;
    };

    public static final Predicate<Body> IS_ENEMY = body -> {
        // Assume anything that is a GameEntity is an enemy for now.
        // If there are other enemy types, they should be added here.
        if (body instanceof GameEntity) return true;
        return body.getUserData() instanceof GameEntity;
    };
    
    public static Predicate<Body> isNot(Body self) {
        return body -> body != self && body.getUserData() != self;
    }
}
