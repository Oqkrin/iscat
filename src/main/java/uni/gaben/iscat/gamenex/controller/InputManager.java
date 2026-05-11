package uni.gaben.iscat.gamenex.controller;

import javafx.scene.Node;
import javafx.scene.Scene;

public class InputManager {
    public boolean up, down, left, right;
    public boolean dash;
    public boolean shooting;
    public boolean suction; // Q - attracts nearby bodies
    public double mouseX, mouseY;
    public double cameraX, cameraY;

    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W, UP    -> up = true;
                case A, LEFT  -> left = true;
                case S, DOWN  -> down = true;
                case D, RIGHT -> right = true;
                case SPACE    -> dash = true;
                case Q        -> suction = true;
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case W, UP    -> up = false;
                case A, LEFT  -> left = false;
                case S, DOWN  -> down = false;
                case D, RIGHT -> right = false;
                case Q        -> suction = false;
            }
        });
    }

    public void attachToCanvas(Node canvas) {
        canvas.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        canvas.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });
        canvas.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown()) shooting = true;
        });
        canvas.setOnMouseReleased(e -> {
            if (!e.isPrimaryButtonDown()) shooting = false;
        });
    }

    public boolean consumeDash() {
        boolean d = dash;
        dash = false;
        return d;
    }
}
