package uni.gaben.iscat.universe.spawn;

import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartController;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.blackhole.BlackHoleBrain;
import uni.gaben.iscat.universe.entities.hardcoded.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entities.brain.IEntityController;
import uni.gaben.iscat.universe.entities.worm.WormAssembler;

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
            case PLAYER            -> spawnPlayer(x, y, PlayerSettings.getPlayerSkinKey());
            case ASTEROID          -> spawnEntity(new AsteroidModel(x, y));
            case BLACKHOLE         -> spawnWithController(BlackHoleModel::new, BlackHoleBrain::new, x, y);
            case HEART             -> spawnWithController(HeartModel::new, HeartController::new, x, y);
            case WORM              -> spawnWorm(x, y);
            case PROJECTILE        -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
        };
    }

    public PlayerModel spawnPlayer(double x, double y, String skinKey) {
        String key = (skinKey == null || skinKey.isBlank()) ? "player1" : skinKey.toLowerCase().trim();

        System.out.println("key: " + key);

        EntityRecord playerRecord = EntityFactory.getCache().get(key);

        if (playerRecord == null) {
            System.err.println("[UniverseSpawner] Skin '" + key + "' non trovata nella cache della Factory! Uso 'player1' come fallback.");
            playerRecord = EntityFactory.getCache().get("player1");
        }

        if (playerRecord == null) {
            throw new RuntimeException("[UniverseSpawner] ERRORE CRITICO: Nemmeno la skin di fallback 'player1' è stata caricata!");
        }

        PlayerModel player = new PlayerModel(x, y, playerRecord);
        model.setPlayer(player);
        return player;
    }

    public EntityModel spawnWorm(double x, double y) {
        return WormAssembler.assemble(
                "iscat_worm_head",
                "iscat_worm_body_part",
                "iscat_worm_tail",
                5,
                x, y,
                model,
                controller
        );
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
        EntityModel jsonEntity = EntityFactory
                .spawn(id, x, y, model, controller);

        if (jsonEntity != null) {
            if (jsonEntity.getEntity().isBoss()) {
                jsonEntity.setWaveController(this.waveController);
            }
        }
        return jsonEntity;
    }
}