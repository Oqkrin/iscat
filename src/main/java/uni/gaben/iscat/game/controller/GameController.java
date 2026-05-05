package uni.gaben.iscat.game.controller;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import uni.gaben.iscat.game.model.entities.GameModel;
import uni.gaben.iscat.game.model.entities.Player;
import uni.gaben.iscat.game.view.GameCanvas;

import static javafx.scene.input.KeyCode.W;

// GameController.java – orchestra il tutto
public class GameController {
    private final GameModel model;
    private final GameCanvas canvas;
    private final InputHandler input;

    public GameController(GameModel model, GameCanvas canvas) {
        this.model = model;
        this.canvas = canvas;
        this.input = new InputHandler();
    }

    public void attachInput(Scene scene) {
        input.setKeyEventHandlers(scene);
    }

    public void update() {
        Player p = model.player;
        if (input.up)    p.y -= p.speed;
        if (input.down)  p.y += p.speed;
        if (input.left)  p.x -= p.speed;
        if (input.right) p.x += p.speed;

        // Calcola angolo player → mouse
        double dx = input.mouseX - (p.x + GameCanvas.TILE_SIZE / 2.0);
        double dy = input.mouseY - (p.y + GameCanvas.TILE_SIZE / 2.0);
        p.directionAngle = Math.toDegrees(Math.atan2(dy, dx));
    }

    public void startLoop() {
        AnimationTimer timer = new AnimationTimer() {
            @Override public void handle(long now) {
                update();
                canvas.render();
            }
        };
        timer.start();
    }
}