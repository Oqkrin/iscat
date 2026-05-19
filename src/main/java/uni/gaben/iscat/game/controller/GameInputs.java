package uni.gaben.iscat.game.controller;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;

public class GameInputs {
    public boolean up, down, left, right;

    // We change these to private/managed flags so they can't be accidentally altered mid-tick
    private boolean dashRequested = false;
    private boolean dashMouseRequested = false;

    public boolean shooting;
    public boolean suction;
    public double mouseX, mouseY;

    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W, UP    -> up = true;
                case A, LEFT  -> left = true;
                case S, DOWN  -> down = true;
                case D, RIGHT -> right = true;
                case SPACE    -> dashRequested = true; // Salva la richiesta discreta
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
            if (e.getButton() == MouseButton.PRIMARY) shooting = true;
            if (e.getButton() == MouseButton.MIDDLE)  dashMouseRequested = true;
        });
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) shooting = false;
            if (e.getButton() == MouseButton.MIDDLE)  dashMouseRequested = false;
        });
    }

    // Metodi di consumo atomici e puliti per evitare lag di input di fine frame
    public boolean consumeDash() {
        boolean d = dashRequested;
        dashRequested = false;
        return d;
    }

    public boolean consumeDashMouse() {
        boolean d = dashMouseRequested;
        dashMouseRequested = false;
        return d;
    }
}