package uni.gaben.iscat.universe;

import org.dyn4j.geometry.Vector2;

public class Thrust {
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
