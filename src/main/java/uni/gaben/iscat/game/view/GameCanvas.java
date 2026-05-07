package uni.gaben.iscat.game.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.model.Space;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.model.entities.Player;
import uni.gaben.iscat.game.model.entities.Star;
import uni.gaben.iscat.game.model.GameSettings;

import java.util.Objects;

/**
 * Vista di gioco: disegna il modello corrente ogni frame.
 * Nessuna logica di gioco qui — solo rendering.
 */
public class GameCanvas extends Canvas {

    public static final double TILE_SIZE           = GameSettings.DIMENSIONE_TILE;
    public static final double SPRITE_NORTH_OFFSET = GameSettings.OFFSET_NORD_SPRITE;

    private static final Image PLAYER_SPRITE = new Image(
            Objects.requireNonNull(GameCanvas.class.getResourceAsStream("/uni/gaben/iscat/sprites/battle_ship_1.png")));

    private final GameModel model;
    private final Space     space;

    public GameCanvas(GameModel model) {
        this.model = model;
        this.space = new Space(0, 0);
        setOnMouseClicked(e -> requestFocus());
    }

    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        setWidth(getParent().getScene().getWidth());
        setHeight(getParent().getScene().getHeight());
        space.heightProperty().bind(heightProperty().asObject()
                .map(Number::intValue));
        space.widthProperty().bind(widthProperty().asObject()
                .map(Number::intValue));
        render();
    }

    @Override public boolean isResizable() { return true; }

    /** Chiamato dal game loop ogni frame. */
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getParent().getScene().getWidth(), getParent().getScene().getHeight());
        gc.setImageSmoothing(false);

        disegnaStelle(gc);
        disegnaGiocatore(gc);
    }

    private void disegnaStelle(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        for (Star star : space.stars) {
            double size = star.getSize();
            gc.fillOval(star.getX(), star.getY(), size, size);
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

    public Space getSpace() { return space; }

    /** Disegna il contatore FPS in alto a sinistra. */
    public void drawFPS(int fps) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillText("FPS: " + fps, 10, 20);
    }
}
