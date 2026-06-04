package uni.gaben.iscat.controller.game;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.model.game.GameModel;

import java.util.function.DoubleConsumer;

/**
 * Handles the main game loop, tracking delta time and orchestrating
 * update and draw calls. It correctly computes active game time (ignoring pause).
 */
public class GameLoopTimer extends AnimationTimer {

    private final GameModel gameModel;
    private final DoubleConsumer updateCall;
    private Runnable drawCall;

    private double totalElapsedSeconds = 0.0;

    public GameLoopTimer(GameModel gameModel, DoubleConsumer updateCall) {
        this.gameModel = gameModel;
        this.updateCall = updateCall;
    }

    public void setDrawCall(Runnable drawCall) {
        this.drawCall = drawCall;
    }

    /**
     * Resets internal accumulators so a new game session starts at zero.
     */
    public void resetTimer() {
        totalElapsedSeconds = 0.0;
        gameModel.setTotalElapsedSeconds(0.0);
        gameModel.timerProperty().set(0);
        gameModel.setLastUpdate(0);
        gameModel.startProperty().set(-1);
    }

    @Override
    public void handle(long now) {
        if (gameModel.getStart() == -1) gameModel.startProperty().set(now);
        if (gameModel.getLastUpdate() == 0) {
            gameModel.setLastUpdate(now);
            return;
        }

        // Update current time (updates dt binding in GameModel)
        gameModel.setNow(now);

        double dt = gameModel.getDt();

        // Increment timer only if the game is actually running
        if (!gameModel.getGameState().isPaused()) {
            totalElapsedSeconds += dt;
            gameModel.setTotalElapsedSeconds(totalElapsedSeconds);

            int h = (int) (totalElapsedSeconds / 3600);
            int m = (int) ((totalElapsedSeconds % 3600) / 60);
            int s = (int) (totalElapsedSeconds % 60);
            gameModel.timerProperty().set(h * 10000 + m * 100 + s);
        }

        // Clamp dt to avoid physics spiral of death
        double clampedDt = Math.min(dt, GameModel.ACCUMULATORUNIT);

        // Always invoke update so input (like toggling pause) is still processed
        if (updateCall != null) {
            updateCall.accept(clampedDt);
        }

        // Always draw to keep the screen (and FPS counter) alive
        if (drawCall != null) {
            drawCall.run();
        }

        gameModel.setLastUpdate(now);
    }
}
