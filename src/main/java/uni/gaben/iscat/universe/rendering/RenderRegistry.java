package uni.gaben.iscat.universe.rendering;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidView;
import uni.gaben.iscat.universe.enemies.fake.FakeIscatView;
import uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemView;
import uni.gaben.iscat.universe.enemies.core.IscatCoreView;
import uni.gaben.iscat.universe.enemies.master.IscatMasterView;
import uni.gaben.iscat.universe.enemies.mother.IscatMotherView;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.enemies.worm.IscatWormView;
import uni.gaben.iscat.universe.consumables.heart.HeartView;
import uni.gaben.iscat.universe.enemies.eater.IscatEaterView;
import uni.gaben.iscat.universe.enemies.mob.IscatMobView;
import uni.gaben.iscat.universe.enemies.bomber.IscatBomberView;
import uni.gaben.iscat.universe.player.PlayerView;
import uni.gaben.iscat.universe.projectiles.ProjectileView;

import java.util.HashMap;
import java.util.Map;

public class RenderRegistry {
    private static RenderRegistry instance;
    private final Map<Class<?>, Drawable<?>> sharedRenderers = new HashMap<>();
    private final Map<AbstractEntityModel, Drawable<?>> instanceRenderers = new HashMap<>();

    private RenderRegistry() {
        for (UniverseSpawnable type : UniverseSpawnable.values()) {
            if (type.getModelClass() != null) {
                Drawable<?> renderer = createRenderer(type);
                if (renderer != null) {
                    sharedRenderers.put(type.getModelClass(), renderer);
                }
            }
        }
    }

    public static synchronized RenderRegistry getInstance() {
        if (instance == null) instance = new RenderRegistry();
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> Drawable<T> getRenderer(T entity) {
        // Se l'entità ha bisogno di una view dedicata, usa instanceRenderers
        if (needsInstanceRenderer(entity.getClass())) {
            return (Drawable<T>) instanceRenderers.computeIfAbsent(entity,
                    e -> createRenderer(UniverseSpawnable.fromModelClass(e.getClass())));
        }
        return (Drawable<T>) sharedRenderers.get(entity.getClass());
    }

    public void removeRenderer(AbstractEntityModel entity) {
        instanceRenderers.remove(entity);
    }

    // Qui aggiungiamo entita che non possono condividere la stessa classe con altre entita dello stesso tipo
    private boolean needsInstanceRenderer(Class<?> clazz) {
        return clazz == uni.gaben.iscat.universe.enemies.master.IscatMasterModel.class
        || clazz == uni.gaben.iscat.universe.enemies.healer.IscatHealerModel.class;
    }

    private Drawable<?> createRenderer(UniverseSpawnable type) {
        if (type == null) return null;
        return switch (type) {
            case PLAYER        -> new PlayerView();
            case ASTEROID      -> new AsteroidView();
            case ISCAT_MOB     -> new IscatMobView();
            case ISCAT_BOMBER  -> new IscatBomberView();
            case ISCAT_MOTHER  -> new IscatMotherView();
            case HEART         -> new HeartView();
            case EATER         -> new IscatEaterView();
            case PROJECTILE    -> new ProjectileView();
            case ISCAT_CORE    -> new IscatCoreView();
            case FAKE_ISCAT    -> new FakeIscatView();
            case FALLEN_STAR_GOLEM -> new FallenStarGolemView();
            case ISCAT_DASHER  -> new uni.gaben.iscat.universe.enemies.dasher.IscatDasherView();
            case ISCAT_HEALER  -> new uni.gaben.iscat.universe.enemies.healer.IscatHealerView();
            case ISCAT_MASTER  -> new IscatMasterView();
            case WORM -> (Drawable<IscatWormSegment>) (segment, gc) ->
                    IscatWormView.forType(segment.getType()).draw(segment, gc);
        };
    }
}