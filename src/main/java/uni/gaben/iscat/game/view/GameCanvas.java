package uni.gaben.iscat.game.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import uni.gaben.iscat.game.model.entities.enemies.IscatBomber;
import uni.gaben.iscat.game.model.settings.VisualSettings;
import uni.gaben.iscat.game.model.space.Space;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.model.entities.Player;
import uni.gaben.iscat.game.model.entities.Star;

import java.util.Objects;

/**
 * Vista di gioco: disegna il modello corrente ogni frame.
 * Nessuna logica di gioco qui — solo rendering.
 */
public class GameCanvas extends Canvas {

    public static final double TILE_SIZE           = VisualSettings.DIMENSIONE_TILE;
    public static final double SPRITE_NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;

    private static final Image PLAYER_SPRITE = new Image(
            Objects.requireNonNull(GameCanvas.class.getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));

    private static final Image BOMBER_SPRITE = new Image(
            Objects.requireNonNull(GameCanvas.class.getResourceAsStream("/uni/gaben/iscat/sprites/IscatBomber.png")));

    private final GameModel model;
    private final Space space;

    public GameCanvas(GameModel model) {
        this.model = model;
        this.space = new Space(0, 0);
        // Space tracks canvas size
        space.widthProperty().bind(widthProperty().asObject().map(Number::intValue));
        space.heightProperty().bind(heightProperty().asObject().map(Number::intValue));
    }

    /** Chiamato dal game loop ogni frame. */
    public void render(int currentFps) {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        disegnaStelle(gc);
        disegnaEnemies(gc);
        disegnaGiocatore(gc);

        if (VisualSettings.MOSTRA_FPS) {
            drawFPS(gc, currentFps);
        }
    }

    private void disegnaStelle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        for (Star star : space.stars) {
            double size = star.getSize();
            gc.fillRect(star.getX(), star.getY(), size, size);
        }
    }

    private void disegnaGiocatore(GraphicsContext gc) {
        Player p = model.getPlayer();
        if (p == null) return;
        double cx = p.getX() + TILE_SIZE / 2.0;
        double cy = p.getY() + TILE_SIZE / 2.0;
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(p.getDirectionAngle() + SPRITE_NORTH_OFFSET);
        gc.drawImage(PLAYER_SPRITE, -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);
        gc.restore();
    }

    private void disegnaEnemies(GraphicsContext gc) {
        for (var enemy : model.getEnemies()) {
            if (enemy instanceof IscatBomber) {
                double cx = enemy.getX() + TILE_SIZE / 2.0;
                double cy = enemy.getY() + TILE_SIZE / 2.0;
                gc.save();
                gc.translate(cx, cy);
                gc.rotate(enemy.getDirectionAngle() + SPRITE_NORTH_OFFSET);
                gc.drawImage(BOMBER_SPRITE, -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);
                gc.restore();
            } else {
                gc.setFill(Color.RED);
                gc.fillOval(enemy.getX(), enemy.getY(), TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawFPS(GraphicsContext gc, int fps) {
        gc.save();
        gc.setFill(Color.LIME);
        gc.setFont(Font.font("Miracode", 14));
        gc.fillText("FPS: " + fps, 10, 25);
        gc.restore();
    }

    public Space getSpace() { return space; }
}
