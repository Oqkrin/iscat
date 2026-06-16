package uni.gaben.iscat.universe.entity.brain.abilities;


public enum AbilityIndex {
    SHOOT("shoot"),
    RANDOMIZED_SHOOT("randomizedShoot"),
    HEAL("heal"),
    SUMMON("summon"),
    MELEE("melee"),
    KAMIKAZE("kamikaze"),
    DASH("dash"),
    PLUNGE("plunge");

    public final String jsonKey;

    AbilityIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public static AbilityIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (AbilityIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[AbilityIndex] Unknown type '" + s + "' — ability will be skipped");
        return null;
    }
}
