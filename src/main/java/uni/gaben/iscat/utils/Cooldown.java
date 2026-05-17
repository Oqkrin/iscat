package uni.gaben.iscat.utils;

/**
 * Utility class to manage time-based cooldowns using delta time (dt).
 */
public class Cooldown {
    private double maxDuration = 0;
    private double timeRemaining = 0;

    /** Creates a cooldown that is ready immediately. */
    public Cooldown() {}

    /**
     * Triggers the cooldown for a specific duration in seconds.
     */
    public void start(double durationInSeconds) {
        this.maxDuration = Math.max(0, durationInSeconds);
        this.timeRemaining = this.maxDuration;
    }

    /**
     * Updates the internal timer. Call this inside your model's update loop.
     */
    public void update(double dt) {
        if (timeRemaining > 0) {
            timeRemaining = Math.max(0, timeRemaining - dt);
        }
    }

    public boolean isReady() {
        return timeRemaining <= 0;
    }

    public boolean isCoolingDown() {
        return timeRemaining > 0;
    }

    public void reset() {
        this.timeRemaining = 0;
    }

    /**
     * @return Values from 0.0 (just started) to 1.0 (ready for action!)
     */
    public double getProgress() {
        if (maxDuration <= 0) return 1.0;
        return 1.0 - (timeRemaining / maxDuration);
    }

    public double getTimeRemaining() {
        return timeRemaining;
    }
}