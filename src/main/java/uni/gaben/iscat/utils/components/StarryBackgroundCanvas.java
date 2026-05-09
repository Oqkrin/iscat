package uni.gaben.iscat.utils.components;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.components.space.SpaceModel;
import uni.gaben.iscat.game.components.space.SpaceView;

/**
 * Canvas con sfondo stellato animato.
 * 
 * Può seguire:
 * - Velocità del giocatore (modalità game)
 * - Posizione del mouse (modalità menu/login)
 */
public class StarryBackgroundCanvas extends Canvas {

    private final SpaceModel space;
    private final SpaceView spaceView = new SpaceView();
    private AnimationTimer animationLoop;
    
    // Mouse tracking mode
    private boolean followMouse = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private double targetVx = 0;
    private double targetVy = 0;
    private double prevMouseX = 0;
    private double prevMouseY = 0;
    
    // Mouse sensitivity for parallax effect
    private static final double MOUSE_PARALLAX_FACTOR = 0.15;

    public StarryBackgroundCanvas() {
        this.space = new SpaceModel(0, 0);
        
        // Update space size when canvas resizes
        widthProperty().addListener((obs, old, newWidth) -> {
            if (newWidth.doubleValue() > 0) {
                space.setWidth(newWidth.intValue());
            }
        });
        heightProperty().addListener((obs, old, newHeight) -> {
            if (newHeight.doubleValue() > 0) {
                space.setHeight(newHeight.intValue());
            }
        });
        
        // Render loop
        animationLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                render();
            }
        };
    }
    
    /**
     * Abilita modalità "segui mouse" per scene senza player.
     */
    public void setFollowMouse(boolean followMouse) {
        this.followMouse = followMouse;
        if (followMouse) {
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }
    
    /**
     * Aggiorna la posizione del mouse (chiamato dalla scena).
     */
    public void updateMousePosition(double x, double y) {
        this.mouseX = x;
        this.mouseY = y;
    }
    
    /**
     * Aggiorna le stelle con velocità esterna (es. player velocity).
     * Disabilita automaticamente followMouse.
     */
    public void updateWithVelocity(double vx, double vy) {
        followMouse = false;
        space.update(vx, vy);
    }
    
    /**
     * Applica impulso diretto alle stelle (es. dash).
     */
    public void applyImpulse(double dvx, double dvy) {
        space.applyImpulse(dvx, dvy);
    }
    
    private void update() {
        if (followMouse) {
            // Calculate mouse velocity for parallax effect
            double dx = mouseX - prevMouseX;
            double dy = mouseY - prevMouseY;
            
            // Parallax effect: stars move opposite to mouse (like looking through a window)
            targetVx = dx * MOUSE_PARALLAX_FACTOR;
            targetVy = dy * MOUSE_PARALLAX_FACTOR;
            
            space.update(targetVx, targetVy);
            
            prevMouseX = mouseX;
            prevMouseY = mouseY;
        }
    }
    
    private void render() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;
        
        GraphicsContext gc = getGraphicsContext2D();
        
        // Clear with black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);
        
        // Draw stars
        spaceView.draw(gc, space);
    }
    
    /**
     * Avvia l'animazione delle stelle.
     */
    public void start() {
        if (animationLoop != null) {
            animationLoop.start();
        }
    }
    
    /**
     * Ferma l'animazione delle stelle.
     */
    public void stop() {
        if (animationLoop != null) {
            animationLoop.stop();
        }
    }
    
    /**
     * Accesso diretto al modello stelle (per uso avanzato).
     */
    public SpaceModel getSpace() {
        return space;
    }
}
