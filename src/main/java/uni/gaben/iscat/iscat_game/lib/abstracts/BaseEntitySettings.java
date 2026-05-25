package uni.gaben.iscat.iscat_game.lib.abstracts;

/**
 * Standardized data class for all entity configuration.
 * Designed to be easily serializable/deserializable from an SQLite database or JSON in the future.
 */
public class BaseEntitySettings {
    public double initLife = 10.0;
    public double dimSprite = 32.0;
    public double scale = 1.0;
    public double dampingLineare = 2.0;
    public double maxVelocity = 10.0;
    public double force = 10.0;
    public double rotationSpeed = 5.0;
    public int xpReward = 10;
    
    // Combat variables
    public double detectionRange = 15.0;
    public double combatRange = 10.0;
    public double preferredRange = 7.0;
    public double fireCooldownS = 2.0;
    
    // Generic custom parameters for specific abilities (e.g. heal amount, extra damage)
    public double customParam1 = 0.0;
    public double customParam2 = 0.0;

    public BaseEntitySettings() {}
}
