package uni.gaben.iscat.universe.entities;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;

import java.util.function.Predicate;

/**
 * Filtri di utilità per identificare e categorizzare le entità di gioco nelle collisioni e nei calcoli IA.
 */
public class EntityFilters {

    private EntityFilters() {}

    /**
     * Filtro che verifica se un corpo fisico appartiene al giocatore.
     */
    public static final Predicate<Body> IS_PLAYER = body -> {
        if (body instanceof PlayerModel) return true;
        return body.getUserData() instanceof PlayerModel;
    };

    /**
     * Filtro che verifica se un corpo fisico è un nemico (escludendo il giocatore).
     */
    public static final Predicate<Body> IS_ENEMY = body -> {
        if (IS_PLAYER.test(body)) return false;
        if (body instanceof EntityModel) return true;
        return body.getUserData() instanceof EntityModel;
    };

    /**
     * Crea un filtro per escludere una specifica entità dai risultati (evita che un'entità controlli se stessa).
     */
    public static Predicate<Body> isNot(Body self) {
        return body -> body != self && body.getUserData() != self;
    }
}