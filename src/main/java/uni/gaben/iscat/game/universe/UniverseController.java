package uni.gaben.iscat.game.universe;

import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.game.view.camera.CameraModel;
import uni.gaben.iscat.game.controller.GameInputs;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.player.PlayerController;
import uni.gaben.iscat.game.universe.starfield.StarfieldController;
import uni.gaben.iscat.game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.utils.Cooldown;

import java.util.ArrayList;
import java.util.List;

public class UniverseController {

    private UniverseModel universeModel;
    private final PlayerController playerController;
    private final List<AiController> aiControllers = new ArrayList<>();
    private final StarfieldController starfieldController = new StarfieldController();

    // --- Dynamic Asteroid Spawn Config ---
    private final Cooldown asteroidCooldown = new Cooldown();
    private static final double ASTEROID_SPAWN_INTERVAL = 3.0; // check and spawn every 3 seconds
    private static final int MAX_ACTIVE_ASTEROIDS = 30; // max asteroids in the world

    public UniverseController(UniverseModel universeModel) {
        this.universeModel = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void addAiController(AiController controller) {
        this.aiControllers.add(controller);
    }

    /**
     * Flusso di aggiornamento principale (Engine Tick) coordinato dal Controller.
     */
    public void updatev(double dt, GameInputs inputs, CameraModel cameraModel) {
        PlayerModel player = universeModel.getPlayer();
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }

        // 0. Dynamic off-camera Asteroid Spawning
        asteroidCooldown.update(dt);
        if (asteroidCooldown.isReady()) {
            asteroidCooldown.start(ASTEROID_SPAWN_INTERVAL);
            List<AsteroidModel> activeAsteroids =
                    universeModel.getEntitiesOfType(AsteroidModel.class);

            if (activeAsteroids.size() < MAX_ACTIVE_ASTEROIDS && player != null) {
                double playerX = UU.mToPx(player.getTransform().getTranslationX());
                double playerY = UU.mToPx(player.getTransform().getTranslationY());

                // Pick an angle and distance off-camera (between 900 and 1500 pixels away)
                double angle = Math.random() * Math.PI * 2.0;
                double dist = 900.0 + Math.random() * 600.0;

                double cx = playerX + Math.cos(angle) * dist;
                double cy = playerY + Math.sin(angle) * dist;

                // Spawn a clump of 3-5 asteroids
                int count = 3 + new java.util.Random().nextInt(3);
                for (int i = 0; i < count; i++) {
                    double offsetAngle = Math.random() * Math.PI * 2.0;
                    double offsetDist = Math.random() * 150.0;

                    double ax = cx + Math.cos(offsetAngle) * offsetDist;
                    double ay = cy + Math.sin(offsetAngle) * offsetDist;

                    // Some very large, some medium
                    double radius = 15.0 + Math.random() * 75.0; // radius 15-90px, diameter 30-180px

                    AsteroidModel ast = new AsteroidModel(ax, ay, radius);

                    double driftAngle = Math.random() * Math.PI * 2.0;
                    double speed = 0.5 + Math.random() * 2.0;
                    ast.setLinearVelocity(new org.dyn4j.geometry.Vector2(
                            Math.cos(driftAngle) * speed,
                            Math.sin(driftAngle) * speed
                    ));

                    UniverseSpawner.getInstance().spawnEntity(ast);
                }
            }
        }

        // 1. Processamento degli Input dell'utente sul Player
        if (player != null) {
            playerController.processInput(
                    inputs,
                    cameraModel.getViewportLeftX(),
                    cameraModel.getViewportTopY(),
                    dt
            );
        }

        // 2. Aggiornamento dei proiettili (durata di vita e culling fuori dalla camera)
        double left = cameraModel.getViewportLeftX() - 200.0;
        double right = cameraModel.getViewportLeftX() + cameraModel.getScreenWidth() + 200.0;
        double top = cameraModel.getViewportTopY() - 200.0;
        double bottom = cameraModel.getViewportTopY() + cameraModel.getScreenHeight() + 200.0;

        for (AbstractProjectileModel p : new ArrayList<>(universeModel.getProjectiles())) {
            p.setLifespan(p.getLifespan() - dt);
            if (p.getLifespan() <= 0) {
                p.kill(true);
            } else {
                double px = UU.mToPx(p.getTransform().getTranslationX());
                double py = UU.mToPx(p.getTransform().getTranslationY());
                if (px < left || px > right || py < top || py > bottom) {
                    p.kill(true);
                }
            }
        }

        // 3. Aggiornamento delle Intelligenze Artificiali degli NPC
        for (AiController ai : aiControllers) {
            ai.aiUpdate(universeModel, dt);
        }

        // 4. Avanzamento sincronizzato del modello fisico (Risoluzione accoppiata)
        // Utilizziamo update(dt) per garantire che l'override del nostro modello venga eseguito
        universeModel.updatev(dt);

        // 5. Sincronizzazione MVC Post-Physics: Rimozione dei corpi morti prima del render visivo
        universeModel.processEntityCleanup();

        // 6. Allineamento molle e aggiornamento della Telecamera (Conversione Metri -> Pixel)
        if (player != null) {
            double targetX = UU.mToPx(player.getTransform().getTranslationX());
            double targetY = UU.mToPx(player.getTransform().getTranslationY());

            cameraModel.getSpringX().setTarget(targetX);
            cameraModel.getSpringY().setTarget(targetY);
        }

        cameraModel.getSpringX().update(dt);
        cameraModel.getSpringY().update(dt);
    }

    public UniverseModel getUniverseModel() { return universeModel; }
    public StarfieldController getStarfieldController() { return starfieldController; }


}