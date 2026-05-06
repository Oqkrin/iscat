package uni.gaben.iscat.game.controller;

import javafx.scene.Node;
import javafx.scene.Scene;

/**
 * Cattura lo stato grezzo di tastiera e mouse ogni frame.
 * {@code dodge} è un impulso one-shot: viene consumato dal controller
 * e resettato subito dopo.
 */
public class InputHandler {

    public boolean up, down, left, right;

    /** true per un solo tick quando SPACE viene premuto. */
    public boolean dodge;

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
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = false;
                case A, LEFT  -> left  = false;
                case S, DOWN  -> down  = false;
                case D, RIGHT -> right = false;
                // SPACE non ha un "rilascio" — è one-shot, consumato dal controller
            }
        });
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
    }

    /** Consuma il flag dodge e lo resetta. */
    public boolean consumeDodge() {
        boolean d = dodge;
        dodge = false;
        return d;
    }
}
