package uni.gaben.iscat.game.controller;

import javafx.scene.Scene;

// InputHandler.java – incapsula i due handler originali
public class InputHandler {
    public boolean up, down, left, right;
    public double mouseX, mouseY;

    public void setKeyEventHandlers(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = true;
                case A, LEFT  -> left  = true;
                case S, DOWN  -> down  = true;
                case D, RIGHT -> right = true;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W, UP    -> up    = false;
                case A, LEFT  -> left  = false;
                case S, DOWN  -> down  = false;
                case D, RIGHT -> right = false;
            }
        });
        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        // mouse pressed/released se servono
    }
}
