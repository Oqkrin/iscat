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
                    String arrayKey = root.keys().next(); // enemies, players, or environment
                    JSONArray entities = root.getJSONArray(arrayKey);

                    for (int i = 0; i < entities.length(); i++) {
                        JSONObject jsonObj = entities.getJSONObject(i);
                        EntityRecord record = parseEntitySettings(jsonObj);
                        if (record.identity() != null && record.identity().entityKey() != null) {
                            cache.put(record.identity().entityKey().toLowerCase().trim(), record);
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

    private static EntityRecord parseEntitySettings(JSONObject json) {
        IdentityData identity = new IdentityData(
                json.optString("EntityKey", ""),
                json.optString("Name", ""),
                json.optString("Description", ""),
                json.optBoolean("IsBoss", false)
        );

        SpriteData sprite = null;
        if (json.has("SpriteName") || json.has("sprite")) {
            JSONObject sprJson = json.has("sprite") ? json.getJSONObject("sprite") : json;
            String spriteName = sprJson.optString("SpriteName", "").trim();
            if (spriteName.toLowerCase().endsWith(".png")) spriteName = spriteName.substring(0, spriteName.length() - 4);
            sprite = new SpriteData(
                    "/uni/gaben/iscat/sprites/entities/" + spriteName + ".png",
                    sprJson.optInt("FrameW", 32),
                    sprJson.optInt("FrameH", 32),
                    sprJson.optDouble("Scale", 1.0)
            );
        }

        PhysicsData physics = null;
        if (json.has("physics") || json.has("ShapeType")) {
            JSONObject physJson = json.has("physics") ? json.getJSONObject("physics") : json;
            physics = new PhysicsData(
                    ShapeType.valueOf(physJson.optString("ShapeType", "CIRCLE")),
                    physJson.optDouble("mass", 1.0),
                    physJson.optDouble("density", 1.0),
                    physJson.optDouble("LinearDamping", 2.0),
                    physJson.optBoolean("isSensor", false),
                    physJson.optLong("collisionFilter", 1L),
                    physJson.optBoolean("isProjectile", false),
                    physJson.optDouble("radius", 0.0),
                    physJson.optDouble("terminalVelocity", 0.0)
            );
        }

        DynamicsData movement = null;
        if (json.has("dynamics") || json.has("MaxVelocity")) {
            JSONObject movJson = json.has("dynamics") ? json.getJSONObject("dynamics") : json;
            movement = new DynamicsData(
                    movJson.optDouble("MaxVelocity", 10.0),
                    movJson.optDouble("MaxForce", 30.0),
                    movJson.optDouble("MaxAngularVelocity", 5.0),
                    movJson.optDouble("TerminalVelocity", 10.0),
                    movJson.optDouble("actionCooldownS", 800.0) / 1000.0
            );
        }

        EnduranceData endurance = null;
        if (json.has("endurance") || json.has("InitLife")) {
            JSONObject endJson = json.has("endurance") ? json.getJSONObject("endurance") : json;
            endurance = new EnduranceData(
                    endJson.optDouble("InitLife", 100.0),
                    endJson.optDouble("MaxLife", endJson.optDouble("InitLife", 100.0)),
                    endJson.optDouble("CollisionDamageScale", 1.0)
            );
        }

        StateData state = null;
        if (json.has("state") || json.has("HasEntranceAnimation") || json.has("AnimationFrames")) {
            JSONObject stateJson = json.has("state") ? json.getJSONObject("state") : json;
            int[] frames = null;
            if (stateJson.has("AnimationFrames")) {
                JSONArray framesArray = stateJson.getJSONArray("AnimationFrames");
                frames = new int[framesArray.length()];
                for (int i = 0; i < framesArray.length(); i++) {
                    frames[i] = framesArray.optInt(i, 1);
                }
            }
            state = new StateData(
                    stateJson.optBoolean("HasEntranceAnimation", false),
                    frames
            );
        }

        XpData xp = null;
        if (json.has("xp") || json.has("XPReward")) {
            JSONObject xpJson = json.has("xp") ? json.getJSONObject("xp") : json;
            xp = new XpData(
                    xpJson.optInt("XPReward", 10),
                    xpJson.optDouble("XpMultiplier", 1.2)
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
