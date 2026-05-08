package uni.gaben.iscat.game.player.controller;

import uni.gaben.iscat.game.input.InputHandler;
import uni.gaben.iscat.game.GameSettings;
import uni.gaben.iscat.game.GameCanvas;
import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.physics.InputDirection;
import uni.gaben.iscat.game.physics.Vec2;
import uni.gaben.iscat.game.player.PlayerSettings;

/**
 * Traduce l'input direzionale in forze fisiche sul giocatore,
 * e aggiorna la direzione della nave verso il cursore del mouse.
 */
public class PlayerMovementController {

    private double spintaCorrenteX = 0;
    private double spintaCorrenteY = 0;

    /** Applica la spinta lerp-smoothed in base ai tasti premuti. */
    public void applicaSpinta(InputHandler input, PlayerModel p) {
        double targetX = 0;
        double targetY = 0;

        if (input.up)    { targetX += InputDirection.UP.dx;    targetY += InputDirection.UP.dy; }
        if (input.down)  { targetX += InputDirection.DOWN.dx;  targetY += InputDirection.DOWN.dy; }
        if (input.left)  { targetX += InputDirection.LEFT.dx;  targetY += InputDirection.LEFT.dy; }
        if (input.right) { targetX += InputDirection.RIGHT.dx; targetY += InputDirection.RIGHT.dy; }

        targetX *= PlayerSettings.FORZA_SPINTA;
        targetY *= PlayerSettings.FORZA_SPINTA;

        spintaCorrenteX = GameSettings.EASING_SPINTA.apply(spintaCorrenteX, targetX, GameSettings.LERP_SPINTA);
        spintaCorrenteY = GameSettings.EASING_SPINTA.apply(spintaCorrenteY, targetY, GameSettings.LERP_SPINTA);

        if (Math.abs(spintaCorrenteX) > 0.001 || Math.abs(spintaCorrenteY) > 0.001) {
            p.applyForce(new Vec2(spintaCorrenteX, spintaCorrenteY));
        }
    }

    /** Ruota la nave verso il cursore del mouse. */
    public void aggiornaDirezione(InputHandler input, PlayerModel p) {
        double cx = p.getX() + GameCanvas.TILE_SIZE / 2.0;
        double cy = p.getY() + GameCanvas.TILE_SIZE / 2.0;
        p.setDirectionAngle(Math.toDegrees(Math.atan2(input.mouseY - cy, input.mouseX - cx)));
    }
}
