package uni.gaben.iscat.universe.entities;

public enum RotationGoalIndex {
    STILL("still"),
    IDLE("idle"),
    MOVEMENT("movement"),
    TARGET("target"),
    CONTINUES_SPIN("continuesSpin"),
    INTERVAL_SPIN("intervalSpin"),
    LOCKED("locked");

    public final String jsonKey;

    RotationGoalIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #IDLE} on unknown values.
     */
    public static RotationGoalIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (RotationGoalIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[RotationGoalIndex] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}
