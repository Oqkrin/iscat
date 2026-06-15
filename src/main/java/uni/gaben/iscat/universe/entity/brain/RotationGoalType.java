package uni.gaben.iscat.universe.entity.brain;

public enum RotationGoalType {
    STILL("still"),
    IDLE("idle"),
    MOVEMENT("movement"),
    TARGET("target"),
    CONTINUES_SPIN("continuesSpin"),
    INTERVAL_SPIN("intervalSpin");

    public final String jsonKey;

    RotationGoalType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #IDLE} on unknown values.
     */
    public static RotationGoalType fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (RotationGoalType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[RotationGoalType] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}
