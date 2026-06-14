package uni.gaben.iscat.universe.entity.shooters;

/**
 * All pattern shooter types recognised by the JSON parser.
 * The {@link #jsonKey} is the canonical string used in JSON files.
 */
public enum PatternType {
    SINGLE_SHOT("singleShot"),
    SPREAD("spread"),
    MULTI_DIRECTION("multiDirection"),
    RING("ring"),
    REPEATER("repeater"),
    PARALLEL_LINE("parallelLine"),
    SUMMON("summon"),
    FIGURE("figure");

    public final String jsonKey;

    PatternType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #SINGLE_SHOT} on unknown values.
     */
    public static PatternType fromJson(String s) {
        if (s == null || s.isEmpty()) return SINGLE_SHOT;
        for (PatternType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[PatternType] Unknown type '" + s + "' — falling back to SINGLE_SHOT");
        return SINGLE_SHOT;
    }
}
