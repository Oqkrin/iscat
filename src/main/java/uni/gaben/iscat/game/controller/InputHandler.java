package uni.gaben.iscat.game.controller;

import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uni.gaben.iscat.utils.ThemeManager;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

/**
 * Cattura lo stato grezzo di tastiera e mouse ogni frame.
 * {@code dodge} è un impulso one-shot: viene consumato dal controller
 * e resettato subito dopo.
 */
public class InputHandler {

    public boolean up, down, left, right;

    // RAINBOW MODE
    public boolean rainbowMode = false;
    private Timeline rainbowTimeline;
    private int themeIdx = 0;
    private final Color[] colors = {Color.RED, Color.LIME, Color.CYAN, Color.ORANGE, Color.MAGENTA, Color.YELLOW};

    /** true per un solo tick quando SPACE viene premuto. */
    public boolean dodge;

    public boolean pausePressed;
    public boolean shooting;

    public double mouseX;
    public double mouseY;

    public void setKeyEventHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W, UP     -> up    = true;
                case A, LEFT   -> left  = true;
                case S, DOWN   -> down  = true;
                case D, RIGHT  -> right = true;
                case SPACE     -> dodge = true;
                case ESCAPE -> pausePressed = true;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = false;
                case A, LEFT  -> left  = false;
                case S, DOWN  -> down  = false;
                case D, RIGHT -> right = false;
                case ESCAPE -> pausePressed = false;
                // SPACE non ha un "rilascio" — è one-shot, consumato dal controller

                // --- GESTIONE TEMI (Tasti 1-0) ---
                case DIGIT1 -> ThemeManager.getInstance().applyTheme(Color.RED, 1.0);
                case DIGIT2 -> ThemeManager.getInstance().applyTheme(Color.LIME, 1.0);
                case DIGIT3 -> ThemeManager.getInstance().applyTheme(Color.CYAN, 1.0);
                case DIGIT4 -> ThemeManager.getInstance().applyTheme(Color.ORANGE, 1.0);
                case DIGIT5 -> ThemeManager.getInstance().applyTheme(Color.MAGENTA, 1.0);
                case DIGIT6 -> ThemeManager.getInstance().applyTheme(Color.YELLOW, 1.0);
                case DIGIT7 -> ThemeManager.getInstance().applyTheme(Color.DEEPSKYBLUE, 1.0);
                case DIGIT8 -> ThemeManager.getInstance().applyTheme(Color.VIOLET, 1.0);
                case DIGIT9 -> ThemeManager.getInstance().applyTheme(Color.rgb(100, 10, 200), 1.0); // Il tuo colore custom
                case DIGIT0 -> ThemeManager.getInstance().applyTheme(Color.WHITE, 1.0); // Reset

                // RAINBOW MODE
                case QUOTE -> toggleRainbow();
            }
        });
    }

    public boolean consumePause() {
        boolean p = pausePressed;
        pausePressed = false;
        return p;
    }


    /**
     * Mouse in coordinate locali al canvas, non alla scena.
     * Deve essere chiamato dopo che il canvas è aggiunto allo scene graph.
     */
    public void setMouseEventHandlers(Node canvas) {
        canvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        // shooting
        canvas.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) shooting = true;
        });
        canvas.setOnMouseReleased(e -> {
            if (!e.isPrimaryButtonDown()) shooting = false;
        });
    }

    /** Consuma il flag dodge e lo resetta. */
    public boolean consumeDodge() {
        boolean d = dodge;
        dodge = false;
        return d;
    }

    private void toggleRainbow() {
        if (rainbowTimeline != null) {
            rainbowTimeline.stop();
            rainbowTimeline = null;
            ThemeManager.getInstance().applyTheme(Color.WHITE, 1.0); // Reset
            return;
        }

        rainbowTimeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            ThemeManager.getInstance().applyTheme(colors[themeIdx % colors.length], 1.0);
            themeIdx++;
        }));
        rainbowTimeline.setCycleCount(Timeline.INDEFINITE);
        rainbowTimeline.play();
    }
}
