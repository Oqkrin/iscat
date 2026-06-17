package uni.gaben.iscat.universe.entities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;

import java.util.function.Predicate;

public class EntityFilters {
    
    private EntityFilters() {}

    public static final Predicate<Body> IS_PLAYER = body -> {
        if (body instanceof PlayerModel) return true;
        return body.getUserData() instanceof PlayerModel;
    };

    public static final Predicate<Body> IS_ENEMY = body -> {
        if (IS_PLAYER.test(body)) return false;
        if (body instanceof EntityModel) return true;
        return body.getUserData() instanceof EntityModel;
    };
    
    public static Predicate<Body> isNot(Body self) {
        return body -> body != self && body.getUserData() != self;
    }
}
