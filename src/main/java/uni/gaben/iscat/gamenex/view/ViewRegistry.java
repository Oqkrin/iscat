package uni.gaben.iscat.gamenex.view;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidView;
import uni.gaben.iscat.gamenex.universe.iscat_eater.IscatEaterModel;
import uni.gaben.iscat.gamenex.universe.iscat_eater.IscatEaterView;
import uni.gaben.iscat.gamenex.universe.hearth.HearthModel;
import uni.gaben.iscat.gamenex.universe.hearth.HearthView;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobView;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartView;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadView;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailView;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerView;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileView;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry for mapping Model classes to their corresponding Renderers (View layer).
 * Lives strictly in the view package to maintain MVC separation.
 */
public class ViewRegistry {
    private static ViewRegistry instance;
    private final Map<Class<?>, Drawable<?>> renderers = new HashMap<>();

    private ViewRegistry() {}

    public static synchronized ViewRegistry getInstance() {
        if (instance == null) {
            instance = new ViewRegistry();
            instance.register(PlayerModel.class, new PlayerView());
            instance.register(AsteroidModel.class, new AsteroidView());
            instance.register(IscatMobModel.class, new IscatMobView());
            instance.register(HearthModel.class, new HearthView());
            instance.register(IscatEaterModel.class, new IscatEaterView());
            instance.register(IscatWormHeadModel.class, new IscatWormHeadView());
            instance.register(IscatWormBodyPartModel.class, new IscatWormBodyPartView());
            instance.register(IscatWormTailModel.class, new IscatWormTailView());
            instance.register(Projectile.class, new ProjectileView());
        }
        return instance;
    }

    public <T extends AbstractEntityModel> void register(Class<T> modelClass, Drawable<T> renderer) {
        renderers.put(modelClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> Drawable<T> getRenderer(Class<T> modelClass) {
        return (Drawable<T>) renderers.get(modelClass);
    }
}
