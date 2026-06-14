package uni.gaben.iscat.universe.entity.brain;

public enum ModifierType {
    SEPARATION("separation"),
    ALIGNMENT("alignment"),
    COHESION("cohesion"),
    COLLISION_AVOIDANCE("collisionAvoidance");

    public final String jsonKey;

    ModifierType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public static ModifierType fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (ModifierType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[ModifierType] Unknown type '" + s + "' — modifier will be skipped");
        return null;
    }
}
