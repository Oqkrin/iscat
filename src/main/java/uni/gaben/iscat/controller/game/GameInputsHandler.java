package uni.gaben.iscat.controller.game;

import javafx.geometry.Point2D;
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
 * Quick‑dash (double‑tap) is detected and exposed as a consumable direction.
 */
public class GameInputsHandler {

    // --- Movement and shooting (read each frame) ---
    public boolean up, down, left, right;
    public boolean shooting;

    // --- Dash triggers (read and then consumed) ---
    public boolean dashKeyPressed;
    public boolean dashMousePressed;   // triggers keyboard dash? Actually we use for slow-motion

    // --- Slow‑motion (mouse dodge button held) ---
    public boolean slowMotionRequested;

    // --- Mouse position ---
    public double mouseX, mouseY;

    // --- Quick‑dash (double‑tap) state ---
    private int quickDashX = 0;              // -1, 0, 1
    private int quickDashY = 0;              // -1, 0, 1
    private boolean quickDashConsumed = false;

    // --- Double‑tap timing ---
    private long lastUpPress = 0;
    private long lastDownPress = 0;
    private long lastLeftPress = 0;
    private long lastRightPress = 0;
    private static final long DOUBLE_TAP_THRESHOLD_MS = 200; // 0.2 seconds

    // --- Pause (consumed) ---
    private boolean pauseRequested = false;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

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

    /**
     * Resets all input states. Called when the game loses focus or at the start of a frame
     * if you want to avoid stuck keys (optional).
     */
    public void resetInputs() {
        up = down = left = right = false;
        shooting = false;
        dashKeyPressed = false;
        dashMousePressed = false;
        slowMotionRequested = false;
        pauseRequested = false;
        // Quick dash state is not reset here; it's cleared after consumption.
        // Timestamps are left as-is to maintain detection between frames.
    }

    /**
     * Consumes the quick‑dash direction if one was detected this frame.
     * Returns a normalized direction vector, or null if none.
     * After calling this, the quick‑dash flag is cleared.
     */
    public Point2D consumeQuickDash() {
        if (quickDashConsumed || (quickDashX == 0 && quickDashY == 0)) {
            return null;
        }
        quickDashConsumed = true; // mark as consumed
        double len = Math.sqrt(quickDashX * quickDashX + quickDashY * quickDashY);
        double normX = quickDashX / len;
        double normY = quickDashY / len;
        // Clear the flags
        quickDashX = 0;
        quickDashY = 0;
        return new Point2D(normX, normY);
    }

    /**
     * Consumes the pause request (one‑shot).
     */
    public boolean consumePause() {
        boolean p = pauseRequested;
        pauseRequested = false;
        return p;
    }

    // -------------------------------------------------------------------------
    // Internal input handling
    // -------------------------------------------------------------------------

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
                if (matchKey(code, settings.getDash2())) dashMousePressed = true;
                if (matchKey(code, settings.getPauseGame())) pauseRequested = true;

                // ----- Quick‑dash detection -----
                detectQuickDash(code);
            } else {
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

            // Quick‑dash detection also for default bindings
            if (isPressed) {
                detectQuickDash(code);
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
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = true;
                if (matchMouse(button, settings.getDash2())) {
                    slowMotionRequested = true;
                }
                if (matchMouse(button, settings.getPauseGame())) pauseRequested = true;
            } else {
                if (matchMouse(button, settings.getDash1())) dashKeyPressed = false;
                if (matchMouse(button, settings.getDash2())) slowMotionRequested = false;
            }
        } else {
            if (button == MouseButton.PRIMARY) shooting = isPressed;
            if (button == MouseButton.MIDDLE) {
                slowMotionRequested = isPressed;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Quick‑dash detection helper
    // -------------------------------------------------------------------------

    private void detectQuickDash(KeyCode code) {
        long now = System.currentTimeMillis();

        if (code == KeyCode.UP || code == KeyCode.W) {
            if (now - lastUpPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashY = -1;
                quickDashConsumed = false;
            }
            lastUpPress = now;
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            if (now - lastDownPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashY = 1;
                quickDashConsumed = false;
            }
            lastDownPress = now;
        } else if (code == KeyCode.LEFT || code == KeyCode.A) {
            if (now - lastLeftPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashX = -1;
                quickDashConsumed = false;
            }
            lastLeftPress = now;
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            if (now - lastRightPress < DOUBLE_TAP_THRESHOLD_MS) {
                quickDashX = 1;
                quickDashConsumed = false;
            }
            lastRightPress = now;
        }
        // Note: if two directions are double‑tapped quickly, both axes will be set,
        // and consumeQuickDash() will return a normalized diagonal vector.
    }

    // -------------------------------------------------------------------------
    // Matching helpers
    // -------------------------------------------------------------------------

    private boolean matchKey(KeyCode code, String settingValue) {
        if (settingValue == null) return false;
        String dbKey = settingValue.trim().toUpperCase();
        String fxKey = code.toString().toUpperCase();

        return switch (dbKey) {
            case "UP" -> code == KeyCode.UP;
            case "DOWN" -> code == KeyCode.DOWN;
            case "LEFT" -> code == KeyCode.LEFT;
            case "RIGHT" -> code == KeyCode.RIGHT;
            case "SPACE" -> code == KeyCode.SPACE;
            default -> fxKey.equals(dbKey);
        };
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
}