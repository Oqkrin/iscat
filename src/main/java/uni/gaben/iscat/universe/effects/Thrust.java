package uni.gaben.iscat.universe.effects;

import org.dyn4j.geometry.Vector2;

public class Thrust {
    // === Engine Particle Thrust Effects ===
    public static final int THRUST_MIN_PARTICLES = 12;
    public static final int THRUST_EXTRA_PARTICLES = 48;
    public static final double THRUST_SPREAD_X_FACTOR = 1.0;
    public static final double THRUST_MIN_PARTICLE_SIZE = 1.0;
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 7.0;
    private double intensity;          // 0..1, based on current speed / max speed
    private final Vector2 localDrift;        // lateral drift vector in local space
    private double shipWidth;          // width of the ship in pixels
    private double shipHeight;         // height of the ship in pixels
    private boolean active = true;

    public Thrust() {
        this.localDrift = new Vector2();
    }

    public void update(double intensity, Vector2 localDrift, double shipWidth, double shipHeight) {
        this.intensity = Math.clamp(intensity, 0.0, 1.0);
        this.localDrift.set(localDrift);
        this.shipWidth = shipWidth;
        this.shipHeight = shipHeight;
    }

    public double getIntensity() { return intensity; }
    public Vector2 getLocalDrift() { return localDrift; }
    public double getShipWidth() { return shipWidth; }
    public double getShipHeight() { return shipHeight; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}