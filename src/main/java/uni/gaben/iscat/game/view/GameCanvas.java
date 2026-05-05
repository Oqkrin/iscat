package uni.gaben.iscat.game.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.model.entities.GameModel;
import uni.gaben.iscat.game.model.entities.Player;

/**
 * Canvas di gioco (View) – responsabile esclusivamente del rendering.
 * Riceve il modello dall'esterno e disegna lo stato corrente.
 */
public class GameCanvas extends Canvas {

    // Dimensione base dei tile (32 pixel * fattore di scala 3)
    public static final int TILE_SIZE = 32 * 3;

    private final GameModel model;

    public GameCanvas(GameModel model) {
        this.model = model;

        // Rende il canvas ridimensionabile automaticamente con il layout
        setManaged(true);
        // Sfondo nero (opzionale, può essere gestito via CSS o qui)
        setOnMouseClicked(e -> requestFocus()); // per mantenere il focus se serve

        // Metodo 1: binding esplicito (alternativa al resize override)
        // widthProperty().addListener(obs -> setWidth(getWidth()));
        // heightProperty().addListener(obs -> setHeight(getHeight()));

        // Metodo 2 (migliore): override di resize() – usato sotto
    }

    /**
     * Chiamato automaticamente dal layout manager quando il contenitore
     * viene ridimensionato. Aggiorna la dimensione del buffer di disegno.
     */
    @Override
    public void resize(double width, double height) {
        super.resize(width, height);
        setWidth(width);
        setHeight(height);
        // Dopo il resize forziamo un redraw (opzionale)
        render();
    }

    @Override
    public boolean isResizable() {
        return true; // permetti al layout di ridimensionarlo
    }

    @Override
    public double prefWidth(double height) { return 800; }
    @Override
    public double prefHeight(double width) { return 600; }

    /**
     * Metodo principale di disegno. Viene chiamato dal game loop.
     */
    public void render() {
        GraphicsContext gc = getGraphicsContext2D();
        // Pulisce l'intera area
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setImageSmoothing(false);

        // Disegna il giocatore
        drawPlayer(gc);

        // In futuro: drawMonsters(gc); drawProjectiles(gc); ...
    }

    private void drawPlayer(GraphicsContext gc) {
        Player p = model.player;
        if (p == null || p.sprite == null) return;

        // Centro del giocatore (in pixel)
        double centerX = p.x + TILE_SIZE / 2.0;
        double centerY = p.y + TILE_SIZE / 2.0;

        gc.save();
        gc.translate(centerX, centerY);
        // L'angolo directionAngle è già calcolato dal controller
        gc.rotate(p.directionAngle + 90);   // +90 se lo sprite punta verso l'alto di default

        // Disegna lo sprite centrato
        gc.drawImage(p.sprite,
                -TILE_SIZE / 2.0, -TILE_SIZE / 2.0,
                TILE_SIZE, TILE_SIZE);
        gc.restore();
    }

    /**
     * Eventuale disegno di debug (FPS, griglia, ecc.)
     */
    public void drawFPS(int fps) {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.WHITE);
        gc.fillText("FPS: " + fps, 10, 20);
    }
}