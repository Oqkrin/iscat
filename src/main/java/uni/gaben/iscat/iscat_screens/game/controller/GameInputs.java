package uni.gaben.iscat.iscat_screens.game.controller;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import uni.gaben.iscat.iscat_screens.login.model.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

public class GameInputs {
    public boolean up, down, left, right;

    private boolean dashRequested = false;
    private boolean dashMouseRequested = false;

    public boolean shooting;
    public double mouseX, mouseY;

    public void attachToScene(Scene scene) {
        scene.setOnKeyPressed(e -> handleKey(e.getCode(), true));
        scene.setOnKeyReleased(e -> handleKey(e.getCode(), false));
    }

    public void resetInputs() {
        up = false;
        down = false;
        left = false;
        right = false;
        dashRequested = false;
        dashMouseRequested = false;
        shooting = false;
    }

    private void handleKey(KeyCode code, boolean isPressed) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (matchKey(code, settings.getWalkUp()))    up = isPressed;
            if (matchKey(code, settings.getWalkDown()))  down = isPressed;
            if (matchKey(code, settings.getWalkLeft()))  left = isPressed;
            if (matchKey(code, settings.getWalkRight())) right = isPressed;

            if (isPressed) {
                if (matchKey(code, settings.getDash1())) dashRequested = true;
                if (matchKey(code, settings.getDash2())) dashRequested = true;
            }
        } else {
            // FALLBACK: Se i dati di sessione sono nulli, usa i comandi standard di fabbrica
            switch (code) {
                case W, UP    -> up = isPressed;
                case S, DOWN  -> down = isPressed;
                case A, LEFT  -> left = isPressed;
                case D, RIGHT -> right = isPressed;
                case SPACE    -> { if (isPressed) dashRequested = true; }
                default -> {}
            }
        }
    }

    /**
     * Helper per mappare la stringa del DB con il KeyCode di JavaFX.
     * Gestisce anche le frecce direzionali se salvate come "UP", "DOWN", ecc.
     */
    private boolean matchKey(KeyCode code, String settingValue) {
        if (settingValue == null) return false;
        String dbKey = settingValue.trim().toUpperCase();
        String fxKey = code.toString().toUpperCase();

        // Mappatura di sicurezza per le frecce direzionali o formati custom
        if (dbKey.equals("FRECCIA SU") || dbKey.equals("ARROW UP")) return code == KeyCode.UP;
        if (dbKey.equals("FRECCIA GIÙ") || dbKey.equals("FRECCIA GIU") || dbKey.equals("ARROW DOWN")) return code == KeyCode.DOWN;
        if (dbKey.equals("FRECCIA SINISTRA") || dbKey.equals("ARROW LEFT")) return code == KeyCode.LEFT;
        if (dbKey.equals("FRECCIA DESTRA") || dbKey.equals("ARROW RIGHT")) return code == KeyCode.RIGHT;
        if (dbKey.equals("SPAZIO") || dbKey.equals("SPACEBAR")) return code == KeyCode.SPACE;

        return fxKey.equals(dbKey);
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

            UserSettings settings = SessionManager.getInstance().getCurrentSettings();
            if (settings != null) {
                if (isMouseMatch(e.getButton(), settings.getDash1())) dashRequested = true;
                if (isMouseMatch(e.getButton(), settings.getDash2())) dashMouseRequested = true;
            } else {
                if (e.getButton() == MouseButton.MIDDLE) dashMouseRequested = true;
            }
        });
        canvas.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) shooting = false;

            UserSettings settings = SessionManager.getInstance().getCurrentSettings();
            if (settings != null) {
                if (isMouseMatch(e.getButton(), settings.getDash1())) dashRequested = false;
                if (isMouseMatch(e.getButton(), settings.getDash2())) dashMouseRequested = false;
            } else {
                if (e.getButton() == MouseButton.MIDDLE) dashMouseRequested = false;
            }
        });
    }

    private boolean isMouseMatch(MouseButton button, String settingValue) {
        if (settingValue == null) return false;
        String val = settingValue.trim();
        return switch (button) {
            case MIDDLE -> val.equalsIgnoreCase("Middle Mouse") || val.equalsIgnoreCase("MIDDLE");
            case PRIMARY -> val.equalsIgnoreCase("Primary Mouse") || val.equalsIgnoreCase("PRIMARY");
            case SECONDARY -> val.equalsIgnoreCase("Secondary Mouse") || val.equalsIgnoreCase("SECONDARY");
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
}