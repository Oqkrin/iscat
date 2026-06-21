package uni.gaben.iscat.universe.entities.parsed;

public enum ModifierIndex {
    SEPARATION("separation"),
    ALIGNMENT("alignment"),
    COHESION("cohesion"),
    COLLISION_AVOIDANCE("collisionavoidance");

    public final String jsonKey;

    ModifierIndex(String jsonKey) {
        this.jsonKey = jsonKey.toLowerCase();
    }

    public static ModifierIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return null;

        String normalizedInput = s.toLowerCase().trim();

        for (ModifierIndex v : values()) {
            if (v.jsonKey.equalsIgnoreCase(normalizedInput)) {
                return v;
            }
        }
        System.err.println("[ModifierIndex] Unknown type '" + s + "' — modifier will be skipped.");
        return null;
    }
}