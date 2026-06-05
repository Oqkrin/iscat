package uni.gaben.iscat.utils.sprite;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Drives a spritesheet animation by tracking elapsed time and mapping it to
 * the correct frame index for the active state row.
 *
 * <p>Supports <em>variable-length states</em>: each row (state) can have a
 * different number of frames, as detected by {@link SpriteSheetsParser}.</p>
 *
 * <h3>Constructors</h3>
 * <ul>
 *   <li>{@link #SpriteSheetsAnimator(double, int[])} – preferred; accepts the
 *       per-row frame count array directly from
 *       {@code SpriteSheetsParser.getFramesPerRow()}.</li>
 *   <li>{@link #SpriteSheetsAnimator(double, int, int)} – legacy convenience
 *       constructor; creates a uniform grid (all rows have the same frame
 *       count). Existing callers are not broken.</li>
 * </ul>
 */
public class SpriteSheetsAnimator {

    private double internalTime = 0;
    private int currentState   = 0;

    /**
     * Jagged duration matrix: {@code frameDurations[state][frame]}.
     * Each inner array has length == framesPerRow[state].
     */
    private DoubleProperty[][] frameDurations;

    // ── Constructors ─────────────────────────────────────────────────────────

    /**
     * Variable-length constructor.
     *
     * @param defaultFrameDuration Seconds each frame is displayed (uniform default).
     * @param framesPerRow         Per-row frame counts from
     *                             {@link SpriteSheetsParser#getFramesPerRow()}.
     */
    public SpriteSheetsAnimator(double defaultFrameDuration, int[] framesPerRow) {
        buildMatrix(defaultFrameDuration, framesPerRow);
    }

    /**
     * Legacy uniform-grid constructor – all rows have the same frame count.
     * Kept for backward compatibility with {@code AnimatedCanvas} and
     * {@code PlayerView}.
     *
     * @param defaultFrameDuration Seconds each frame is displayed.
     * @param framesCount          Frames per row.
     * @param statesCount          Number of rows (animation states).
     */
    public SpriteSheetsAnimator(double defaultFrameDuration, int framesCount, int statesCount) {
        int[] uniform = new int[statesCount];
        for (int i = 0; i < statesCount; i++) uniform[i] = framesCount;
        buildMatrix(defaultFrameDuration, uniform);
    }

    // ── Matrix initialisation ────────────────────────────────────────────────

    private void buildMatrix(double defaultDuration, int[] framesPerRow) {
        frameDurations = new DoubleProperty[framesPerRow.length][];
        for (int s = 0; s < framesPerRow.length; s++) {
            int count = Math.max(1, framesPerRow[s]);
            frameDurations[s] = new DoubleProperty[count];
            for (int f = 0; f < count; f++) {
                frameDurations[s][f] = new SimpleDoubleProperty(defaultDuration);
            }
        }
    }

    /**
     * Re-initialises the matrix with a uniform duration. Retained for
     * compatibility; prefer the constructor overloads instead.
     */
    public void constantDurationFiller(double defaultFrameDuration, int framesCount, int statesCount) {
        int[] uniform = new int[statesCount];
        for (int i = 0; i < statesCount; i++) uniform[i] = framesCount;
        buildMatrix(defaultFrameDuration, uniform);
    }

    // ── Frame duration control ───────────────────────────────────────────────

    /**
     * Returns the bindable {@link DoubleProperty} for a specific (state, frame)
     * pair so external systems can drive playback speed reactively.
     */
    public DoubleProperty durationProperty(int state, int frame) {
        if (!inBounds(state, frame)) return new SimpleDoubleProperty(0);
        return frameDurations[state][frame];
    }

    /** Sets a frame duration directly, without a JavaFX binding. */
    public void setFrameDuration(int state, int frame, double duration) {
        if (frameDurations == null || !inBounds(state, frame)) return;
        frameDurations[state][frame].set(duration);
    }

    // ── Playback ─────────────────────────────────────────────────────────────

    public void update(double deltaTime) {
        internalTime += deltaTime;
    }

    /**
     * Returns the current frame index within the active state, looping
     * correctly over the (potentially variable-length) frame list.
     */
    public int getCurrentFrame() {
        if (frameDurations == null || frameDurations.length == 0) return 0;

        DoubleProperty[] durations = frameDurations[currentState];
        int len = durations.length;

        double totalDuration = 0;
        for (DoubleProperty p : durations) totalDuration += p.get();
        if (totalDuration <= 0) return 0;

        double timeInCycle = internalTime % totalDuration;
        double accumulated = 0;

        for (int i = 0; i < len; i++) {
            accumulated += durations[i].get();
            if (timeInCycle < accumulated) return i;
        }
        return len - 1;
    }

    public void setState(int newState) {
        if (frameDurations == null) return;
        if (newState >= 0 && newState < frameDurations.length && newState != currentState) {
            currentState = newState;
            reset();
        }
    }

    /** @return {@code true} once {@code internalTime} has covered the full cycle duration. */
    public boolean hasCompletedCycle() {
        if (frameDurations == null || frameDurations.length == 0) return true;

        DoubleProperty[] durations = frameDurations[currentState];
        double total = 0;
        for (DoubleProperty p : durations) total += p.get();
        return internalTime >= total;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public int getCurrentState() { return currentState; }
    public double getTime()      { return internalTime; }

    public void setTime(double time) { this.internalTime = time; }
    public void reset()              { this.internalTime = 0; }

    // ── Internals ────────────────────────────────────────────────────────────

    private boolean inBounds(int state, int frame) {
        if (state  < 0 || state  >= frameDurations.length)         return false;
        if (frame  < 0 || frame  >= frameDurations[state].length)  return false;
        return true;
    }
}