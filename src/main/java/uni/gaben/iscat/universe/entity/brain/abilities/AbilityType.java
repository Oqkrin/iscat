package uni.gaben.iscat.universe.entity.brain.abilities;


public enum AbilityType {
    SHOOT("shoot"),
    RANDOMIZED_SHOOT("randomizedShoot"),
    HEAL("heal"),
    SUMMON("summon"),
    MELEE("melee"),
    KAMIKAZE("kamikaze"),
    DASH("dash"),
    PLUNGE("plunge");

    public final String jsonKey;

    AbilityType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public static AbilityType fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (AbilityType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[AbilityType] Unknown type '" + s + "' — ability will be skipped");
        return null;
    }
}
