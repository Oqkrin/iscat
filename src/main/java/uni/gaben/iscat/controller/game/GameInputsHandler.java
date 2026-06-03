package uni.gaben.iscat.controller.game;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

public class GameInputsHandler{
    public boolean up, down, left, right;
    public boolean shooting;

    private boolean dashRequested = false;
    private boolean dashMouseRequested = false;

    public double mouseX, mouseY;

    private boolean pauseRequested = false;

    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(e -> handleKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> handleKey(e.getCode(), false));

        scene.setOnMousePressed(e -> handleMouse(e.getButton(), true));
        scene.setOnMouseReleased(e -> handleMouse(e.getButton(), false));
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
    }

    public void resetInputs() {
        up = false;
        down = false;
        left = false;
        right = false;
        dashRequested = false;
        dashMouseRequested = false;
        shooting = false;
        pauseRequested = false;
    }

    private void handleKey(KeyCode code, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (matchKey(code, settings.getWalkUp()))    up = isPressed;
            if (matchKey(code, settings.getWalkDown()))  down = isPressed;
            if (matchKey(code, settings.getWalkLeft()))  left = isPressed;
            if (matchKey(code, settings.getWalkRight())) right = isPressed;
            if (matchKey(code, settings.getAttack()))    shooting = isPressed;

            if (isPressed) {
                if (matchKey(code, settings.getDash1()))     dashRequested = true;
                if (matchKey(code, settings.getDash2()))     dashMouseRequested = true;
                if (matchKey(code, settings.getPauseGame())) pauseRequested = true;
            } else {
                if (matchKey(code, settings.getDash1()))    dashRequested = false;
                if (matchKey(code, settings.getDash2()))    dashMouseRequested = false;
            }
        } else {
            switch (code) {
                case W, UP    -> up = isPressed;
                case S, DOWN  -> down = isPressed;
                case A, LEFT  -> left = isPressed;
                case D, RIGHT -> right = isPressed;
                case Z        -> shooting = isPressed;
                case SPACE    -> { if (isPressed) dashRequested = true; }
                case ESCAPE, P -> { if (isPressed) pauseRequested = true; }
                default -> {}
            }
        }
    }


    private void handleMouse(MouseButton button, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (matchMouse(button, settings.getWalkUp()))    up = isPressed;
            if (matchMouse(button, settings.getWalkDown()))  down = isPressed;
            if (matchMouse(button, settings.getWalkLeft()))  left = isPressed;
            if (matchMouse(button, settings.getWalkRight())) right = isPressed;
            if (matchMouse(button, settings.getAttack()))    shooting = isPressed;

            if (isPressed) {
                if (matchMouse(button, settings.getDash1())) dashRequested = true;
                if (matchMouse(button, settings.getDash2())) dashMouseRequested = true;
                if (matchMouse(button, settings.getPauseGame())) pauseRequested = true;
            } else {
                if (matchMouse(button, settings.getDash1())) dashRequested = false;
                if (matchMouse(button, settings.getDash2())) dashMouseRequested = false;
            }
        } else {
            if (button == MouseButton.PRIMARY) shooting = isPressed;
            if (button == MouseButton.MIDDLE) {
                if (isPressed) dashMouseRequested = true;
                else dashMouseRequested = false;
            }
        }
    }

    private boolean matchKey(KeyCode code, String settingValue) {
        if (settingValue == null) return false;
        String dbKey = settingValue.trim().toUpperCase();
        String fxKey = code.toString().toUpperCase();

        if (dbKey.equals("UP")) return code == KeyCode.UP;
        if (dbKey.equals("DOWN")) return code == KeyCode.DOWN;
        if (dbKey.equals("LEFT")) return code == KeyCode.LEFT;
        if (dbKey.equals("RIGHT")) return code == KeyCode.RIGHT;
        if (dbKey.equals("SPACE")) return code == KeyCode.SPACE;

        return fxKey.equals(dbKey);
    }

    private boolean matchMouse(MouseButton button, String settingValue) {
        if (settingValue == null) return false;
        String val = settingValue.trim().toUpperCase();

        return switch (button) {
            case PRIMARY   -> val.equals("MOUSEPRIMARY");
            case SECONDARY -> val.equals("MOUSESECONDARY");
            case MIDDLE    -> val.equals("MOUSEMIDDLE");
            default -> false;
        };
    }

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

    public boolean consumePause() {
        boolean p = pauseRequested;
        pauseRequested = false;
        return p;
    }
}