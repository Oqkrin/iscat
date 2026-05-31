package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.universe.lib.abstracts.EntitySettings;

/**
 * Extends EntitySettings with the extra fields stored in the Entita table.
 * Every field here maps 1-to-1 to a DB column — no hardcoded values anywhere.
 * DB columns added (migration script / IscatDB.db):
 *   ShapeType    TEXT  DEFAULT 'CIRCLE'       — fixture geometry
 *   BehaviorType TEXT  DEFAULT 'WANDER_SHOOT' — brain wiring
 * Existing Entita columns consumed here:
 *   EntityKey, Name, Description, SpritePath, FrameW, FrameH
 *   + all columns already covered by EntitySettings
 */
public class GenericEntitySettings extends EntitySettings {

    // ── Identity ──────────────────────────────────────────────────────────────
    /** Primary key used to spawn this enemy (e.g. "iscat_mob"). */
    public String entityKey = "";

    /** Display name shown in the bestiary. */
    public String name = "";

    /** Lore description shown in the bestiary. */
    public String description = "";

    // ── Sprite ────────────────────────────────────────────────────────────────
    /** Classpath resource path to the spritesheet PNG. */
    public String spritePath = "";

    /** Width of a single animation frame in pixels. */
    public int frameW = 32;

    /** Height of a single animation frame in pixels. */
    public int frameH = 32;

    // ── Physics shape ─────────────────────────────────────────────────────────
    /**
     * Fixture geometry to create for this enemy.
     * CIRCLE → Geometry.createCircle(radius)
     * SQUARE → Geometry.createSquare(side)
     */
    public ShapeType shapeType = ShapeType.CIRCLE;

    // ── AI wiring ─────────────────────────────────────────────────────────────
    /**
     * Which Brain configuration to apply.
     * WANDER_SHOOT → wandering + LineOfSight shoot action (like IscatMob)
     * RAM          → charge at player, no shooting (like IscatCore / Dasher)
     * IDLE         → no movement, no action (like worm body segments)
     */
    public BehaviorType behaviorType = BehaviorType.WANDER_SHOOT;

    // ── Enums ─────────────────────────────────────────────────────────────────

    public enum ShapeType {
        CIRCLE,
        SQUARE;

        public static ShapeType fromString(String s) {
            if (s == null) return CIRCLE;
            return switch (s.trim().toUpperCase()) {
                case "SQUARE" -> SQUARE;
                default       -> CIRCLE;
            };
        }
    }

    public enum BehaviorType {
        WANDER_SHOOT,
        RAM,
        IDLE;

        public static BehaviorType fromString(String s) {
            if (s == null) return WANDER_SHOOT;
            return switch (s.trim().toUpperCase()) {
                case "RAM"  -> RAM;
                case "IDLE" -> IDLE;
                default     -> WANDER_SHOOT;
            };
        }
    }
}