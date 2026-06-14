package uni.gaben.iscat.universe.entity.brain;

public enum SteeringGoalType {
    IDLE("idle"),
    PURSUIT("pursuit"),
    EVADE("evade"),
    PURSUIT_WITH_RANGE("pursuitWithRange"),
    EVADE_WITH_RANGE("evadeWithRange"),
    ORBIT("Orbit");       // used by iscat_dasher — currently aliases to idle

    public final String jsonKey;

    SteeringGoalType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #IDLE} on unknown values
     * and logs a warning rather than throwing, so a bad JSON doesn't crash the game.
     */
    public static SteeringGoalType fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (SteeringGoalType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[SteeringGoalType] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}
