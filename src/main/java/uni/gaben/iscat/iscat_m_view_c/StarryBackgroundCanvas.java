package uni.gaben.iscat.iscat_m_view_c;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.iscat_game.universe.starfield.StarfieldModel;
import uni.gaben.iscat.iscat_game.universe.starfield.StarfieldView;
import uni.gaben.iscat.iscat_game.universe.starfield.StarModel;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.Random;

/**
 * Canvas con sfondo stellato ad altissima fedeltà visiva.
 * Implementa smorzamento dinamico del parallasse e distribuzione stellare logaritmica.
 */
public class StarryBackgroundCanvas extends Canvas {

    private final StarfieldModel space;
    private final StarfieldView spaceView = new SkinnyStarfieldView(); // Mantiene compatibilità strutturale
    private final AnimationTimer animationLoop;

    // Configurazione stelle
    private static final int STAR_COUNT = 1000;

    // Coordinate target (dove la telecamera vuole andare) e correnti (dove si trova fluidamente)
    private double targetCameraX = 0;
    private double targetCameraY = 0;
    private double currentCameraX = 0;
    private double currentCameraY = 0;

    // Gestione del tempo per fluidità indipendente dal framerate
    private long lastTime = 0;

    // Mouse tracking
    private boolean followMouse = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private double prevMouseX = 0;
    private double prevMouseY = 0;

    // Sensibilità del parallasse del mouse
    private static final double MOUSE_PARALLAX_FACTOR = 0.65;

    // Coefficiente di reattività dello smorzamento (più è alto, più segue i movimenti rapidamente)
    private static final double DAMPING_SPEED = 8.0;

    public StarryBackgroundCanvas() {
        this.space = new StarfieldModel(0, 0);

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

    /**
     * Popola lo Starfield usando una distribuzione a campana asimmetrica.
     * Genera molte più stelle microscopiche di sfondo che giganti in primo piano.
     */
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
     * Incrementa il target della telecamera in base alla velocità vettoriale del giocatore.
     */
    public void updateWithVelocity(double vx, double vy) {
        followMouse = false;
        targetCameraX += vx;
        targetCameraY += vy;
    }

    /**
     * Registra un impulso cinematico immediato modificando il target di destinazione.
     */
    public void applyImpulse(double dvx, double dvy) {
        targetCameraX += dvx;
        targetCameraY += dvy;
    }

    /**
     * Calcola lo scostamento e applica l'interpolazione lineare (Lerp) tarata sul delta time.
     */
    private void update(double dt) {
        if (followMouse) {
            double dx = mouseX - prevMouseX;
            double dy = mouseY - prevMouseY;

            targetCameraX += dx * MOUSE_PARALLAX_FACTOR;
            targetCameraY += dy * MOUSE_PARALLAX_FACTOR;

            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }

        // AGGIUNTA ESTETICA: Un micro-drift costante automatico (0.8 px/sec) per non far mai apparire lo spazio statico
        targetCameraX += 0.8 * dt;

        // Formula di smorzamento asintotico stabile e indipendente dal framerate: 1 - e^(-k * dt)
        double lerpFactor = 1.0 - Math.exp(-DAMPING_SPEED * dt);

        currentCameraX += (targetCameraX - currentCameraX) * lerpFactor;
        currentCameraY += (targetCameraY - currentCameraY) * lerpFactor;

        // Aggiorna le proprietà reali lette dallo StarfieldView durante il disegno
        spaceView.setCameraX(currentCameraX);
        spaceView.setCameraY(currentCameraY);
    }

    private void render() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0 || space.getStars().isEmpty()) return;

        GraphicsContext gc = getGraphicsContext2D();

        ThemeColors.ensureLoaded();
        Color bgColor = ThemeColors.parsedColors.getOrDefault("bg-primary", Color.BLACK);

        gc.setFill(bgColor);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        spaceView.draw(space, gc);
    }

    public void start() {
        lastTime = 0; // Resetta il cronometro interno per evitare balzi temporali all'avvio
        if (animationLoop != null) animationLoop.start();
    }

    public void stop() {
        if (animationLoop != null) animationLoop.stop();
    }

    public StarfieldModel getSpace() { return space; }
    public StarfieldView getSpaceView() { return spaceView; }

    // Sotto-classe interna d'appoggio per raggirare in sicurezza l'estensione originaria se necessario
    private static class SkinnyStarfieldView extends StarfieldView {}
}