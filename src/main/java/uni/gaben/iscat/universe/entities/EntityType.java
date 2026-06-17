package uni.gaben.iscat.universe.entities;

public enum EntityType {
        PLAYER("player"),
        ISCAT("iscat"),
        GOBLIN("collisionAvoidance"),
        PROJECTILE("projectile");

        public final String jsonKey;

    EntityType(String jsonKey) {
            this.jsonKey = jsonKey;
        }

        public static EntityType fromJson(String s) {
            if (s == null || s.isEmpty()) return null;
            for (EntityType v : values()) {
                if (v.jsonKey.equals(s)) return v;
            }
            System.err.println("[ModifierIndex] Unknown type '" + s + "' — modifier will be skipped");
            return null;
        }
    }
