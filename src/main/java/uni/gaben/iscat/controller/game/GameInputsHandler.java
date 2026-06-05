package uni.gaben.iscat.controller.game;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

/**
 * Handles raw keyboard/mouse input for the game.
 * State is read directly every frame – no buffered consumption for movement or dash.
 * Pause uses a one‑shot consumption pattern to avoid multiple toggles.
 */
public class GameInputsHandler {

    // Movement and shooting (read each frame)
    public boolean up, down, left, right;
    public boolean shooting;

    // Dash triggers (read and then consumed)
    public boolean dashKeyPressed;
    public boolean dashMousePressed;   // triggers keyboard dash? Actually we use for slow-motion

    // Slow‑motion (mouse dodge button held)
    public boolean slowMotionRequested;

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
        up = down = left = right = false;
        shooting = false;
        dashKeyPressed = false;
        dashMousePressed = false;
        slowMotionRequested = false;
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
                if (matchKey(code, settings.getDash1())) dashKeyPressed = true;
                if (matchKey(code, settings.getDash2())) dashMousePressed = true; // used for slow?
                if (matchKey(code, settings.getPauseGame())) pauseRequested = true;
            } else {
                // No consumption, just clear state on release
                if (matchKey(code, settings.getDash1())) dashKeyPressed = false;
                if (matchKey(code, settings.getDash2())) dashMousePressed = false;
            }
        } else {
            // Default fallback
            switch (code) {
                case W, UP    -> up = isPressed;
                case S, DOWN  -> down = isPressed;
                case A, LEFT  -> left = isPressed;
                case D, RIGHT -> right = isPressed;
                case Z        -> shooting = isPressed;
                case SPACE    -> { if (isPressed) dashKeyPressed = true; }
                case ESCAPE, P -> { if (isPressed) pauseRequested = true; }
                default -> {}
            }
        }
    }

    private void handleMouse(MouseButton button, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            // Movement via mouse? (optional, keep for completeness)
            if (matchMouse(button, settings.getWalkUp()))    up = isPressed;
            if (matchMouse(button, settings.getWalkDown()))  down = isPressed;
            if (matchMouse(button, settings.getWalkLeft()))  left = isPressed;
            if (matchMouse(button, settings.getWalkRight())) right = isPressed;
            if (matchMouse(button, settings.getAttack()))    shooting = isPressed;

            if (isPressed) {
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = true;
                if (matchMouse(button, settings.getDash2())) {
                    // Mouse dodge → request slow‑motion
                    slowMotionRequested = true;
                }
                if (matchMouse(button, settings.getPauseGame())) pauseRequested = true;
            } else {
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = false;
                if (matchMouse(button, settings.getDash2())) slowMotionRequested = false;
            }
        } else {
            // Default mouse fallback
            if (button == MouseButton.PRIMARY) shooting = isPressed;
            if (button == MouseButton.MIDDLE) {
                slowMotionRequested = isPressed;
            }
        }
    }

    // ----- Match helpers -----
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
            case PRIMARY -> val.equals("MOUSEPRIMARY");
            case SECONDARY -> val.equals("MOUSESECONDARY");
            case MIDDLE -> val.equals("MOUSEMIDDLE");
            default -> false;
        };
    }

    // Pause is still consumed (one‑shot)
    public boolean consumePause() {
        boolean p = pauseRequested;
        pauseRequested = false;
        return p;
    }
}