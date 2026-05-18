package uni.gaben.iscat.gamenex.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.DistanceJoint;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiController;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.enemies.fake_iscat.FakeIscatController;
import uni.gaben.iscat.gamenex.universe.enemies.fake_iscat.FakeIscatModel;
import uni.gaben.iscat.gamenex.universe.enemies.fallen_star_golem.FallenStarGolemController;
import uni.gaben.iscat.gamenex.universe.enemies.fallen_star_golem.FallenStarGolemModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_core.IscatCoreController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_core.IscatCoreModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mother.IscatMotherController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mother.IscatMotherModel;
import uni.gaben.iscat.gamenex.universe.hearth.HearthController;
import uni.gaben.iscat.gamenex.universe.hearth.HearthModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_eater.IscatEaterController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_eater.IscatEaterModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mob.IscatMobController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mob.IscatMobModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part.IscatWormBodyPartController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part.IscatWormBodyPartModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head.IscatWormHeadController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head.IscatWormHeadModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_tail.IscatWormTailController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_tail.IscatWormTailModel;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_bomber.IscatBomberController;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

import java.util.function.BiFunction;
import java.util.function.Function;

public class UniverseSpawner {
    private static UniverseSpawner instance;

    private UniverseModel model;
    private UniverseController controller;

    private UniverseSpawner() {}

    public static synchronized UniverseSpawner getInstance() {
        if (instance == null) instance = new UniverseSpawner();
        return instance;
    }

    public void init(UniverseModel model, UniverseController controller) {
        this.model = model;
        this.controller = controller;
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
            case HEARTH -> spawnStandard(HearthModel::new, HearthController::new, x, y);
            case EATER -> spawnStandard(IscatEaterModel::new, IscatEaterController::new, x, y);
            case ISCAT_CORE -> spawnStandard(IscatCoreModel::new, IscatCoreController:: new, x, y);
            case FAKE_ISCAT -> spawnStandard(FakeIscatModel::new, FakeIscatController::new, x, y);
            case FALLEN_STAR_GOLEM -> spawnStandard(FallenStarGolemModel::new, FallenStarGolemController::new, x, y);
            case WORM -> spawnWorm(x, y);

            case PROJECTILE -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
            case WORM_HEAD, WORM_BODY, WORM_TAIL -> throw new IllegalArgumentException("Usa il tipo WORM per spawnare l'intero verme");
        };
    }

    /**
     * HOOK PER IL DATABASE FUTURO.
     * Gestisce la generazione di entità moddate o create dai giocatori a runtime.
     */
    private Object spawnCustomRuntimeEntity(String id, double x, double y) {
        // TODO: Quando implementerai il database:
        // 1. ArchetipoCustom arch = Database.getArchetipo(id);
        // 2. CustomModel model = new CustomModel(arch, x, y);
        // 3. questoSpawer.model.addEntity(model);
        System.out.println("[Runtime Spawner] Identificata entità custom non presente nell'Enum: " + id + " a coordinate (" + x + "," + y + ")");
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

    public IscatWormHeadModel spawnWorm(double x, double y) {
        if (model == null || controller == null) {
            System.err.println("UniverseSpawner non inizializzato!");
            return null;
        }

        double spacing = UU.pxToM(48.0);

        IscatWormHeadModel head = new IscatWormHeadModel(x, y);
        model.addEntity(head);
        controller.addAiController(new IscatWormHeadController(head));

        Body previous = head;

        for (int i = 0; i < 10; i++) {
            double bodyX = x - (i + 1) * spacing;
            IscatWormBodyPartModel body = new IscatWormBodyPartModel(bodyX, y);
            model.addEntity(body);

            // Crea il giunto fisico tra i segmenti consecutivi del verme
            DistanceJoint<Body> joint =
                new DistanceJoint<>(previous, body, previous.getWorldCenter(), body.getWorldCenter());
            joint.setRestDistance(spacing);
            model.addJoint(joint);

            previous = body;
        }

        double tailX = x - 11 * spacing;
        IscatWormTailModel tail = new IscatWormTailModel(tailX, y);
        model.addEntity(tail);

        // Collega la coda all'ultimo segmento corporeo
        DistanceJoint<Body> joint =
            new DistanceJoint<>(previous, tail, previous.getWorldCenter(), tail.getWorldCenter());
        joint.setRestDistance(spacing);
        model.addJoint(joint);

        return head;
    }
}