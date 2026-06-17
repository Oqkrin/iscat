package uni.gaben.iscat.universe.entities;

public enum SteeringGoalIndex {
    IDLE("idle"),
    PURSUIT("pursuit"),
    EVADE("evade"),
    PURSUIT_WITH_RANGE("pursuitWithRange"),
    EVADE_WITH_RANGE("evadeWithRange"),
    ORBIT("Orbit");       // used by iscat_dasher — currently aliases to idle

    public final String jsonKey;

    SteeringGoalIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #IDLE} on unknown values
     * and logs a warning rather than throwing, so a bad JSON doesn't crash the game.
     */
    public static SteeringGoalIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (SteeringGoalIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[SteeringGoalIndex] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}
