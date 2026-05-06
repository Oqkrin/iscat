package uni.gaben.iscat.game.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import uni.gaben.iscat.game.model.entities.GameModel;
import uni.gaben.iscat.game.model.entities.Player;
import uni.gaben.iscat.game.model.physics.InputDirection;
import uni.gaben.iscat.game.model.physics.Vec2;
import uni.gaben.iscat.game.view.GameCanvas;
import uni.gaben.iscat.utils.settings.GameSettings;

/**
 * Controller di gioco: traduce l'input in forze fisiche e fa avanzare il mondo.
 *
 * La spinta usa lerp (stabile con dt=1.0).
 * Lo sfondo stellato usa una molla fisica per il parallasse.
 * Il dodge propaga un impulso visivo alle stelle.
 */
public class GameController {

    private final GameModel    model;
    private final GameCanvas   canvas;
    private final InputHandler input;

    /** Forza di spinta corrente, interpolata via lerp ogni tick. */
    private double currentThrustX = 0;
    private double currentThrustY = 0;

    /** Angolo del dodge appena eseguito, per propagare l'impulso alle stelle. */
    private boolean dodgeJustFired = false;

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

    /** Un tick: input → fisica → stelle. */
    public void update() {
        Player p = model.getPlayer();

        applicaSpinta(p);
        applicaDodge(p);
        aggiornaDirezione(p);

        model.update(GameSettings.DT);

        // parallasse stelle — molla fisica
        Vec2 vel = p.getVelocity();
        canvas.getSpace().update(vel.x, vel.y);

        // impulso visivo al dodge
        if (dodgeJustFired) {
            double rad = Math.toRadians(p.getDirectionAngle());
            canvas.getSpace().applyImpulse(
                    Math.cos(rad) * GameSettings.DODGE_IMPULSE * GameSettings.DODGE_STAR_IMPULSE_FACTOR,
                    Math.sin(rad) * GameSettings.DODGE_IMPULSE * GameSettings.DODGE_STAR_IMPULSE_FACTOR
            );
            dodgeJustFired = false;
        }
    }

    // --- input → forze ---

    /**
     * Lerp verso la forza target: accelerazione morbida senza instabilità numerica.
     * La molla fisica per la spinta esplode con dt=1.0 e stiffness alta;
     * il lerp è equivalente ma incondizionatamente stabile.
     */
    private void applicaSpinta(Player p) {
        double targetX = 0, targetY = 0;

        if (input.up)    { targetX += InputDirection.UP.dx;    targetY += InputDirection.UP.dy; }
        if (input.down)  { targetX += InputDirection.DOWN.dx;  targetY += InputDirection.DOWN.dy; }
        if (input.left)  { targetX += InputDirection.LEFT.dx;  targetY += InputDirection.LEFT.dy; }
        if (input.right) { targetX += InputDirection.RIGHT.dx; targetY += InputDirection.RIGHT.dy; }

        targetX *= GameSettings.PLAYER_THRUST_FORCE;
        targetY *= GameSettings.PLAYER_THRUST_FORCE;

        currentThrustX = GameSettings.THRUST_EASING.apply(currentThrustX, targetX, GameSettings.THRUST_LERP);
        currentThrustY = GameSettings.THRUST_EASING.apply(currentThrustY, targetY, GameSettings.THRUST_LERP);

        if (Math.abs(currentThrustX) > 0.001 || Math.abs(currentThrustY) > 0.001) {
            p.applyForce(new Vec2(currentThrustX, currentThrustY));
        }
    }

    /** Dodge: impulso fisico sulla nave + flag per l'impulso visivo alle stelle. */
    private void applicaDodge(Player p) {
        boolean wasReady = p.isDodgeReady();
        if (input.consumeDodge()) p.requestDodge();
        p.processDodge();
        // se era pronto e ora non lo è più, il dodge è appena scattato
        if (wasReady && !p.isDodgeReady()) dodgeJustFired = true;
    }

    /** Ruota la nave verso il cursore del mouse. */
    private void aggiornaDirezione(Player p) {
        double cx = p.getX() + GameCanvas.TILE_SIZE / 2.0;
        double cy = p.getY() + GameCanvas.TILE_SIZE / 2.0;
        p.setDirectionAngle(Math.toDegrees(Math.atan2(input.mouseY - cy, input.mouseX - cx)));
    }

    // --- loop ---

    /** Avvia il loop JavaFX. */
    public void startLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) { update(); canvas.render(); }
        };
        timer.start();
    }
}
