package uni.gaben.iscat.game.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.npcs.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.game.view.GameCanvas;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.utils.settings.GameSettings;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.controller.PlayerDodgeController;
import uni.gaben.iscat.game.components.entities.player.controller.PlayerMovementController;
import uni.gaben.iscat.game.components.entities.player.controller.PlayerShootingController;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Orchestratore del gioco.
 * Gestisce il loop e delega ogni responsabilità al proprio handler.
 *
 * Controllers:
 *   GamePauseController      — pausa, navigazione
 *   PlayerMovementController — spinta, direzione
 *   PlayerDodgeController    — scatto, impulso stelle
 *   PlayerShootingController — sparo
 *   GameScrollController     — scroll del mondo ai bordi
 *   GameFpsTracker           — calcolo FPS
 */
public class GameController {

    private final GameModel model;
    private GameCanvas canvas;
    private final InputHandler input;

    // Controllers
    private final GamePauseController     pause    = new GamePauseController();
    private final PlayerMovementController movement = new PlayerMovementController();
    private final PlayerDodgeController   dodge    = new PlayerDodgeController();
    private final PlayerShootingController shooting = new PlayerShootingController();
    private final GameScrollController    scroll   = new GameScrollController();
    private final GameFpsTracker          fps      = new GameFpsTracker();

    private final Random rand = new Random();
    private boolean playerSpawned = false;
    private AnimationTimer gameLoop;

    public GameController(GameModel model) {
        this.model = model;
        this.input = new InputHandler();
        initialize();
    }

    public void initialize() {
        // Diciamo al modello di chiamare setupEnemyAudio per ogni nuovo nemico
        model.setOnEnemySpawned(this::setupEnemyAudio);

        // Gestiamo anche i nemici già presenti (quelli di test)
        for (NpcModel existingEnemy : model.getEnemies()) {
            setupEnemyAudio(existingEnemy);
        }
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        pause.setFocusTarget(canvas);
    }

    public void attachInput(Scene scene) {
        input.setKeyEventHandlers(scene);
        input.setMouseEventHandlers(canvas);
    }

    public void setupPlayerAudio() {
        PlayerModel p = model.getPlayer();
        // Dash audio → PlayerDodgeController
        dodge.setOnScatto(() -> {
            int sfx = 1 + rand.nextInt(3);
            IscatAudioManager.getInstance().playSFX("fart_alt" + sfx);
        });
        // Shot spawn + audio → PlayerShootingController
        shooting.setOnSparo(model::spawnProjectile);
        shooting.setOnSparoSound(() -> IscatAudioManager.getInstance().playSFX("shoot"));
        // Hurt audio stays on the model (LivingEntityModel.onHurt is a minor violation
        // but acceptable until LivingEntityModel is refactored)
        p.setOnHurt(() -> IscatAudioManager.getInstance().playSFX("hurt"));
    }

    public void setupEnemyAudio(NpcModel enemy) {
        //System.out.println("[GameController] DEBUG: Sto collegando l'audio al nemico: " + enemy.getClass().getSimpleName());

        enemy.setOnHurt(() -> {
            IscatAudioManager.getInstance().playSFX("hurt");
        });

        enemy.setOnDeath(() -> {
            IscatAudioManager.getInstance().playSFX("explosion");
        });
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------

    private long lastFrameTime = 0;
    private double accumulator = 0.0;
    private static final double FIXED_DT = 1.0 / 60.0; // 60 updates per second
    private static final long NANOS_PER_SECOND = 1_000_000_000L;

    public void startLoop() {
        if (gameLoop != null) return;
        lastFrameTime = 0;
        accumulator = 0.0;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate delta time in seconds
                if (lastFrameTime == 0) {
                    lastFrameTime = now;
                    return;
                }
                
                double deltaSeconds = (now - lastFrameTime) / (double) NANOS_PER_SECOND;
                lastFrameTime = now;
                
                // Cap delta time to prevent spiral of death
                deltaSeconds = Math.min(deltaSeconds, 0.25);
                
                // Accumulate time
                accumulator += deltaSeconds;
                
                // Fixed timestep updates
                while (accumulator >= FIXED_DT) {
                    update();
                    accumulator -= FIXED_DT;
                }
                
                fps.update(now);
                canvas.render(fps.getFps(), scroll.getCameraX(), scroll.getCameraY());
            }
        };
        gameLoop.start();
    }

    public void stopLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
            gameLoop = null;
        }
    }

    // -------------------------------------------------------------------------
    // Update — one tick
    // -------------------------------------------------------------------------

    private void update() {
        pause.processInput(input);
        if (pause.isPaused()) return;

        if (!playerSpawned) spawnPlayerAtCenter();

        PlayerModel p = model.getPlayer();

        shooting.process(input, p);
        movement.applicaSpinta(input, p);
        boolean dashFired = dodge.process(input, p);

        model.update(GameSettings.DT);

        // Update camera to follow player
        scroll.process(p, canvas.getWidth(), canvas.getHeight());

        movement.aggiornaDirezione(input, p, scroll.getCameraX(), scroll.getCameraY());
        canvas.getSpace().update(p.getVelocity().x, p.getVelocity().y);

        if (dashFired) {
            dodge.applyStarImpulse(p, canvas.getSpace());
        }
    }

    // -------------------------------------------------------------------------
    // Spawn
    // -------------------------------------------------------------------------

    private void spawnPlayerAtCenter() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        PlayerModel p = model.getPlayer();
        p.setX(w / 2.0 - GameCanvas.TILE_SIZE / 2.0);
        p.setY(h / 2.0 - GameCanvas.TILE_SIZE / 2.0);
        // Snap camera to player immediately to avoid initial lerp impulse
        scroll.snapToPlayer(p, w, h);
        playerSpawned = true;
    }

    // -------------------------------------------------------------------------
    // Pause / navigation — delegated to GamePauseController
    // -------------------------------------------------------------------------

    public void setOnPauseToggle(Consumer<Boolean> callback) {
        pause.setOnPauseToggle(callback);
    }

    public void togglePause() { pause.toggle(); }

    public void exitToMainMenu() { pause.exitToMainMenu(); }
}
