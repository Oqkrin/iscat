package uni.gaben.iscat.universe.spawn;

import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.boosts.heart.HeartController;
import uni.gaben.iscat.universe.entities.boosts.heart.HeartModel;
import uni.gaben.iscat.universe.entities.blackhole.BlackHoleBrain;
import uni.gaben.iscat.universe.entities.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.asteroids.AsteroidModel;
import uni.gaben.iscat.universe.entities.brain.IEntityController;
import uni.gaben.iscat.universe.entities.worm.WormAssembler;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.SessionManager;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Singleton centrale per lo spawning di entità statiche, controllate da IA o caricate da JSON.
 * Applica un confinamento geometrico radiale per impedire lo spawn fuori dai confini del mondo.
 */
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
        this.model = model; this.controller = controller; this.waveController = waveController;
    }

    /**
     * Esegue lo spawn forzando trigonometricamente la posizione entro il raggio limite dell'universo.
     */
    public Object spawn(String id, double x, double y) {
        if (model != null) {
            double maxAllowedRadiusPx = UU.mToPx(model.getUniverseRadius()) - UU.mToPx(2.0);
            double distance = Math.sqrt(x * x + y * y);

            if (distance > maxAllowedRadiusPx) {
                double angle = Math.atan2(y, x);
                x = Math.cos(angle) * maxAllowedRadiusPx;
                y = Math.sin(angle) * maxAllowedRadiusPx;
            }
        }

        UniverseSpawnable type = UniverseSpawnable.fromString(id);
        return (type != null) ? spawn(type, x, y) : spawnCustomRuntimeEntity(id, x, y);
    }

    /**
     * Smista l'istanza in base alle costanti fisse dell'enum {@link UniverseSpawnable}.
     */
    public Object spawn(UniverseSpawnable type, double x, double y) {
        return switch (type) {
            case PLAYER            -> spawnPlayer(x, y, SessionManager.getPlayerSkinKey());
            case ASTEROID          -> spawnEntity(new AsteroidModel(x, y));
            case BLACKHOLE         -> spawnWithController(BlackHoleModel::new, BlackHoleBrain::new, x, y);
            case HEART             -> spawnWithController(HeartModel::new, HeartController::new, x, y);
            case WORM              -> spawnWorm(x, y);
            case PROJECTILE        -> throw new IllegalArgumentException("Usa spawnProjectile per istanziare proiettili");
        };
    }

    /**
     * Inizializza il giocatore applicando la skin o un fallback predefinito ("player1").
     */
    public PlayerModel spawnPlayer(double x, double y, String skinKey) {
        String key = (skinKey == null || skinKey.isBlank()) ? "player1" : skinKey.toLowerCase().trim();
        EntityRecord playerRecord = EntityFactory.getCache().get(key);

        if (playerRecord == null) playerRecord = EntityFactory.getCache().get("player1");
        if (playerRecord == null) throw new RuntimeException("Errore critico: risorsa player1 mancante!");

        PlayerModel player = new PlayerModel(x, y, playerRecord);
        model.setPlayer(player);
        return player;
    }

    public EntityModel spawnWorm(double x, double y) {
        return WormAssembler.assemble("iscat_worm_head", "iscat_worm_body_part", "iscat_worm_tail", 10, x, y, model, controller);
    }

    /**
     * Metodo funzionale per istanziare e accoppiare simultaneamente un'entità al suo controller logico.
     */
    public <M extends AbstractPhysicalEntityModel> M spawnWithController(
            BiFunction<Double, Double, M> modelFactory, Function<M, IEntityController> controllerFactory, double x, double y) {

        if (model == null || controller == null) return null;

        M entityModel = modelFactory.apply(x, y);
        spawnEntity(entityModel);

        if (controllerFactory != null) {
            controller.addEntityController(controllerFactory.apply(entityModel));
        }
        return entityModel;
    }

    public <T extends AbstractPhysicalEntityModel> T spawnEntity(T entity) {
        if (entity instanceof ProjectileModel projectile) {
            projectile.setUniverseModel(this.model);
        }
        model.addEntity(entity);
        return entity;
    }

    private Object spawnCustomRuntimeEntity(String id, double x, double y) {
        EntityModel jsonEntity = EntityFactory.spawn(id, x, y, model, controller);
        if (jsonEntity != null && jsonEntity.getEntityRecord().isBoss()) {
            jsonEntity.setWaveController(this.waveController);
        }
        return jsonEntity;
    }

    public UniverseModel getUniverseModel() { return model; }
}