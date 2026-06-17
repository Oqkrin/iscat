package uni.gaben.iscat.universe.entities;

public enum ModifierIndex {
    SEPARATION("separation"),
    ALIGNMENT("alignment"),
    COHESION("cohesion"),
    COLLISION_AVOIDANCE("collisionAvoidance");

    public final String jsonKey;

    ModifierIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public static ModifierIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (ModifierIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[ModifierIndex] Unknown type '" + s + "' — modifier will be skipped");
        return null;
    }
}
