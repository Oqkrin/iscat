package uni.gaben.iscat.universe;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.enemies.generic.GenericEntityFactory;
import uni.gaben.iscat.universe.enemies.master.IscatMasterModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.enemies.generic.GenericEntityBrain;
import uni.gaben.iscat.universe.consumables.heart.HeartController;
import uni.gaben.iscat.universe.consumables.heart.HeartModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegmentBrain;
import uni.gaben.iscat.universe.enviroment.blackhole.BlackHoleBrain;
import uni.gaben.iscat.universe.enviroment.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.IEntityController;

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
            case ASTEROID          -> spawnEntity(new AsteroidModel(x, y, 50)); // simplified, adjust as needed
            case BLACKHOLE         -> spawnWithController(BlackHoleModel::new, BlackHoleBrain::new, x, y);
            case HEART             -> spawnWithController(HeartModel::new, HeartController::new, x, y);
            case ISCAT_HEALER      -> spawnCustomRuntimeEntity("iscat_healer", x, y);
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

    public void spawnInitialAsteroidBelts(double centerX, double centerY) {
        for (int clump = 0; clump < 6; clump++) {
            double angle = (clump * (Math.PI * 2.0 / 6.0)) + (Math.random() * 0.5);
            double dist = 600.0 + Math.random() * 1200.0;

            double cx = centerX + Math.cos(angle) * dist;
            double cy = centerY + Math.sin(angle) * dist;

            int count = random.nextInt(3, 18);
            for (int i = 0; i < count; i++) {
                double offsetAngle = Math.random() * Math.PI * 2.0;
                double offsetDist = Math.random() * 180.0;

                double ax = cx + Math.cos(offsetAngle) * offsetDist;
                double ay = cy + Math.sin(offsetAngle) * offsetDist;

                double radius = 20.0 + Math.random() * 350.0;
                AsteroidModel ast = new AsteroidModel(ax, ay, radius);

                double driftAngle = Math.random() * Math.PI * 2.0;
                double speed = 0.5 + Math.random() * 2.0;
                ast.setLinearVelocity(new Vector2(
                        Math.cos(driftAngle) * speed,
                        Math.sin(driftAngle) * speed
                ));

                UniverseSpawner.getInstance().spawnEntity(ast);
            }
        }
    }

}