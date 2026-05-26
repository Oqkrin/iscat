package uni.gaben.iscat.iscat_game.universe;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.iscat_game.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.iscat_game.universe.iscats.fake.FakeIscatController;
import uni.gaben.iscat.iscat_game.universe.iscats.fake.FakeIscatModel;
import uni.gaben.iscat.iscat_game.universe.iscats.fallen_star_golem.FallenStarGolemController;
import uni.gaben.iscat.iscat_game.universe.iscats.fallen_star_golem.FallenStarGolemModel;
import uni.gaben.iscat.iscat_game.universe.iscats.core.IscatCoreController;
import uni.gaben.iscat.iscat_game.universe.iscats.core.IscatCoreModel;
import uni.gaben.iscat.iscat_game.universe.iscats.dasher.IscatDasherController;
import uni.gaben.iscat.iscat_game.universe.iscats.dasher.IscatDasherModel;
import uni.gaben.iscat.iscat_game.universe.iscats.master.IscatMasterController;
import uni.gaben.iscat.iscat_game.universe.iscats.master.IscatMasterModel;
import uni.gaben.iscat.iscat_game.universe.iscats.mother.IscatMotherController;
import uni.gaben.iscat.iscat_game.universe.iscats.mother.IscatMotherModel;
import uni.gaben.iscat.iscat_game.universe.iscats.worm.IscatWormController;
import uni.gaben.iscat.iscat_game.universe.iscats.worm.IscatWormModel;
import uni.gaben.iscat.iscat_game.universe.iscats.worm.IscatWormSegment;
import uni.gaben.iscat.iscat_game.universe.heart.HeartController;
import uni.gaben.iscat.iscat_game.universe.heart.HeartModel;
import uni.gaben.iscat.iscat_game.universe.iscats.eater.IscatEaterController;
import uni.gaben.iscat.iscat_game.universe.iscats.eater.IscatEaterModel;
import uni.gaben.iscat.iscat_game.universe.iscats.mob.IscatMobController;
import uni.gaben.iscat.iscat_game.universe.iscats.mob.IscatMobModel;
import uni.gaben.iscat.iscat_game.universe.iscats.bomber.IscatBomberController;
import uni.gaben.iscat.iscat_game.universe.iscats.bomber.IscatBomberModel;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UniverseSpawner {
    private static UniverseSpawner instance;

    private UniverseModel model;
    private UniverseController controller;
    private EnemyWaveController waveController;

    private UniverseSpawner() {}

    public static synchronized UniverseSpawner getInstance() {
        if (instance == null) instance = new UniverseSpawner();
        return instance;
    }

    public void init(UniverseModel model, UniverseController controller, EnemyWaveController waveController) {
        this.model = model;
        this.controller = controller;
        this.waveController = waveController;
    }

    /**
     * ENTRY POINT PER STRINGHE (Runtime e Database-friendly)
     * Controlla se l'ID appartiene alle entità fisse, altrimenti devia sul canale custom.
     */
    public Object spawn(String id, double x, double y) {
        UniverseSpawnable type = UniverseSpawnable.fromString(id);

        if (type != null) {
            // Entità Core: passiamo allo switch nativo ed esaustivo
            return spawn(type, x, y);
        }

        // Fallback: Entità custom generata a runtime o letta da Database/JSON
        return spawnCustomRuntimeEntity(id, x, y);
    }

    /**
     * IL CUORE BLINDATO.
     * Switch Expression senza `default` per il controllo totale in compilazione.
     */
    public Object spawn(UniverseSpawnable type, double x, double y) {
        return switch (type) {
            case PLAYER -> spawnPlayer(x, y);
            case ASTEROID -> spawnStandard(AsteroidModel::new, null, x, y);
            case ISCAT_MOB -> spawnStandard(IscatMobModel::new, IscatMobController::new, x, y);
            case ISCAT_MOTHER -> spawnStandard(IscatMotherModel::new, IscatMotherController::new, x, y);
            case ISCAT_BOMBER -> spawnStandard(IscatBomberModel::new, IscatBomberController::new, x, y);
            case HEART -> spawnStandard(HeartModel::new, HeartController::new, x, y);
            case EATER -> spawnStandard(IscatEaterModel::new, IscatEaterController::new, x, y);
            case ISCAT_CORE -> spawnStandard(IscatCoreModel::new, IscatCoreController:: new, x, y);
            case FAKE_ISCAT -> spawnStandard(FakeIscatModel::new, FakeIscatController::new, x, y);
            case FALLEN_STAR_GOLEM -> spawnStandard(FallenStarGolemModel::new, FallenStarGolemController::new, x, y);
            case ISCAT_DASHER -> spawnStandard(IscatDasherModel::new, IscatDasherController::new, x, y);
            case ISCAT_HEALER -> spawnStandard(uni.gaben.iscat.iscat_game.universe.iscats.healer.IscatHealerModel::new, uni.gaben.iscat.iscat_game.universe.iscats.healer.IscatHealerController::new, x, y);
            case ISCAT_MASTER -> spawnIscatMaster(x, y);
            case WORM -> spawnWorm(x, y);

            case PROJECTILE -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
        };
    }

    public IscatMasterModel spawnIscatMaster(double x, double y) {
        IscatMasterModel master = new IscatMasterModel(x, y, waveController);
        model.addEntity(master);
        controller.addAiController(new IscatMasterController(master));
        return master;
    }

    /**
     * HOOK PER IL DATABASE FUTURO.
     * Gestisce la generazione di entità moddate o create dai giocatori a runtime.
     */
    private Object spawnCustomRuntimeEntity(String id, double x, double y) {
        // TODO: Quando implementerai il database:
        // 1. ArchetipoCustom arch = Database.getArchetipo(id);
        // 2. CustomModel model = new CustomModel(arch, x, y);
        // 3. questoSpawer.model.addEntity(model);System.out.println("[Runtime Spawner] Identificata entità custom non presente nell'Enum: " + id + " a coordinate (" + x + "," + y + ")");
        return null;
    }

    private <M extends AbstractEntityModel> M spawnStandard(
            BiFunction<Double, Double, M> modelFactory,
            Function<M, ?> controllerFactory,
            double x, double y) {

        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        M entityModel = modelFactory.apply(x, y);
        model.addEntity(entityModel);

        if (controllerFactory != null) {
            Object aiController = controllerFactory.apply(entityModel);
            controller.addAiController((AiController) aiController);
        }

        return entityModel;
    }

    public PlayerModel spawnPlayer(double x, double y) {
        PlayerModel player = new PlayerModel(x, y);
        model.setPlayer(player);
        return player;
    }

    public <T extends AbstractEntityModel> T spawnEntity(T entity) {
        model.addEntity(entity);
        return entity;
    }

    public IscatWormModel spawnWorm(double x, double y) {
        IscatWormModel worm = new IscatWormModel(x, y);
        for (IscatWormSegment seg : worm.getSegments()) {
            model.addEntity(seg); // ogni segmento è un corpo fisico separato
        }
        controller.addAiController(new IscatWormController(worm));
        return worm;
    }
}