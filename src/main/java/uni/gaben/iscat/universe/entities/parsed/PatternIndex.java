package uni.gaben.iscat.universe.entities.parsed;

/**
 * All pattern shooter types recognised by the JSON parser.
 * The {@link #jsonKey} is the canonical string used in JSON files.
 */
public enum PatternIndex {
    SINGLE_SHOT("singleShot"),
    SPREAD("spread"),
    MULTI_DIRECTION("multiDirection"),
    RING("ring"),
    REPEATER("repeater"),
    PARALLEL_LINE("parallelLine"),
    SUMMON("summon"),
    FIGURE("figure"),
    VARROW("varrow"),
    SPIRAL("spiral");

    public final String jsonKey;

    PatternIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Case-sensitive lookup by JSON key. Falls back to {@link #SINGLE_SHOT} on unknown values.
     */
    public static PatternIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return SINGLE_SHOT;
        for (PatternIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[PatternIndex] Unknown type '" + s + "' — falling back to SINGLE_SHOT");
        return SINGLE_SHOT;
    }
}
