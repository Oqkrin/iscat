package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.entity.*;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.universe.entity.brain.IEntityController;

import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UniverseSpawner {
    private static UniverseSpawner instance;
    private UniverseModel model;
    private UniverseController controller;
    private UniverseWaveController waveController;
    private Random random = new Random();

    private UniverseSpawner() {}

    public static synchronized UniverseSpawner getInstance() {
        if (instance == null) instance = new UniverseSpawner();
        return instance;
    }

    public void init(UniverseModel model, UniverseController controller, UniverseWaveController waveController) {
        this.model = model;
        this.controller = controller;
        this.waveController = waveController;
    }

    public Object spawn(String id, double x, double y) {
        UniverseSpawnable type = UniverseSpawnable.fromString(id);
        if (type != null) {
            return spawn(type, x, y);
        }
        return spawnCustomRuntimeEntity(id, x, y);
    }

    public Object spawn(UniverseSpawnable type, double x, double y) {
        return switch (type) {
            case PLAYER            -> spawnPlayer(x, y);
            case ASTEROID          -> spawnCustomRuntimeEntity("asteroid", x, y);
            case BLACKHOLE         -> spawnCustomRuntimeEntity("blackhole", x, y);
            case HEART             -> spawnCustomRuntimeEntity("heart", x, y);
            case WORM              -> spawnCustomRuntimeEntity("iscat_worm_head", x, y); // Simplified worm for now
            case PROJECTILE        -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
        };
    }

    public Object waveSpawn(UniverseSpawnable type, double x, double y, int playerLevel) {
        Object toSpawn = spawn(type, x, y);
        if (toSpawn instanceof GameEntity l && l.hasModule(EnduranceModule.class)) {
            EnduranceModule em = l.getModule(EnduranceModule.class);
            // TODO: Scale max endurance
            em.setEndurance(em.getMaxEndurance());
        }
        return toSpawn;
    }

    public GameEntity spawnPlayer(double x, double y) {
        GameEntity player = EntityFactory.spawn("player1", x, y, model, controller);
        model.setPlayer(player);
        return player;
    }

    public <M extends GameEntity> M spawnWithController(
            BiFunction<Double, Double, M> modelFactory,
            Function<M, IEntityController> controllerFactory,
            double x, double y) {

        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        M entity = modelFactory.apply(x, y);
        model.addEntity(entity);

        if (controllerFactory != null) {
            IEntityController ctrl = controllerFactory.apply(entity);
            controller.addEntityController(ctrl);
        }
        return entity;
    }

    public <T extends GameEntity> T spawnEntity(T entity) {
        model.addEntity(entity);
        return entity;
    }

    private GameEntity spawnCustomRuntimeEntity(String id, double x, double y) {
        GameEntity entity = EntityFactory.spawn(id, x, y, model, controller);

        if (entity != null && entity.hasModule(EnduranceModule.class)) {
            if (entity.getRecord().identity().isBoss()) {
                entity.getModule(EnduranceModule.class).setWaveController(this.waveController);
            }
        }
        return entity;
    }
}
