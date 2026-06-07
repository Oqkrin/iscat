package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.entity.GenericEntityFactory;
import uni.gaben.iscat.universe.entity.special.master.IscatMasterModel;
import uni.gaben.iscat.universe.entity.special.worm.IscatWormModel;
import uni.gaben.iscat.universe.entity.special.worm.IscatWormSegment;
import uni.gaben.iscat.universe.entity.GenericEntityBrain;
import uni.gaben.iscat.universe.entity.consumables.heart.HeartController;
import uni.gaben.iscat.universe.entity.consumables.heart.HeartModel;
import uni.gaben.iscat.universe.entity.special.worm.IscatWormSegmentBrain;
import uni.gaben.iscat.universe.entity.enviroment.blackhole.BlackHoleBrain;
import uni.gaben.iscat.universe.entity.enviroment.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
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
        if (type != null) return spawn(type, x, y);
        return spawnCustomRuntimeEntity(id, x, y);
    }

    public Object spawn(UniverseSpawnable type, double x, double y) {
        return switch (type) {
            case PLAYER            -> spawnPlayer(x, y);
            case ASTEROID          -> spawnEntity(new AsteroidModel(x, y)); // simplified, adjust as needed
            case BLACKHOLE         -> spawnWithController(BlackHoleModel::new, BlackHoleBrain::new, x, y);
            case HEART             -> spawnWithController(HeartModel::new, HeartController::new, x, y);
            case ISCAT_MASTER      -> spawnIscatMaster(x, y);
            case WORM              -> spawnWorm(x, y);
            case PROJECTILE        -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
        };
    }

    public Object waveSpawn(UniverseSpawnable type, double x, double y, int playerLevel) {
        Object toSpawn = spawn(type, x, y);
        if (toSpawn instanceof LivingEntityModel l) {
            l.setMaxLife(l.getMaxLife() * playerLevel);
            l.setLife(l.getMaxLife());
        }
        return toSpawn;
    }

    public PlayerModel spawnPlayer(double x, double y) {
        PlayerModel player = new PlayerModel(x, y);
        model.setPlayer(player);
        return player;
    }

    public IscatMasterModel spawnIscatMaster(double x, double y) {
        IscatMasterModel master = new IscatMasterModel(x, y, waveController);
        model.addEntity(master);
        controller.addEntityController(new GenericEntityBrain(master));
        return master;
    }

    public IscatWormModel spawnWorm(double x, double y) {
        IscatWormModel worm = new IscatWormModel(x, y);
        for (IscatWormSegment seg : worm.getSegments()) {
            model.addEntity(seg);
            // Use custom brain instead of GenericEntityBrain
            IscatWormSegmentBrain brain = new IscatWormSegmentBrain(seg, worm.getHead());
            controller.addEntityController(brain);
        }
        worm.connectSegments(model);   // add joints
        return worm;
    }

    // Generic spawn for any IEntityController factory (covers Brain and old controllers)
    public  <M extends AbstractEntityModel> M spawnWithController(
            BiFunction<Double, Double, M> modelFactory,
            Function<M, IEntityController> controllerFactory,
            double x, double y) {

        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        M entityModel = modelFactory.apply(x, y);
        model.addEntity(entityModel);

        if (controllerFactory != null) {
            IEntityController ctrl = controllerFactory.apply(entityModel);
            controller.addEntityController(ctrl);
        }
        return entityModel;
    }

    public <T extends AbstractEntityModel> T spawnEntity(T entity) {
        model.addEntity(entity);
        return entity;
    }

    private Object spawnCustomRuntimeEntity(String id, double x, double y) {
        return GenericEntityFactory
                .spawn(id, x, y, model, controller);
    }



}