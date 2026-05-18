package uni.gaben.iscat.gamenex.view;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.UniverseSpawnable;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidView;
import uni.gaben.iscat.gamenex.universe.enemies.fake_iscat.FakeIscatView;
import uni.gaben.iscat.gamenex.universe.enemies.fallen_star_golem.FallenStarGolemView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_core.IscatCoreView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mother.IscatMotherView;
import uni.gaben.iscat.gamenex.universe.hearth.HearthView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_eater.IscatEaterView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mob.IscatMobView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part.IscatWormBodyPartView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head.IscatWormHeadView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_tail.IscatWormTailView;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_bomber.IscatBomberView;
import uni.gaben.iscat.gamenex.universe.player.PlayerView;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileView;

import java.util.HashMap;
import java.util.Map;

public class ViewRegistry {
    private static ViewRegistry instance;
    private final Map<Class<?>, Drawable<?>> renderers = new HashMap<>();

    private ViewRegistry() {
        // Auto-registrazione bindata a livello di compilazione!
        for (UniverseSpawnable type : UniverseSpawnable.values()) {
            if (type.getModelClass() != null) {
                Drawable<?> renderer = createRenderer(type);
                if (renderer != null) {
                    renderers.put(type.getModelClass(), renderer);
                }
            }
        }
    }

    public static synchronized ViewRegistry getInstance() {
        if (instance == null) {
            instance = new ViewRegistry();
        }
        return instance;
    }

    /**
     * QUESTO È IL GUARDIA-PORTA.
     * Switch Expression senza `default`: se aggiungi un elemento all'enum e
     * dimentichi di metterlo qui, il gioco non compila.
     */
    private Drawable<?> createRenderer(UniverseSpawnable type) {
        return switch (type) {
            case PLAYER -> new PlayerView();
            case ASTEROID -> new AsteroidView();
            case ISCAT_MOB -> new IscatMobView();
            case ISCAT_BOMBER -> new IscatBomberView();
            case ISCAT_MOTHER -> new IscatMotherView();
            case HEARTH -> new HearthView();
            case EATER -> new IscatEaterView();
            case WORM_HEAD -> new IscatWormHeadView();
            case WORM_BODY -> new IscatWormBodyPartView();
            case WORM_TAIL -> new IscatWormTailView();
            case PROJECTILE -> new ProjectileView();
            case ISCAT_CORE -> new IscatCoreView();
            case FAKE_ISCAT -> new FakeIscatView();
            case FALLEN_STAR_GOLEM -> new FallenStarGolemView();
            case WORM -> null; // WORM non ha View diretta, essendo composto
        };
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> Drawable<T> getRenderer(Class<T> modelClass) {
        return (Drawable<T>) renderers.get(modelClass);
    }
}