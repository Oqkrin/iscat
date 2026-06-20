package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Updatable;

public class TrajectoryModifier implements Updatable {

    private final Vector2 direction;      // unit forward vector
    private final Vector2 perpendicular;  // unit left‑hand perpendicular (never mutated)
    private double amplitude;
    private double frequency;
    private double phaseOffset;           // initial phase in radians (0 = start at centre)
    private double elapsedTime;

    // reusable scratch vectors
    private final Vector2 lateralScratch = UU.vector2zero();
    private final Vector2 trajectoryOut   = UU.vector2zero();

    public TrajectoryModifier(Vector2 direction, Vector2 perpendicular,
                              double amplitude, double frequency) {
        this(direction, perpendicular, amplitude, frequency, 0.0);
    }

    public TrajectoryModifier(Vector2 direction, Vector2 perpendicular,
                              double amplitude, double frequency, double phaseOffset) {
        this.direction = direction.getNormalized();
        this.perpendicular = perpendicular.getNormalized();  // always stays unit
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phaseOffset = phaseOffset;
        this.elapsedTime = 0.0;
    }

    @Override
    public void update(double dt) {
        elapsedTime += dt;
    }

    /** Current lateral velocity vector (perpendicular * amplitude * sin(…)) */
    public Vector2 getLateralVelocity() {
        double angle = 2.0 * Math.PI * frequency * elapsedTime + phaseOffset;
        double lateralSpeed = amplitude * Math.sin(angle);
        lateralScratch.set(perpendicular).multiply(lateralSpeed);
        return lateralScratch;
    }

    /** Combined forward + lateral velocity */
    public Vector2 getTrajectory(double forwardSpeed) {
        trajectoryOut.set(direction).multiply(forwardSpeed).add(getLateralVelocity());
        return trajectoryOut;
    }

    // --- live tuning ---
    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }
    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }
    public Vector2 getDirection() { return direction; }

    /** Resets time to zero (use when reusing the same modifier instance) */
    public void resetTime() {
        elapsedTime = 0.0;
    }
}