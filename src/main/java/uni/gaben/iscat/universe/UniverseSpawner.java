package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.IEntityController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.enemies.fake.FakeIscatController;
import uni.gaben.iscat.universe.enemies.fake.FakeIscatModel;
import uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemController;
import uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemModel;
import uni.gaben.iscat.universe.enemies.core.IscatCoreController;
import uni.gaben.iscat.universe.enemies.core.IscatCoreModel;
import uni.gaben.iscat.universe.enemies.dasher.IscatDasherController;
import uni.gaben.iscat.universe.enemies.dasher.IscatDasherModel;
import uni.gaben.iscat.universe.enemies.master.IscatMasterController;
import uni.gaben.iscat.universe.enemies.master.IscatMasterModel;
import uni.gaben.iscat.universe.enemies.mother.IscatMotherController;
import uni.gaben.iscat.universe.enemies.mother.IscatMotherModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormController;
import uni.gaben.iscat.universe.enemies.worm.IscatWormModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.consumables.heart.HeartController;
import uni.gaben.iscat.universe.consumables.heart.HeartModel;
import uni.gaben.iscat.universe.enemies.eater.IscatEaterController;
import uni.gaben.iscat.universe.enemies.eater.IscatEaterModel;
import uni.gaben.iscat.universe.enemies.mob.IscatMobBrain;
import uni.gaben.iscat.universe.enemies.mob.IscatMobModel;
import uni.gaben.iscat.universe.enemies.bomber.IscatBomberController;
import uni.gaben.iscat.universe.enemies.bomber.IscatBomberModel;
import uni.gaben.iscat.universe.player.PlayerModel;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UniverseSpawner {
    private static UniverseSpawner instance;
    private UniverseModel model;
    private UniverseController controller;
    private UniverseWaveController waveController;

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
            case ASTEROID          -> spawnEntity(new AsteroidModel(x, y, 50)); // simplified, adjust as needed
            case ISCAT_MOB         -> spawnWithController(IscatMobModel::new, IscatMobBrain::new, x, y);
            case ISCAT_MOTHER      -> spawnWithController(IscatMotherModel::new, IscatMotherController::new, x, y);
            case ISCAT_BOMBER      -> spawnWithController(IscatBomberModel::new, IscatBomberController::new, x, y);
            case HEART             -> spawnWithController(HeartModel::new, HeartController::new, x, y);
            case EATER             -> spawnWithController(IscatEaterModel::new, IscatEaterController::new, x, y);
            case ISCAT_CORE        -> spawnWithController(IscatCoreModel::new, IscatCoreController::new, x, y);
            case FAKE_ISCAT        -> spawnWithController(FakeIscatModel::new, FakeIscatController::new, x, y);
            case FALLEN_STAR_GOLEM -> spawnWithController(FallenStarGolemModel::new, FallenStarGolemController::new, x, y);
            case ISCAT_DASHER      -> spawnWithController(IscatDasherModel::new, IscatDasherController::new, x, y);
            case ISCAT_HEALER      -> spawnWithController(
                    uni.gaben.iscat.universe.enemies.healer.IscatHealerModel::new,
                    uni.gaben.iscat.universe.enemies.healer.IscatHealerController::new, x, y);
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
        controller.addEntityController(new IscatMasterController(master));
        return master;
    }

    public IscatWormModel spawnWorm(double x, double y) {
        IscatWormModel worm = new IscatWormModel(x, y);
        for (IscatWormSegment seg : worm.getSegments()) {
            model.addEntity(seg);
        }
        controller.addWormController(new IscatWormController(worm));
        return worm;
    }

    // Generic spawn for any IEntityController factory (covers Brain and old controllers)
    private <M extends AbstractEntityModel> M spawnWithController(
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
        // TODO: Database integration
        System.out.println("[Runtime Spawner] Custom entity '" + id + "' at (" + x + "," + y + ")");
        return null;
    }
}