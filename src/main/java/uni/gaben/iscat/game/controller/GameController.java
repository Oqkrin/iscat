package uni.gaben.iscat.game.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.model.GameSettings;
import uni.gaben.iscat.game.model.entities.Player;
import uni.gaben.iscat.game.model.physics.InputDirection;
import uni.gaben.iscat.game.model.physics.Vec2;
import uni.gaben.iscat.game.view.GameCanvas;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Controller di gioco: traduce l'input in forze fisiche e fa avanzare il mondo.
 * La spinta usa lerp con easing configurabile.
 * Lo scatto propaga un impulso visivo alle stelle.
 */
public class GameController {

    private final GameModel    model;
    private final GameCanvas   canvas;
    private final InputHandler input;

    // Logica Pause Menu
    private boolean paused = false;
    private Consumer<Boolean> onPauseToggle; // contiene l'azione da eseguire in caso di pausa
    private Runnable onExitToMenu;

    // FPS tracking (rolling average over 60 frames)
    private static final int FPS_WINDOW = 60;
    private final long[] frameTimes = new long[FPS_WINDOW];
    private int  frameIndex = 0;
    private int  frameCount = 0;
    private long lastFrame  = 0;
    private long sumNs      = 0;
    private int  currentFps = 0;

    private double spintaCorrenteX = 0;
    private double spintaCorrenteY = 0;
    private boolean scattoAppenaEseguito = false;
    private final Random rand = new Random();
    
    // Game loop control
    private AnimationTimer gameLoop;

    public GameController(GameModel model, GameCanvas canvas) {
        this.model  = model;
        this.canvas = canvas;
        this.input  = new InputHandler();
    }

    /** Collega tastiera alla scena e mouse al canvas. */
    public void attachInput(Scene scene) {
        input.setKeyEventHandlers(scene);
        input.setMouseEventHandlers(canvas);
    }

    /**
     * Collega il callback audio al player.
     * Chiamato da GameScene.onShow() dopo che i suoni sono stati caricati.
     */
    public void setupPlayerAudio() {
        model.getPlayer().setOnScatto(() -> {
            int sfx = 1 + rand.nextInt(3);
            IscatAudioManager.getInstance().playSFX("fart_alt" + sfx);
        });
    }

    /** Un tick: input → fisica → stelle. */
    public void update() {
        // Ascolta se ESC venga premuto
        if (input.consumePause()) {
            togglePause();
        }

        // Se il gioco in pausa ci fermiamo qui
        if (paused) return;

        // Logica del gioco non in pausa
        Player p = model.getPlayer();

        applicaSpinta(p);
        applicaScatto(p);
        aggiornaDirezione(p);

        model.update(GameSettings.DT);

        Vec2 vel = p.getVelocity();
        canvas.getSpace().update(vel.x, vel.y);

        if (scattoAppenaEseguito) {
            double rad = Math.toRadians(p.getDirectionAngle());
            canvas.getSpace().applyImpulse(
                    Math.cos(rad) * GameSettings.IMPULSO_SCATTO * GameSettings.FATTORE_IMPULSO_STELLE,
                    Math.sin(rad) * GameSettings.IMPULSO_SCATTO * GameSettings.FATTORE_IMPULSO_STELLE
            );
            scattoAppenaEseguito = false;
        }
    }

    // =========================================
    // PAUSE MENU STUFF
    // =========================================

    // Metodo che la scena chiama l'azione da eseguire
    public void setOnPauseToggle(Consumer<Boolean> callback) {
        this.onPauseToggle = callback;
    }

    public void togglePause() {
        paused = !paused;

        if (onPauseToggle != null) {
            onPauseToggle.accept(paused);
        }

        // Ridiamo il focus al canvas per gli input del player
        if (!paused) {
            canvas.requestFocus();
        }
    }

    public void setOnExitToMenu(Runnable callback) {
        this.onExitToMenu = callback;
    }

    public void exitToMainMenu() {
        if (onExitToMenu != null) {
            onExitToMenu.run();
        }
    }

    // --- input → forze ---

    private void applicaSpinta(Player p) {
        double targetX = 0;
        double targetY = 0;

        if (input.up)    { targetX += InputDirection.UP.dx;    targetY += InputDirection.UP.dy; }
        if (input.down)  { targetX += InputDirection.DOWN.dx;  targetY += InputDirection.DOWN.dy; }
        if (input.left)  { targetX += InputDirection.LEFT.dx;  targetY += InputDirection.LEFT.dy; }
        if (input.right) { targetX += InputDirection.RIGHT.dx; targetY += InputDirection.RIGHT.dy; }

        targetX *= GameSettings.FORZA_SPINTA;
        targetY *= GameSettings.FORZA_SPINTA;

        spintaCorrenteX = GameSettings.EASING_SPINTA.apply(spintaCorrenteX, targetX, GameSettings.LERP_SPINTA);
        spintaCorrenteY = GameSettings.EASING_SPINTA.apply(spintaCorrenteY, targetY, GameSettings.LERP_SPINTA);

        if (Math.abs(spintaCorrenteX) > 0.001 || Math.abs(spintaCorrenteY) > 0.001) {
            p.applyForce(new Vec2(spintaCorrenteX, spintaCorrenteY));
        }
    }

    private void applicaScatto(Player p) {
        boolean eraDisponibile = p.isScattoDisponibile();
        if (input.consumeDodge()) p.richiestaScatto();
        p.elaboraScatto();
        if (eraDisponibile && !p.isScattoDisponibile()) scattoAppenaEseguito = true;
    }

    /** Ruota la nave verso il cursore del mouse. */
    private void aggiornaDirezione(Player p) {
        double cx = p.getX() + GameCanvas.TILE_SIZE / 2.0;
        double cy = p.getY() + GameCanvas.TILE_SIZE / 2.0;
        p.setDirectionAngle(Math.toDegrees(Math.atan2(input.mouseY - cy, input.mouseX - cx)));
    }

    public void startLoop() {
        // Don't start if already running
        if (gameLoop != null) {
            return;
        }
        
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate FPS using rolling average
                if (lastFrame > 0) {
                    long dt = now - lastFrame;
                    sumNs -= frameTimes[frameIndex];   // Remove oldest value
                    frameTimes[frameIndex] = dt;
                    sumNs += dt;                       // Add new value
                    frameIndex = (frameIndex + 1) % FPS_WINDOW;
                    if (frameCount < FPS_WINDOW) frameCount++;
                    currentFps = (int) (1_000_000_000L * frameCount / sumNs);
                }
                lastFrame = now;
                
                // Update game logic and render
                update();
                canvas.render(currentFps);
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
}
