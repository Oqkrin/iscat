package uni.gaben.iscat.universe.rendering;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidView;
import uni.gaben.iscat.universe.enemies.master.IscatMasterView;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.enemies.worm.IscatWormView;
import uni.gaben.iscat.universe.consumables.heart.HeartView;
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
                // Salta i nemici speciali che richiedono un'istanza specifica nel costruttore
                if (needsInstanceRenderer(type.getModelClass())) {
                    continue;
                }
                Drawable<?> renderer = createSharedRenderer(type);
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
        Drawable<?> instanceRenderer = instanceRenderers.get(entity);
        if (instanceRenderer != null) {
            return (Drawable<T>) instanceRenderer;
        }

        // Se l'entità richiede un renderer unico per istanza, lo creiamo passando l'oggetto reale (e)
        if (needsInstanceRenderer(entity.getClass())) {
            return (Drawable<T>) instanceRenderers.computeIfAbsent(entity,
                    this::createInstanceRenderer);
        }

        return (Drawable<T>) sharedRenderers.get(entity.getClass());
    }

    public <T extends AbstractEntityModel> void register(T entity, Drawable<T> view) {
        instanceRenderers.put(entity, view);
    }

    public void removeRenderer(AbstractEntityModel entity) {
        instanceRenderers.remove(entity);
    }

    private boolean needsInstanceRenderer(Class<?> clazz) {
        return clazz == uni.gaben.iscat.universe.enemies.master.IscatMasterModel.class
                || clazz == uni.gaben.iscat.universe.enemies.healer.IscatHealerModel.class;
    }

    /** Crea i renderer globali statici che NON dipendono da una specifica istanza a runtime. */
    private Drawable<?> createSharedRenderer(UniverseSpawnable type) {
        if (type == null) return null;
        return switch (type) {
            case PLAYER            -> new PlayerView();
            case ASTEROID          -> new AsteroidView();
            case HEART             -> new HeartView();
            case PROJECTILE        -> new ProjectileView();
            case WORM              -> (Drawable<IscatWormSegment>) (segment, gc) ->
                    IscatWormView.forType(segment.getType()).draw(segment, gc);
            default                -> null; // Gestiti dinamicamente come istanze
        };
    }

    /** Crea dinamicamente il renderer iniettando l'istanza corretta del modello nel costruttore. */
    private Drawable<?> createInstanceRenderer(AbstractEntityModel entity) {
        if (entity instanceof uni.gaben.iscat.universe.enemies.healer.IscatHealerModel healer) {
            return new uni.gaben.iscat.universe.enemies.healer.IscatHealerView(healer);
        }
        if (entity instanceof uni.gaben.iscat.universe.enemies.master.IscatMasterModel master) {
            return new IscatMasterView(master); // Aggiungi 'master' come argomento se anche IscatMasterView lo richiede
        }
        return null;
    }
}