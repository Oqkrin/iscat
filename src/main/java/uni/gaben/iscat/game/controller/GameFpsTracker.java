package uni.gaben.iscat.game.controller;

/**
 * Calcola gli FPS correnti usando una media mobile su una finestra di frame.
 */
public class GameFpsTracker {

    private static final int WINDOW = 60;

    private final long[] frameTimes = new long[WINDOW];
    private int  frameIndex = 0;
    private int  frameCount = 0;
    private long lastFrame  = 0;
    private long sumNs      = 0;
    private int  currentFps = 0;

    /**
     * Deve essere chiamato ogni frame con il timestamp corrente (nanoseconds).
     * @param now System.nanoTime() o il valore passato da AnimationTimer.handle()
     */
    public void update(long now) {
        if (lastFrame > 0) {
            long dt = now - lastFrame;
            sumNs -= frameTimes[frameIndex];
            frameTimes[frameIndex] = dt;
            sumNs += dt;
            frameIndex = (frameIndex + 1) % WINDOW;
            if (frameCount < WINDOW) frameCount++;
            currentFps = (int) (1_000_000_000L * frameCount / sumNs);
        }
        lastFrame = now;
    }

    public int getFps() { return currentFps; }
}
