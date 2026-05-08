package uni.gaben.iscat.game;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.input.InputHandler;
import uni.gaben.iscat.game.input.GameFpsTracker;
import uni.gaben.iscat.game.input.GamePauseController;
import uni.gaben.iscat.game.input.GameWrapController;
import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.player.controller.PlayerDodgeController;
import uni.gaben.iscat.game.player.controller.PlayerMovementController;
import uni.gaben.iscat.game.player.controller.PlayerShootingController;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Orchestratore del gioco.
 * Gestisce il loop e delega ogni responsabilità al proprio handler.
 *
 * Controllers:
 *   GamePauseController    — pausa, navigazione
 *   PlayerMovementController — spinta, direzione
 *   PlayerDodgeController    — scatto, impulso stelle
 *   PlayerShootingController — sparo
 *   GameWrapController     — wrapping ai bordi
 *   GameFpsTracker         — calcolo FPS
 */
public class GameController {

    private final GameModel      model;
    private       GameCanvas     canvas;
    private final InputHandler   input;

    // Controllers
    private final GamePauseController    pause    = new GamePauseController();
    private final PlayerMovementController movement = new PlayerMovementController();
    private final PlayerDodgeController    dodge    = new PlayerDodgeController();
    private final PlayerShootingController shooting = new PlayerShootingController();
    private final GameWrapController       wrap     = new GameWrapController();
    private final GameFpsTracker           fps      = new GameFpsTracker();

    private final Random rand = new Random();
    private boolean playerSpawned = false;
    private AnimationTimer gameLoop;

    public GameController(GameModel model) {
        this.model = model;
        this.input = new InputHandler();
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
        p.setOnScatto(() -> {
            int sfx = 1 + rand.nextInt(3);
            IscatAudioManager.getInstance().playSFX("fart_alt" + sfx);
        });
        p.setOnSparoSound(() -> IscatAudioManager.getInstance().playSFX("shoot"));
        p.setOnHurt(()       -> IscatAudioManager.getInstance().playSFX("hurt"));
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------

    public void startLoop() {
        if (gameLoop != null) return;
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                fps.update(now);
                update();
                canvas.render(fps.getFps());
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

        boolean wrapped = wrap.wrap(p, canvas.getWidth(), canvas.getHeight());
        if (!wrapped) movement.aggiornaDirezione(input, p);

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
