package uni.gaben.iscat.gamenex.world;

public final class PhysicsSettings {
    private PhysicsSettings() {}
    
    // Dyn4j uses MKS (Meters-Kilograms-Seconds).
    // For proper physics simulation without astronomical numbers, we map 1 meter to 64 pixels.
    public static final double SCALE = 32.0;
}
