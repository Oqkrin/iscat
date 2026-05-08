package uni.gaben.iscat.game;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import uni.gaben.iscat.game.enemies.EnemyModel;
import uni.gaben.iscat.game.enemies.EnemyView;
import uni.gaben.iscat.game.enemies.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.game.enemies.iscat_bomber.IscatBomberView;
import uni.gaben.iscat.game.player.PlayerView;
import uni.gaben.iscat.game.projectile.ProjectileView;
import uni.gaben.iscat.game.settings.VisualSettings;
import uni.gaben.iscat.game.space.SpaceModel;
import uni.gaben.iscat.game.space.SpaceView;

/**
 * Coordinatore del rendering di gioco.
 * Non contiene logica di disegno — delega a renderer specializzati.
 *
 * Ordine di rendering (back to front):
 *   1. Sfondo (stelle)
 *   2. Proiettili
 *   3. Nemici
 *   4. Giocatore
 *   5. HUD (FPS)
 */
public class GameCanvas extends Canvas {

    public static final double TILE_SIZE = VisualSettings.DIMENSIONE_TILE;

    private final GameModel model;
    private final SpaceModel space;

    // Renderers — one per entity type, stateless, loaded once
    private final SpaceView      spaceRenderer      = new SpaceView();
    private final PlayerView     playerRenderer     = new PlayerView();
    private final IscatBomberView bomberRenderer    = new IscatBomberView();
    private final ProjectileView  projectileRenderer = new ProjectileView();
    private final EnemyView       fallbackRenderer   = new EnemyView();

    public GameCanvas(GameModel model) {
        this.model = model;
        this.space = new SpaceModel(0, 0);
        space.widthProperty().bind(widthProperty().asObject().map(Number::intValue));
        space.heightProperty().bind(heightProperty().asObject().map(Number::intValue));
    }

    /** Called by the game loop every frame. */
    public void render(int currentFps) {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();

        // Clear
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        // 1. Background
        spaceRenderer.draw(gc, space);

        // 2. Projectiles
        for (var projectile : model.getProjectiles()) {
            projectileRenderer.draw(gc, projectile);
        }

        // 3. Enemies
        for (EnemyModel enemy : model.getEnemies()) {
            if (enemy instanceof IscatBomberModel bomber) {
                bomberRenderer.draw(gc, bomber);
            } else {
                fallbackRenderer.draw(gc, enemy);
            }
        }

        // 4. Player
        if (model.getPlayer() != null) {
            playerRenderer.draw(gc, model.getPlayer());
        }

        // 5. HUD
        if (VisualSettings.MOSTRA_FPS) {
            drawFPS(gc, currentFps);
        }
    }

    private void drawFPS(GraphicsContext gc, int fps) {
        gc.save();
        gc.setFill(Color.LIME);
        gc.setFont(Font.font("Miracode", 14));
        gc.fillText("FPS: " + fps, 10, 25);
        gc.restore();
    }

    public SpaceModel getSpace() { return space; }
}
