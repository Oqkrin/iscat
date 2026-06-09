package uni.gaben.iscat.universe.entity;

/**
 * Standardized data class for all entity configuration.
 * Designed to be easily serializable/deserializable from an SQLite database or JSON in the future.
 * Maps directly to the Entity table schema.
 */
public class PhysicalEntitySettings {
    // Physical properties
    public double initLife;
    public double scale = 1.0;
    public double linearDamping = 2.0;
    public double mass = 1.0;
    
    // Movement properties
    public double maxVelocity = 10.0;
    public double maxForce = 10.0;
    public double maxAngularVelocity = 5.0;
    
    // Behavioral properties
    public double detectionRange = 15.0;
    public double combatRange = 10.0;
    public double preferredRange = 7.0;

    // Rewards
    public int xpReward = 10;

    /** Tipologia geometrica della fixture fisica da applicare al corpo dell'entità. */
    public ShapeType shapeType = ShapeType.CIRCLE;
    public double widthPx;
    public double heightPx;

    /**
     * Definisce le geometrie primitive supportate per il corpo rigido (Collider) del motore fisico.
     */
    public enum ShapeType {
        CIRCLE,
        SQUARE;

        /**
         * Converte in modo sicuro una stringa nella costante enumerativa corrispondente.
         * In caso di valore nullo o non riconosciuto, applica il fallback di sicurezza su CIRCLE.
         */
        public static ShapeType fromString(String s) {
            if (s == null) return CIRCLE;
            return switch (s.trim().toUpperCase()) {
                case "SQUARE" -> SQUARE;
                default       -> CIRCLE;
            };
        }
    }
}
