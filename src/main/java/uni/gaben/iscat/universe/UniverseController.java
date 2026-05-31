package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormController;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.IEntityController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasTerminalVelocity;
import uni.gaben.iscat.universe.rendering.RenderRegistry;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.Updatable;

import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.player.PlayerController;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldController;

import uni.gaben.iscat.screens.game.controller.GameInputs;

import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller principale del motore di gioco (Universe).
 * Coordina il ciclo vitale di tutte le entità, l'elaborazione degli input del giocatore,
 * l'avanzamento dei sistemi di Intelligenza Artificiale, lo spawn procedurale degli ostacoli,
 * lo step del motore fisico e la sincronizzazione con i DAO del database SQLite per il salvataggio dei punteggi.
 */
public class UniverseController {

    private UniverseModel universeModel;
    private final PlayerController playerController;
    private ScoreDAO scoreDAO;
    private EnemyDAO enemyDAO;
    private final List<IEntityController> entityControllers = new ArrayList<>();
    private final StarfieldController starfieldController = new StarfieldController();
    private final UniverseWaveController universeWaveController = new UniverseWaveController();
    private final Cooldown asteroidCooldown = new Cooldown();

    private static final double ASTEROID_SPAWN_INTERVAL = 3.0;
    private static final int MAX_ACTIVE_ASTEROIDS = 30;
    private final Random random = new Random();

    private final List<IscatWormController> wormControllers = new ArrayList<>();

    public void addWormController(IscatWormController wormController) {
        wormControllers.add(wormController);
    }

    public UniverseController(UniverseModel universeModel) {
        this.universeModel = universeModel;
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.enemyDAO = IscatDB.getInstance().getEnemyDAO();
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    /**
     * Esegue l'aggiornamento logico completo (tick) di tutti i sistemi del mondo di gioco.
     * Viene invocato ciclicamente dal game loop principale.
     *
     * @param dt          Delta time (tempo trascorso in secondi dall'ultimo frame).
     * @param inputs      Involucro contenente lo stato corrente delle periferiche di input.
     * @param cameraModel Il modello geometrico della telecamera di gioco.
     */
    public void updatev(double dt, GameInputs inputs, CameraModel cameraModel) {
        PlayerModel player = universeModel.getPlayer();

        syncPlayerController(player);
        spawnAsteroids(dt, player);
        processPlayerInputs(player, inputs, cameraModel, dt);
        updateProjectiles(cameraModel, dt);
        updateEntities(dt);
        updateAI(dt);
        applyTerminalVelocityLimits();
        syncWormSegments();

        // Avanzamento delle equazioni di vincolo e della dinamica dei corpi rigidi (Dyn4J)
        universeModel.stepPhysics(dt);

        processEntityCleanup(player);
        updateCamera(player, cameraModel, dt);
    }

    private void syncPlayerController(PlayerModel player) {
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }
    }

    private void processPlayerInputs(PlayerModel player, GameInputs inputs, CameraModel cameraModel, double dt) {
        if (player == null) return;
        playerController.processInput(inputs, cameraModel, dt);
    }

    private void updateEntities(double dt) {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof Updatable updatable) {
                updatable.update(dt);
            }
        }
    }

    private void updateAI(double dt) {
        for (IEntityController controller : new ArrayList<>(entityControllers)) {
            controller.update(universeModel, dt);
        }
        for (IscatWormController w : wormControllers) {
            w.update(universeModel, dt);
        }
    }

    /**
     * Applica i massimi vettoriali di velocità consentiti (Terminal Velocity) ai corpi fisici.
     * Previene l'accelerazione incontrollata dovuta a forze cumulative del motore fisico.
     */
    private void applyTerminalVelocityLimits() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof HasTerminalVelocity entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 velocity = body.getLinearVelocity();
                if (velocity.getMagnitude() > maxSpeed) {
                    body.setLinearVelocity(
                            velocity.getNormalized().setMagnitude(maxSpeed)
                    );
                }
            }
        }
    }

    /**
     * Allinea l'orientamento grafico (rotazione dello sprite) dei segmenti del verme cinematica
     * basandosi sul loro vettore di movimento attuale.
     */
    private void syncWormSegments() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof IscatWormSegment segment && segment.getType() != IscatWormSegment.Type.HEAD) {
                Vector2 velocity = body.getLinearVelocity();
                if (velocity.getMagnitudeSquared() > 0.01) {
                    body.getTransform().setRotation(velocity.getDirection());
                }
            }
        }
    }

    /**
     * Aggiorna lo stato temporale dei proiettili e distrugge istantaneamente quelli
     * che si muovono oltre i confini del viewport visibile della telecamera (più un margine).
     */
    private void updateProjectiles(CameraModel cameraModel, double dt) {
        double zoom = cameraModel.getZoom();
        double worldLeft = cameraModel.getViewportLeftX();
        double worldRight = worldLeft + (cameraModel.getScreenWidth() / zoom);
        double worldTop = cameraModel.getViewportTopY();
        double worldBottom = worldTop + (cameraModel.getScreenHeight() / zoom);

        double margin = 200.0;
        worldLeft -= margin;
        worldRight += margin;
        worldTop -= margin;
        worldBottom += margin;

        for (AbstractProjectileModel p : new ArrayList<>(universeModel.getProjectiles())) {
            p.deltaToLife(-dt);
            if (p.shouldRemove()) continue;

            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());

            if (px < worldLeft || px > worldRight || py < worldTop || py > worldBottom) {
                p.kill(true);
            }
        }
    }

    /**
     * Ispeziona tutte le entità attive per identificare quelle rimosse o prive di punti vita.
     * In caso di abbattimento valido di un nemico da parte del giocatore (o per eccezioni di boss speciali),
     * assegna l'esperienza, aggiorna il punteggio di sessione e persiste l'incremento di uccisioni su SQLite.
     */
    private void processEntityCleanup(PlayerModel player) {
        List<AbstractEntityModel> toRemove = new ArrayList<>();

        for (AbstractEntityModel entity : new ArrayList<>(universeModel.getEntities())) {
            if (entity == null) continue;
            boolean shouldRemove = false;

            if (entity.shouldRemove()) {
                shouldRemove = true;
            } else if (entity instanceof LivingEntityModel living) {
                if (living.getLife() <= 0) {
                    if (!living.shouldRemove()) {
                        living.kill();
                    }
                    shouldRemove = living.shouldRemove();
                }
            }

            if (shouldRemove) {
                toRemove.add(entity);
                if (entity instanceof LivingEntityModel living &&
                        living != player &&
                        living.getXpReward() > 0 &&
                        player != null) {

                    String entityKey = living.getEntityKey();
                    String cleanKey = entityKey != null ? entityKey.toLowerCase().trim() : "";

                    // Condizione di sblocco speciale: Healer e Master bypassano il vincolo del colpo finale da proiettile
                    boolean isSpecialEnemy = cleanKey.equals("iscat_healer") || cleanKey.equals("iscat_master");

                    if (living.isKilledByProjectile() || isSpecialEnemy) {
                        player.addXp(living.getXpReward());
                        SessionScoreTracker.getInstance().addScore((int) living.getXpReward());

                        // Persistenza sul database SQLite dell'utente autenticato
                        SessionUser user = SessionManager.getInstance().getCurrentUser();
                        if (user != null) {
                            scoreDAO.increment(user.id(), "Deaths", 1);
                            if (!cleanKey.isEmpty()) {
                                enemyDAO.incrementKill(user.id(), cleanKey);
                            }
                        }
                    }
                }
            }
        }

        // Rimozione fisica dal motore d'animazione, dal mondo e disattivazione del thread cerebrale dell'IA
        for (AbstractEntityModel entity : toRemove) {
            universeModel.removeEntity(entity);
            RenderRegistry.getInstance().removeRenderer(entity);

            entityControllers.removeIf(
                    ctrl -> (ctrl instanceof Brain<?> brain && brain.getEntity() == entity)
            );
        }
    }

    /**
     * Aggiorna la posizione del target della telecamera applicando un effetto di anticipo visivo (Spring)
     * proporzionale al vettore di velocità e orientamento del giocatore.
     */
    private void updateCamera(PlayerModel player, CameraModel cameraModel, double dt) {
        if (player != null) {
            double targetX = UU.mToPx(player.getTransform().getTranslationX());
            double targetY = UU.mToPx(player.getTransform().getTranslationY());
            double velocityMag = player.getLinearVelocity().getMagnitude();
            cameraModel.getSpringX().setTarget(
                    targetX + Math.sin(player.getTransform().getRotationAngle()) * velocityMag
            );
            cameraModel.getSpringY().setTarget(
                    targetY + Math.cos(player.getTransform().getRotationAngle()) * velocityMag
            );
        }
        cameraModel.getSpringX().update(dt);
        cameraModel.getSpringY().update(dt);
    }

    /**
     * Gestisce lo spawn ciclico procedurale di gruppi di asteroidi in una corona circolare
     * esterna rispetto alla vista del giocatore, conferendo loro velocità vettoriali di deriva.
     */
    private void spawnAsteroids(double dt, PlayerModel player) {
        asteroidCooldown.update(dt);
        if (!asteroidCooldown.isReady()) return;
        asteroidCooldown.start(ASTEROID_SPAWN_INTERVAL);

        List<AsteroidModel> activeAsteroids = universeModel.getEntitiesOfType(AsteroidModel.class);
        if (activeAsteroids.size() >= MAX_ACTIVE_ASTEROIDS || player == null) return;

        double playerX = UU.mToPx(player.getTransform().getTranslationX());
        double playerY = UU.mToPx(player.getTransform().getTranslationY());
        double angle = Math.random() * Math.PI * 2.0;
        double dist = 900.0 + Math.random() * 600.0;
        double cx = playerX + Math.cos(angle) * dist;
        double cy = playerY + Math.sin(angle) * dist;
        int count = 3 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            double offsetAngle = Math.random() * Math.PI * 2.0;
            double offsetDist = Math.random() * 150.0;
            double ax = cx + Math.cos(offsetAngle) * offsetDist;
            double ay = cy + Math.sin(offsetAngle) * offsetDist;
            double radius = 15.0 + Math.random() * 75.0;
            AsteroidModel asteroid = new AsteroidModel(ax, ay, radius);
            double driftAngle = Math.random() * Math.PI * 2.0;
            double speed = UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN
                    + Math.random() * (UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MAX - UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN);
            asteroid.setLinearVelocity(new Vector2(Math.cos(driftAngle) * speed, Math.sin(driftAngle) * speed));
            UniverseSpawner.getInstance().spawnEntity(asteroid);
        }
    }

    public void addEntityController(IEntityController controller) {
        entityControllers.add(controller);
    }

    public UniverseModel getUniverseModel() {
        return universeModel;
    }

    public StarfieldController getStarfieldController() {
        return starfieldController;
    }
}