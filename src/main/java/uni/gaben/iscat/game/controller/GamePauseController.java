package uni.gaben.iscat.game.controller;

import javafx.scene.Node;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

import java.util.function.Consumer;

/**
 * Gestisce la pausa e la navigazione fuori dal gioco.
 */
public class GamePauseController {

    private boolean paused = false;
    private Consumer<Boolean> onPauseToggle;
    private Node focusTarget; // node to refocus when unpausing (canvas)

    public void setOnPauseToggle(Consumer<Boolean> callback) {
        this.onPauseToggle = callback;
    }

    public void setFocusTarget(Node node) {
        this.focusTarget = node;
    }

    /** Checks input and toggles pause if ESC was pressed. */
    public void processInput(InputHandler input) {
        if (input.consumePause()) toggle();
    }

    public void toggle() {
        paused = !paused;
        if (onPauseToggle != null) onPauseToggle.accept(paused);
        if (!paused && focusTarget != null) focusTarget.requestFocus();
    }

    public void exitToMainMenu() {
        if (paused) toggle(); // unpause so state is clean
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }

    public boolean isPaused() { return paused; }
}
