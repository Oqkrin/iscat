package uni.gaben.iscat.utils.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.universe.starfield.StarfieldModel;
import uni.gaben.iscat.game.universe.starfield.StarfieldView;
import uni.gaben.iscat.game.universe.starfield.StarModel;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.Random;

/**
 * Canvas con sfondo stellato animato che si interfaccia con il modello passivo StarfieldModel.
 */
public class StarryBackgroundCanvas extends Canvas {

    private final StarfieldModel space;
    private final StarfieldView spaceView = new StarfieldView();
    private final AnimationTimer animationLoop;

    // Configurazione stelle
    private static final int STAR_COUNT = 150;

    // Mouse tracking mode
    private boolean followMouse = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private double prevMouseX = 0;
    private double prevMouseY = 0;

    // Sensibilità parallasse del mouse
    private static final double MOUSE_PARALLAX_FACTOR = 0.15;

    public StarryBackgroundCanvas() {
        // Inizializza il tuo modello passivo
        this.space = new StarfieldModel(0, 0);

        // Rigenera e ripopola il modello quando cambiano le dimensioni della finestra
        widthProperty().addListener((obs, old, newWidth) -> {
            if (newWidth.doubleValue() > 0) {
                spaceView.setW(newWidth.doubleValue());
                populateStarfield();
            }
        });
        heightProperty().addListener((obs, old, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                spaceView.setH(newHeight.doubleValue());
                populateStarfield();
            }
        });

        // Loop di calcolo e rendering ad ogni frame
        animationLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
    }

    /**
     * Popola la lista interna dello StarfieldModel passivo usando le dimensioni correnti del Canvas.
     */
    private void populateStarfield() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Puliamo il vecchio stato (essenziale per evitare sovrapposizioni al resize/reset)
        space.clear();

        Random rand = new Random();
        for (int i = 0; i < STAR_COUNT; i++) {
            double starX = rand.nextDouble() * w;
            double starY = rand.nextDouble() * h;
            // Dimensioni assortite tra 1.0 e 3.5 pixel
            double size = 1.0 + rand.nextDouble() * 2.5;

            // Inseriamo la stella nel modello tramite il tuo metodo nativo
            space.addStar(new StarModel(starX, starY, size));
        }
    }

    public void setFollowMouse(boolean followMouse) {
        this.followMouse = followMouse;
        if (followMouse) {
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }

    public void updateMousePosition(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    /**
     * Sposta la telecamera della View in base alla velocità (es. del player).
     * Sostituisce il vecchio space.update() che dava errore di compilazione.
     */
    public void updateWithVelocity(double vx, double vy) {
        followMouse = false;
        spaceView.setCameraX(spaceView.getCameraX() + vx);
        spaceView.setCameraY(spaceView.getCameraY() + vy);
    }

    /**
     * Applica un impulso istantaneo alle coordinate della telecamera di sfondo.
     */
    public void applyImpulse(double dvx, double dvy) {
        spaceView.setCameraX(spaceView.getCameraX() + dvx);
        spaceView.setCameraY(spaceView.getCameraY() + dvy);
    }

    private void update() {
        if (followMouse) {
            double dx = mouseX - prevMouseX;
            double dy = mouseY - prevMouseY;

            // Muove la telecamera interna in base allo spostamento del mouse
            spaceView.setCameraX(spaceView.getCameraX() + (dx * MOUSE_PARALLAX_FACTOR));
            spaceView.setCameraY(spaceView.getCameraY() + (dy * MOUSE_PARALLAX_FACTOR));

            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }

    private void render() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0 || space.getStars().isEmpty()) return;

        GraphicsContext gc = getGraphicsContext2D();

        ThemeColors.ensureLoaded();
        Color bgColor = ThemeColors.parsedColors.getOrDefault("bg-primary", Color.BLACK);

        // Pulisce lo sfondo
        gc.setFill(bgColor);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        // Disegna delegando alla View nativa passando il modello popolato
        spaceView.draw(space, gc);
    }

    public void start() {
        if (animationLoop != null) animationLoop.start();
    }

    public void stop() {
        if (animationLoop != null) animationLoop.stop();
    }

    public StarfieldModel getSpace() {
        return space;
    }

    public StarfieldView getSpaceView() {
        return spaceView;
    }
}