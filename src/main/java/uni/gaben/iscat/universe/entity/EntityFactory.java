package uni.gaben.iscat.universe.entity;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityFactory {

    private EntityFactory() {}

    private static final Map<String, EntityRecord> cache = new ConcurrentHashMap<>();
    private static final String JSON_PATH = "/uni/gaben/iscat/json/enemies.json";

    // ========================= BUILDER =========================

    public static class EntityRecordBuilder {
        // Identity
        private String entityKey = "";
        private String name = "";
        private String description = "";
        // Visual
        private String spritePath = "";
        private int frameW = 32;
        private int frameH = 32;
        private double scale = 1.0;
        private int[] animationFrames = null;
        private boolean isBoss = false;
        private boolean hasEntranceAnimation = false;
        // Physical
        private double initLife = 100;
        private double linearDamping = 2.0;
        private double mass = 1.0;
        private double maxVelocity = 10.0;
        private double maxForce = 10.0;
        private double maxAngularVelocity = 5.0;
        private int xpReward = 10;
        private EntityRecord.ShapeType shapeType = EntityRecord.ShapeType.CIRCLE;
        // Behavioural
        private double detectionRange = 15.0;
        private double combatRange = 10.0;
        private double preferredRange = 7.0;
        private double actionCooldownSec = 0.8;
        // Audio
        private EntityRecord.AudioProfile audio = new EntityRecord.AudioProfile(List.of(), List.of(), List.of(), List.of(), List.of());
        // AI
        private EntityRecord.BrainRecord brain = null;
        // Player
        private EntityRecord.PlayerRecord player = null;

        // Fluent setters
        public EntityRecordBuilder entityKey(String v) { entityKey = v; return this; }
        public EntityRecordBuilder name(String v) { name = v; return this; }
        public EntityRecordBuilder description(String v) { description = v; return this; }
        public EntityRecordBuilder spritePath(String v) { spritePath = v; return this; }
        public EntityRecordBuilder frameW(int v) { frameW = v; return this; }
        public EntityRecordBuilder frameH(int v) { frameH = v; return this; }
        public EntityRecordBuilder scale(double v) { scale = v; return this; }
        public EntityRecordBuilder animationFrames(int[] v) { animationFrames = v; return this; }
        public EntityRecordBuilder isBoss(boolean v) { isBoss = v; return this; }
        public EntityRecordBuilder hasEntranceAnimation(boolean v) { hasEntranceAnimation = v; return this; }
        public EntityRecordBuilder initLife(double v) { initLife = v; return this; }
        public EntityRecordBuilder linearDamping(double v) { linearDamping = v; return this; }
        public EntityRecordBuilder mass(double v) { mass = v; return this; }
        public EntityRecordBuilder maxVelocity(double v) { maxVelocity = v; return this; }
        public EntityRecordBuilder maxForce(double v) { maxForce = v; return this; }
        public EntityRecordBuilder maxAngularVelocity(double v) { maxAngularVelocity = v; return this; }
        public EntityRecordBuilder xpReward(int v) { xpReward = v; return this; }
        public EntityRecordBuilder shapeType(EntityRecord.ShapeType v) { shapeType = v; return this; }
        public EntityRecordBuilder detectionRange(double v) { detectionRange = v; return this; }
        public EntityRecordBuilder combatRange(double v) { combatRange = v; return this; }
        public EntityRecordBuilder preferredRange(double v) { preferredRange = v; return this; }
        public EntityRecordBuilder actionCooldownSec(double v) { actionCooldownSec = v; return this; }
        public EntityRecordBuilder audio(EntityRecord.AudioProfile v) { audio = v; return this; }
        public EntityRecordBuilder brain(EntityRecord.BrainRecord v) { brain = v; return this; }
        public EntityRecordBuilder player(EntityRecord.PlayerRecord v) { player = v; return this; }

        public EntityRecord build() {
            return new EntityRecord(
                    entityKey, name, description,
                    spritePath, frameW, frameH, scale,
                    animationFrames, isBoss, hasEntranceAnimation,
                    initLife, linearDamping, mass, maxVelocity, maxForce, maxAngularVelocity, xpReward, shapeType,
                    detectionRange, combatRange, preferredRange, actionCooldownSec,
                    audio, brain, player
            );
        }
    }

    // ========================= FACTORY METHODS =========================

    public static EntityModel spawn(
            String entityKey,
            double x, double y,
            UniverseModel universe,
            UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        EntityRecord record = loadRecord(normalizedKey);
        if (record == null) {
            System.err.println("[EntityFactory] Impossibile spawnare: EntityKey sconosciuta '" + normalizedKey + "'");
            return null;
        }

        EntityModel model = new EntityModel(x, y, record);
        EntityBrain brain = EntityBrain.fromRecord(model);

        universe.addEntity(model);
        controller.addEntityController(brain);

        return model;
    }

    public static CompletableFuture<Void> preloadAllAsync() {
        return CompletableFuture.runAsync(() -> {
            try (InputStream is = EntityFactory.class.getResourceAsStream(JSON_PATH)) {
                if (is == null) {
                    throw new RuntimeException("File JSON non trovato in: " + JSON_PATH);
                }

                String jsonText = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                JSONObject root = new JSONObject(jsonText);
                JSONArray enemies = root.getJSONArray("enemies");

                cache.clear();

                for (int i = 0; i < enemies.length(); i++) {
                    JSONObject jsonEnemy = enemies.getJSONObject(i);
                    EntityRecord record = parseEnemySettings(jsonEnemy);
                    if (record.entityKey() != null) {
                        cache.put(record.entityKey().toLowerCase().trim(), record);
                    }
                }
                System.out.println("[EntityFactory] Cache JSON pronta. Pre-caricate " + cache.size() + " definizioni.");

            } catch (Exception ex) {
                System.err.println("[EntityFactory] Errore critico nel parsing del JSON: " + ex.getMessage());
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        });
    }

    private static EntityRecord loadRecord(String entityKey) {
        EntityRecord cached = cache.get(entityKey);
        if (cached != null) {
            return cached;
        }

        System.err.println("[PERFORMANCE WARNING] Cache-miss per '" + entityKey + "'! Ricarico il file JSON in modo sincrono.");
        try {
            preloadAllAsync().get();
            return cache.get(entityKey);
        } catch (Exception e) {
            System.err.println("[EntityFactory] Impossibile recuperare l'entità dopo il cache-miss.");
            return null;
        }
    }

    private static EntityRecord parseEnemySettings(JSONObject json) {
        EntityRecordBuilder builder = new EntityRecordBuilder();

        // Identity
        builder.entityKey(json.optString("EntityKey", ""))
                .name(json.optString("Name", ""))
                .description(json.optString("Description", ""));

        // Sprite path
        String spriteName = json.optString("SpriteName", "").trim();
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }
        builder.spritePath("/uni/gaben/iscat/sprites/enemies/" + spriteName + ".png");

        // Visual
        builder.frameW(json.optInt("FrameW", 32))
                .frameH(json.optInt("FrameH", 32))
                .shapeType(EntityRecord.ShapeType.valueOf(json.optString("ShapeType", "CIRCLE")))
                .scale(json.optDouble("Scale", 1.0));

        // Physical
        builder.initLife(json.optInt("InitLife", 100))
                .linearDamping(json.optDouble("LinearDamping", 2.0))
                .mass(json.optDouble("mass", 1.0))
                .maxVelocity(json.optDouble("MaxVelocity", 10.0))
                .maxForce(json.optDouble("MaxForce", 30.0))
                .maxAngularVelocity(json.optDouble("MaxAngularVelocity", 5.0))
                .xpReward(json.optInt("XPReward", 10));

        // Behavioural
        builder.detectionRange(json.optDouble("DetectionRange", 15.0))
                .combatRange(json.optDouble("CombatRange", 10.0))
                .preferredRange(json.optDouble("PreferredRange", 10.0))
                .actionCooldownSec(json.optDouble("actionCooldownS", 800.0) / 1000.0); // ms → sec

        // Boss flags
        builder.isBoss(json.optBoolean("IsBoss", false))
                .hasEntranceAnimation(json.optBoolean("HasEntranceAnimation", false));

        // Animation frames
        if (json.has("AnimationFrames")) {
            JSONArray framesArray = json.getJSONArray("AnimationFrames");
            int[] frames = new int[framesArray.length()];
            for (int i = 0; i < framesArray.length(); i++) {
                frames[i] = framesArray.optInt(i, 1);
            }
            builder.animationFrames(frames);
        }

        // Audio
        if (json.has("audio")) {
            JSONObject audioJson = json.getJSONObject("audio");
            builder.audio(new EntityRecord.AudioProfile(
                    jsonArrayToList(audioJson.optJSONArray("attack")),
                    jsonArrayToList(audioJson.optJSONArray("idle")),
                    jsonArrayToList(audioJson.optJSONArray("hurt")),
                    jsonArrayToList(audioJson.optJSONArray("death")),
                    jsonArrayToList(audioJson.optJSONArray("spawn"))
            ));
        }

        // AI (optional)
        if (json.has("ai")) {
            builder.brain(parseBrain(json.getJSONObject("ai")));
        }

        // Player (not present in enemies.json, but builder may set later)
        return builder.build();
    }

    private static EntityRecord.BrainRecord parseBrain(JSONObject aiJson) {
        // Build each component with inner builders
        EntityRecord.SteeringRecord steering = parseSteering(aiJson.optJSONObject("steering"));
        EntityRecord.RotationRecord rotation = parseRotation(aiJson.optJSONObject("rotation"));
        List<EntityRecord.AbilityRecord> abilities = parseAbilities(aiJson.optJSONArray("abilities"));
        List<EntityRecord.ModifierRecord> modifiers = parseModifiers(aiJson.optJSONArray("modifiers"));
        return new EntityRecord.BrainRecord(steering, rotation, abilities, modifiers);
    }

    private static EntityRecord.SteeringRecord parseSteering(JSONObject obj) {
        if (obj == null) return new EntityRecord.SteeringRecord("idle", 2.5, 2.5, 10.0, 5.0);
        return new EntityRecord.SteeringRecord(
                obj.optString("type", "idle"),
                obj.optDouble("maxPredictionTime", 2.5),
                obj.optDouble("minDistance", 2.5),
                obj.optDouble("maxDistance", 10.0),
                obj.optDouble("safetyDistance", 5.0)
        );
    }

    private static EntityRecord.RotationRecord parseRotation(JSONObject obj) {
        if (obj == null) return new EntityRecord.RotationRecord("idle", 0.0, 8, 0.1, "player");
        return new EntityRecord.RotationRecord(
                obj.optString("type", "idle"),
                obj.optDouble("spinSpeedRadPerSec", 0.0),
                obj.optInt("spinSteps", 8),
                obj.optDouble("stepPauseSec", 0.1),
                obj.optString("target", "player")
        );
    }

    private static List<EntityRecord.AbilityRecord> parseAbilities(JSONArray arr) {
        List<EntityRecord.AbilityRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            EntityRecord.AbilityRecord ac = new EntityRecord.AbilityRecord(
                    obj.optString("type"),
                    obj.optDouble("combatRange", 10.0),
                    obj.optDouble("cooldownSec", 0.8),
                    obj.optString("bulletType", "ENEMY_BULLET"),
                    obj.optBoolean("aimAtTarget", true),
                    obj.optDouble("nerfPrediction", 0.0),
                    parsePatternList(obj.optJSONArray("patterns")),
                    parsePattern(obj.optJSONObject("pattern")),
                    obj.optDouble("healAmount", 0.0),
                    obj.optString("summonEntityKey"),
                    obj.optInt("summonCount", 1),
                    obj.optDouble("summonRadiusPx", 100.0),
                    obj.optDouble("meleeDamage", 0.0),
                    obj.optInt("attackStateIndex", 4)
            );
            list.add(ac);
        }
        return list;
    }

    private static List<EntityRecord.PatternRecord> parsePatternList(JSONArray arr) {
        List<EntityRecord.PatternRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(parsePattern(arr.getJSONObject(i)));
        }
        return list;
    }

    private static EntityRecord.PatternRecord parsePattern(JSONObject obj) {
        if (obj == null) return null;
        return new EntityRecord.PatternRecord(
                obj.optString("type"),
                obj.optInt("count", 1),
                obj.optDouble("angleStepDeg", 30.0),
                obj.optDouble("intervalSec", 0.15),
                obj.optInt("repeats", 1),
                obj.optString("summonedEntityKey"),
                obj.optDouble("summonRadiusPx", 100.0),
                obj.optString("figureType", "CIRCLE")
        );
    }

    private static List<EntityRecord.ModifierRecord> parseModifiers(JSONArray arr) {
        List<EntityRecord.ModifierRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new EntityRecord.ModifierRecord(
                    obj.optString("type"),
                    obj.optDouble("radius", 2.0),
                    obj.optDouble("weight", 1.0),
                    obj.optDouble("maxPredictionTime", 1.0),
                    obj.optDouble("avoidRadius", 2.0)
            ));
        }
        return list;
    }

    private static List<String> jsonArrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        }
        return list;
    }

    public static Map<String, EntityRecord> getCache() {
        return cache;
    }
}