package uni.gaben.iscat.utils;

/**
 * Time‑based cooldown with two modes:
 * <ul>
 *   <li><b>Variable</b> – constructed with {@code new Cooldown()};
 *        call {@link #start(double)} each activation.</li>
 *   <li><b>Fixed</b>    – constructed with {@code new Cooldown(double)};
 *        call {@link #start()} (no argument) to use the preset duration.</li>
 * </ul>
 */
public class Cooldown implements Updatable {
    private double maxDuration = 0;
    private double timeRemaining = 0;
    private double defaultDuration;

    /** Variable‑duration cooldown – ready immediately. */
    public Cooldown() {
        this.defaultDuration = -1;  // marker: no default
    }

    /** Fixed‑duration cooldown – ready immediately. */
    public Cooldown(double defaultDuration) {
        this.defaultDuration = Math.max(0, defaultDuration);
    }

    /** Start with the preset default duration. */
    public void start() {
        if (defaultDuration <= 0) {
            throw new IllegalStateException("No default duration set; use start(double)");
        }
        start(defaultDuration);
    }

    /** Start with a specific duration (also updates max for progress). */
    public void start(double durationInSeconds) {
        this.maxDuration = Math.max(0, durationInSeconds);
        this.timeRemaining = this.maxDuration;
    }

    /** Call every frame. */
    @Override
    public void update(double dt) {
        if (timeRemaining > 0) {
            timeRemaining = Math.max(0, timeRemaining - dt);
        }
    }

    public boolean isReady()       { return timeRemaining <= 0; }
    public boolean isCoolingDown() { return timeRemaining > 0; }

    /** Immediately makes it ready. */
    public void reset() {
        this.timeRemaining = 0;
    }

    public void setDefaultDuration(double defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    public double getDefaultDuration() {
        return defaultDuration;
    }

    /** @return 0 (just started) to 1 (ready). */
    public double getProgress() {
        if (maxDuration <= 0) return 1.0;
        return 1.0 - (timeRemaining / maxDuration);
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }
}