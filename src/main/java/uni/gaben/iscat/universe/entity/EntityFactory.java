package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.Data.BrainData;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.modules.*;
import uni.gaben.iscat.universe.entity.Data.*;

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
    private static final String[] JSON_PATHS = {
            "/uni/gaben/iscat/json/enemies.json",
            "/uni/gaben/iscat/json/players.json",
            "/uni/gaben/iscat/json/environment.json",
            "/uni/gaben/iscat/json/projectiles.json"
    };

    public static GameEntity spawn(
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

        GameEntity entity = new GameEntity(x, y, record);

        // Attach modules based on Data presence
        if (record.sprite() != null) {
            entity.addModule(new SpriteModule());
        }
        if (record.physics() != null) entity.addModule(new PhysicsModule());
        if (record.dynamics() != null) entity.addModule(new MovementModule());
        if (record.endurance() != null) entity.addModule(new EnduranceModule());
        if (record.state() != null) entity.addModule(new StateModule());
        if (record.xp() != null) entity.addModule(new XpModule());
        if (record.brain() != null) {
            BrainModule bm = new BrainModule();
            entity.addModule(bm);
            if (controller != null) controller.addEntityController(bm.getBrain());
        }

        universe.addEntity(entity);

        return entity;
    }

    public static CompletableFuture<Void> preloadAllAsync() {
        return CompletableFuture.runAsync(() -> {
            cache.clear();
            for (String path : JSON_PATHS) {
                try (InputStream is = EntityFactory.class.getResourceAsStream(path)) {
                    if (is == null) {
                        System.err.println("[EntityFactory] File JSON non trovato: " + path);
                        continue;
                    }

                    String jsonText = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                            .lines().collect(Collectors.joining("\n"));

                    JSONObject root = new JSONObject(jsonText);
                    EntityType fileType = EntityType.ENEMY;
                    if (path.contains("players")) fileType = EntityType.PLAYER;
                    else if (path.contains("environment")) fileType = EntityType.ENVIRONMENT;
                    else if (path.contains("projectiles")) fileType = EntityType.PROJECTILE;

                    Object rootVal;
                    if (root.length() == 1 && (root.get(root.keys().next()) instanceof JSONArray || root.get(root.keys().next()) instanceof JSONObject)) {
                        String rootKey = root.keys().next();
                        Object val = root.get(rootKey);
                        // If it's a single key but its value doesn't look like a collection, then it's a single entity at root.
                        // Wait, if it's a single entity at root, its value is an object.
                        // Let's check if the inner object has typical entity keys or not.
                        if (val instanceof JSONObject && ((JSONObject)val).has("physics")) {
                            rootVal = root;
                        } else {
                            rootVal = val;
                        }
                    } else {
                        rootVal = root;
                    }

                    if (rootVal instanceof JSONArray) {
                        JSONArray entities = (JSONArray) rootVal;
                        for (int i = 0; i < entities.length(); i++) {
                            JSONObject jsonObj = entities.getJSONObject(i);
                            EntityRecord record = parseEntitySettings(jsonObj, fileType);
                            if (record.identity() != null && record.identity().entityKey() != null) {
                                cache.put(record.identity().entityKey().toLowerCase().trim(), record);
                            }
                        }
                    } else if (rootVal instanceof JSONObject) {
                        JSONObject dict = (JSONObject) rootVal;
                        for (String key : dict.keySet()) {
                            JSONObject jsonObj = dict.getJSONObject(key);
                            // Inject key as fallback EntityKey at root level (legacy) and inside identity block
                            if (!jsonObj.has("EntityKey")) jsonObj.put("EntityKey", key);
                            if (jsonObj.has("identity")) {
                                JSONObject idBlock = jsonObj.getJSONObject("identity");
                                if (!idBlock.has("entityKey") && !idBlock.has("EntityKey")) {
                                    idBlock.put("entityKey", key);
                                }
                            }
                            EntityRecord record = parseEntitySettings(jsonObj, fileType);
                            if (record.identity() != null && record.identity().entityKey() != null
                                    && !record.identity().entityKey().isBlank()) {
                                cache.put(record.identity().entityKey().toLowerCase().trim(), record);
                            } else {
                                // Last-resort: use the dict key itself
                                cache.put(key.toLowerCase().trim(), record);
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("[EntityFactory] Errore critico nel parsing di " + path + ": " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
            System.out.println("[EntityFactory] Cache JSON pronta. Pre-caricate " + cache.size() + " definizioni.");
        });
    }

    private static EntityRecord loadRecord(String entityKey) {
        EntityRecord cached = cache.get(entityKey);
        if (cached != null) return cached;

        System.err.println("[PERFORMANCE WARNING] Cache-miss per '" + entityKey + "'! Ricarico.");
        try {
            preloadAllAsync().get();
            return cache.get(entityKey);
        } catch (Exception e) {
            return null;
        }
    }

    private static EntityRecord parseEntitySettings(JSONObject json, EntityType type) {
        // Identity: either nested under "identity" or flat at root
        JSONObject idJson = json.has("identity") ? json.getJSONObject("identity") : json;
        IdentityData identity = new IdentityData(
                idJson.optString("entityKey", idJson.optString("EntityKey", "")),
                idJson.optString("name",      idJson.optString("Name", "")),
                idJson.optString("description", idJson.optString("Description", "")),
                idJson.optBoolean("isBoss",   idJson.optBoolean("IsBoss", false)),
                type,
                idJson.optString("ownerId", json.optString("ownerId", null))
        );

        // Sprite (enemies only; projectiles and environment render via VFX)
        SpriteData sprite = null;
        if (json.has("sprite")) {
            JSONObject sprJson = json.getJSONObject("sprite");
            // Support both inline spritePath and legacy SpriteName
            String path;
            if (sprJson.has("spritePath")) {
                path = sprJson.getString("spritePath");
            } else {
                String name = sprJson.optString("SpriteName", "").trim();
                if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
                path = "/uni/gaben/iscat/sprites/entities/" + name + ".png";
            }
            sprite = new SpriteData(
                    path,
                    sprJson.optInt("frameW", sprJson.optInt("FrameW", 32)),
                    sprJson.optInt("frameH", sprJson.optInt("FrameH", 32)),
                    sprJson.optDouble("scale", sprJson.optDouble("Scale", 1.0))
            );
        } else if (json.has("SpriteName")) {
            // Flat legacy format
            String name = json.optString("SpriteName", "").trim();
            if (name.toLowerCase().endsWith(".png")) name = name.substring(0, name.length() - 4);
            sprite = new SpriteData(
                    "/uni/gaben/iscat/sprites/entities/" + name + ".png",
                    json.optInt("FrameW", 32),
                    json.optInt("FrameH", 32),
                    json.optDouble("Scale", 1.0)
            );
        }

        // Physics: nested "physics" or flat root
        PhysicsData physics = null;
        if (json.has("physics") || json.has("ShapeType") || json.has("shapeType")) {
            JSONObject physJson = json.has("physics") ? json.getJSONObject("physics") : json;
            String shapeStr = physJson.optString("shapeType", physJson.optString("ShapeType", "CIRCLE")).toUpperCase();
            physics = new PhysicsData(
                    ShapeType.valueOf(shapeStr),
                    physJson.optDouble("mass",   physJson.optDouble("Mass", 1.0)),
                    physJson.optDouble("density", physJson.optDouble("Density", 1.0)),
                    physJson.optDouble("linearDamping", physJson.optDouble("LinearDamping", 2.0)),
                    physJson.optBoolean("isSensor", false),
                    physJson.optLong("collisionFilter", 1L),
                    physJson.optBoolean("isProjectile", false),
                    physJson.optDouble("radius", 0.0),
                    physJson.optDouble("terminalVelocity", 0.0)
            );
        }

        // Dynamics: accept both "dynamics" (new) and "movement" (legacy enemies.json) keys
        DynamicsData movement = null;
        String dynKey = json.has("dynamics") ? "dynamics" : json.has("movement") ? "movement" : null;
        if (dynKey != null || json.has("MaxVelocity")) {
            JSONObject movJson = (dynKey != null) ? json.getJSONObject(dynKey) : json;
            movement = new DynamicsData(
                    movJson.optDouble("maxVelocity",      movJson.optDouble("MaxVelocity", 10.0)),
                    movJson.optDouble("maxForce",         movJson.optDouble("MaxForce", 30.0)),
                    movJson.optDouble("maxAngularVelocity",movJson.optDouble("MaxAngularVelocity", 5.0)),
                    movJson.optDouble("terminalVelocity", movJson.optDouble("TerminalVelocity", 10.0)),
                    movJson.optDouble("actionCooldownS", 800.0) / 1000.0
            );
        }

        EnduranceData endurance = null;
        if (json.has("endurance") || json.has("InitLife") || json.has("initLife")) {
            JSONObject endJson = json.has("endurance") ? json.getJSONObject("endurance") : json;
            double initLife = endJson.optDouble("initLife", endJson.optDouble("InitLife", 100.0));
            endurance = new EnduranceData(
                    initLife,
                    endJson.optDouble("maxLife", endJson.optDouble("MaxLife", initLife)),
                    endJson.optDouble("collisionDamageScale", endJson.optDouble("CollisionDamageScale", 1.0))
            );
        }

        StateData state = null;
        if (json.has("state") || json.has("HasEntranceAnimation") || json.has("AnimationFrames")
                || json.has("hasEntranceAnimation") || json.has("animationFrames")) {
            JSONObject stateJson = json.has("state") ? json.getJSONObject("state") : json;
            int[] frames = null;
            String framesKey = stateJson.has("animationFrames") ? "animationFrames" :
                               stateJson.has("AnimationFrames") ? "AnimationFrames" : null;
            if (framesKey != null) {
                JSONArray framesArray = stateJson.getJSONArray(framesKey);
                frames = new int[framesArray.length()];
                for (int i = 0; i < framesArray.length(); i++) {
                    frames[i] = framesArray.optInt(i, 1);
                }
            }
            state = new StateData(
                    stateJson.optBoolean("hasEntranceAnimation", stateJson.optBoolean("HasEntranceAnimation", false)),
                    frames
            );
        }

        XpData xp = null;
        if (json.has("xp") || json.has("XPReward") || json.has("xpReward")) {
            JSONObject xpJson = json.has("xp") ? json.getJSONObject("xp") : json;
            xp = new XpData(
                    xpJson.optInt("xpReward",    xpJson.optInt("XPReward", 10)),
                    xpJson.optDouble("xpMultiplier", xpJson.optDouble("XpMultiplier", 1.2))
            );
        }

        AudioData audio = null;
        if (json.has("audio")) {
            JSONObject audioJson = json.getJSONObject("audio");
            audio = new AudioData(
                    jsonArrayToList(audioJson.optJSONArray("attack")),
                    jsonArrayToList(audioJson.optJSONArray("idle")),
                    jsonArrayToList(audioJson.optJSONArray("hurt")),
                    jsonArrayToList(audioJson.optJSONArray("death")),
                    jsonArrayToList(audioJson.optJSONArray("spawn"))
            );
        }

        BrainData brain = null;
        if (json.has("ai")) {
            brain = parseBrain(json.getJSONObject("ai"));
        }

        PlayerData player = null;
        if (json.has("player")) {
            JSONObject pJson = json.getJSONObject("player");
            player = new PlayerData(
                    pJson.optDouble("dashImpulse", 5.0),
                    pJson.optDouble("dashDurationSec", 0.2),
                    pJson.optDouble("dashCooldownSec", 1.0),
                    pJson.optDouble("stunDurationSec", 0.5),
                    pJson.optDouble("baseCooldownSec", 0.5),
                    new ArrayList<>() // Parsing level abilities omitted for brevity
            );
        }

        return new EntityRecord(identity, sprite, physics, movement, endurance, state, xp, audio, brain, player);
    }

    private static BrainData parseBrain(JSONObject aiJson) {
        BrainData.SteeringRecord steering = parseSteering(aiJson.optJSONObject("steering"));
        BrainData.RotationRecord rotation = parseRotation(aiJson.optJSONObject("rotation"));
        List<BrainData.AbilityRecord> abilities = parseAbilities(aiJson.optJSONArray("abilities"));
        List<BrainData.ModifierRecord> modifiers = parseModifiers(aiJson.optJSONArray("modifiers"));
        return new BrainData(steering, rotation, abilities, modifiers);
    }

    private static BrainData.SteeringRecord parseSteering(JSONObject obj) {
        if (obj == null) return new BrainData.SteeringRecord("idle", 2.5, 2.5, 10.0, 5.0);
        return new BrainData.SteeringRecord(
                obj.optString("type", "idle"),
                obj.optDouble("maxPredictionTime", 2.5),
                obj.optDouble("minDistance", 2.5),
                obj.optDouble("maxDistance", 10.0),
                obj.optDouble("safetyDistance", 5.0)
        );
    }

    private static BrainData.RotationRecord parseRotation(JSONObject obj) {
        if (obj == null) return new BrainData.RotationRecord("idle", 0.0, 8, 0.1, "player");
        return new BrainData.RotationRecord(
                obj.optString("type", "idle"),
                obj.optDouble("spinSpeedRadPerSec", 0.0),
                obj.optInt("spinSteps", 8),
                obj.optDouble("stepPauseSec", 0.1),
                obj.optString("target", "player")
        );
    }

    private static List<BrainData.AbilityRecord> parseAbilities(JSONArray arr) {
        List<BrainData.AbilityRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new BrainData.AbilityRecord(
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
            ));
        }
        return list;
    }

    private static List<BrainData.PatternRecord> parsePatternList(JSONArray arr) {
        List<BrainData.PatternRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(parsePattern(arr.getJSONObject(i)));
        }
        return list;
    }

    private static BrainData.PatternRecord parsePattern(JSONObject obj) {
        if (obj == null) return null;
        return new BrainData.PatternRecord(
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

    private static List<BrainData.ModifierRecord> parseModifiers(JSONArray arr) {
        List<BrainData.ModifierRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new BrainData.ModifierRecord(
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
