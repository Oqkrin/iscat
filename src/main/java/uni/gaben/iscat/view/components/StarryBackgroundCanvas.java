package uni.gaben.iscat.view.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.Star;
import uni.gaben.iscat.universe.effects.Starfield;
import uni.gaben.iscat.universe.rendering.StarfieldRenderer;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Random;

/**
 * Canvas con sfondo stellato ad altissima fedeltà visiva.
 * Implementa smorzamento dinamico del parallasse e distribuzione stellare logaritmica.
 */
public class StarryBackgroundCanvas extends Canvas {

    private final Starfield space;
    private final StarfieldRenderer spaceView = new SkinnyStarfieldVFX(); // Mantiene compatibilità strutturale
    private final AnimationTimer animationLoop;

    private static final int STAR_COUNT = 1000;

    private double targetCameraX = 0;
    private double targetCameraY = 0;
    private double currentCameraX = 0;
    private double currentCameraY = 0;

    private long lastTime = 0;

    private boolean followMouse = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private double prevMouseX = 0;
    private double prevMouseY = 0;

    private static final double MOUSE_PARALLAX_FACTOR = 0.65;

    private static final double DAMPING_SPEED = 8.0;

    /** Inizializza il canvas dello sfondo stellato e configura i listener di ridimensionamento e l'AnimationTimer. */
    public StarryBackgroundCanvas() {
        this.space = new Starfield(0, 0);

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

        animationLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // Conversione nanosecondi -> secondi secondi per il calcolo fisico del frame-rate
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Protezione da sbalzi di lag massicci (es. spostamento della finestra)
                if (dt > 0.1) dt = 0.1;

                update(dt);
                render();
            }
        };
    }

    /** Popola lo Starfield generando casualmente le stelle sul canvas applicando un filtro di parallasse sulla dimensione. */
    private void populateStarfield() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        space.clear();
        Random rand = new Random();

        for (int i = 0; i < STAR_COUNT; i++) {
            double starX = rand.nextDouble() * w;
            double starY = rand.nextDouble() * h;

            // FISICA DEL PARALLASSE: Applichiamo un fattore esponenziale alla dimensione.
            // Math.pow(rand, 2.5) distribuisce la stragrande maggioranza dei valori vicino allo 0.
            double sizeBias = Math.pow(rand.nextDouble(), 2.5);

            // Dimensione finale: range strutturato tra 0.7 e 3.5 pixel
            double size = 0.7 + (sizeBias * 2.8);

            space.addStar(new Star(starX, starY, size));
        }
    }

    /** Attiva o disattiva l'inseguimento del movimento del mouse per l'effetto parallasse. */
    public void setFollowMouse(boolean followMouse) {
        this.followMouse = followMouse;
        if (followMouse) {
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }

    /** Aggiorna le coordinate correnti del cursore del mouse all'interno del canvas. */
    public void updateMousePosition(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    /** Incrementa il target della telecamera in base alla velocità vettoriale fornita. */
    public void updateWithVelocity(double vx, double vy) {
        followMouse = false;
        targetCameraX += vx;
        targetCameraY += vy;
    }

    /** Applica un impulso cinematico immediato modificando le coordinate di destinazione della telecamera. */
    public void applyImpulse(double dvx, double dvy) {
        targetCameraX += dvx;
        targetCameraY += dvy;
    }

    /** Aggiorna lo stato logico della telecamera, calcolando gli scostamenti del mouse e applicando lo smorzamento. */
    private void update(double dt) {
        if (followMouse) {
            double dx = mouseX - prevMouseX;
            double dy = mouseY - prevMouseY;

            targetCameraX += dx * MOUSE_PARALLAX_FACTOR;
            targetCameraY += dy * MOUSE_PARALLAX_FACTOR;

            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }

        targetCameraX += 0.8 * dt;

        // Formula di smorzamento asintotico stabile e indipendente dal framerate: 1 - e^(-k * dt)
        double lerpFactor = 1.0 - Math.exp(-DAMPING_SPEED * dt);

        currentCameraX += (targetCameraX - currentCameraX) * lerpFactor;
        currentCameraY += (targetCameraY - currentCameraY) * lerpFactor;

        // Aggiorna le proprietà reali lette dallo StarfieldRenderer durante il disegno
        spaceView.setCameraX(currentCameraX);
        spaceView.setCameraY(currentCameraY);
    }

    /** Disegna il colore di sfondo e delega allo StarfieldRenderer il rendering delle stelle sul contesto grafico. */
    private void render() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0 || space.getStars().isEmpty()) return;

        GraphicsContext gc = getGraphicsContext2D();

        Color bgColor = ThemeManager.getInstance().getBgPrimary();

        gc.setFill(bgColor);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        spaceView.render(space, gc);
    }

    /** Avvia il ciclo di animazione dello sfondo stellato resettando il cronometro interno. */
    public void start() {
        lastTime = 0; // Resetta il cronometro interno per evitare balzi temporali all'avvio
        if (animationLoop != null) animationLoop.start();
    }

    /** Arresta l'AnimationTimer interrompendo l'aggiornamento e il disegno dello sfondo. */
    public void stop() {
        if (animationLoop != null) animationLoop.stop();
    }

    public Starfield getSpace() { return space; }
    public StarfieldRenderer getSpaceView() { return spaceView; }

    /** Sotto-classe interna d'appoggio per estendere strutturalmente StarfieldRenderer. */
    private static class SkinnyStarfieldVFX extends StarfieldRenderer {}
}