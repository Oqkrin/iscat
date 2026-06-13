package uni.gaben.iscat.universe.entity;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntityFactory {
    private EntityFactory() {}
    private static final Map<String, EntityRecord> cache = new ConcurrentHashMap<>();

    private static final String ENEMIES_DIR = "/uni/gaben/iscat/json/enemies/";

    public static EntityModel spawn(
            String entityKey,
            double x, double y,
            UniverseModel universe,
            UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        EntityRecord entity = loadRecord(normalizedKey);

        if (entity == null) {
            System.err.println("[EntityFactory] Impossibile spawnare: EntityKey sconosciuta '" + normalizedKey + "'");
            return null;
        }

        EntityModel model = new EntityModel(x, y, entity);
        EntityBrain brain = EntityBrain.fromRecord(model);

        universe.addEntity(model);
        controller.addEntityController(brain);

        return model;
    }

    public static CompletableFuture<Void> preloadAllAsync() {
        return CompletableFuture.runAsync(() -> {
            cache.clear();
            int loadedCount = 0;

            try {
                // Troviamo l'URL della cartella usando il ClassLoader
                URL dirUrl = EntityFactory.class.getResource(ENEMIES_DIR);
                if (dirUrl == null) {
                    throw new RuntimeException("Cartella dei nemici non trovata: " + ENEMIES_DIR);
                }

                URI uri = dirUrl.toURI();
                Path myPath;

                // Gestiamo il caso in cui siamo dentro un JAR o in ambiente di sviluppo (IDE)
                if (uri.getScheme().equals("jar")) {
                    FileSystem fileSystem;
                    try {
                        fileSystem = FileSystems.getFileSystem(uri);
                    } catch (FileSystemNotFoundException e) {
                        // Se il file system del JAR non è ancora aperto, lo apriamo
                        fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    }
                    myPath = fileSystem.getPath(ENEMIES_DIR);
                } else {
                    myPath = Paths.get(uri);
                }

                // Scansioniamo dinamicamente tutti i file .json nella cartella
                try (Stream<Path> walk = Files.walk(myPath, 1)) {
                    List<Path> jsonFiles = walk
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .toList();

                    for (Path filePath : jsonFiles) {
                        // Leggiamo il contenuto del file corrente
                        String jsonText = Files.readString(filePath);

                        JSONObject jsonEnemy = new JSONObject(jsonText);
                        EntityRecord record = parseEnemySettings(jsonEnemy);

                        // Usiamo la EntityKey estratta direttamente dal file stesso!
                        if (record.entityKey() != null && !record.entityKey().isEmpty()) {
                            String finalKey = record.entityKey().toLowerCase().trim();
                            cache.put(finalKey, record);
                            loadedCount++;
                        }
                    }
                }

                System.out.println("[EntityFactory] Scansione automatica completata. Caricati " + loadedCount + " nemici.");

            } catch (Exception ex) {
                System.err.println("[EntityFactory] Errore critico durante il caricamento dinamico dei nemici: " + ex.getMessage());
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

        System.err.println("[PERFORMANCE WARNING] Cache-miss per '" + entityKey + "'! Ricarico i file JSON.");
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

        // Threat
        //int threat = json.optInt("threatLevel", 1);
        // builder.threatLevel(threat);

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

        return builder.build();
    }

    private static EntityRecord.BrainRecord parseBrain(JSONObject aiJson) {
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

            String type = obj.optString("type");
            double combatRange = obj.optDouble("combatRange", 10.0);
            double cooldownSec = obj.optDouble("cooldownSec", 0.8);
            String bulletType = obj.optString("bulletType", "ENEMY_BULLET");
            boolean aimAtTarget = obj.optBoolean("aimAtTarget", true);
            double nerfPrediction = obj.optDouble("nerfPrediction", 0.0);

            List<EntityRecord.PatternRecord> patterns = parsePatternList(obj.optJSONArray("patterns"));
            EntityRecord.PatternRecord pattern = parsePattern(obj.optJSONObject("pattern"));

            double healAmount = obj.optDouble("healAmount", 0.0);
            String summonEntityKey = obj.optString("summonEntityKey", null);
            int summonCount = obj.optInt("summonCount", 1);
            double summonRadiusPx = obj.optDouble("summonRadiusPx", 100.0);
            double meleeDamage = obj.optDouble("meleeDamage", 0.0);
            int attackStateIndex = obj.optInt("attackStateIndex", 4);

            double dashCooldownMS = obj.optDouble("dashCooldownMS", 0.0);
            double dashDurationMS = obj.optDouble("dashDurationMS", 0.0);
            double dashPrediction = obj.optDouble("dashPrediction", 0.0);
            double dashAvoidRange = obj.optDouble("dashAvoidRange", 0.0);
            double dashImpulse = obj.optDouble("dashImpulse", 0.0);
            double plungeCooldownMS = obj.optDouble("plungeCooldownMS", 0.0);

            EntityRecord.AbilityRecord ac = new EntityRecord.AbilityRecord(
                    type, combatRange, cooldownSec, bulletType, aimAtTarget, nerfPrediction,
                    patterns, pattern, healAmount, summonEntityKey, summonCount, summonRadiusPx,
                    meleeDamage, attackStateIndex,
                    dashCooldownMS, dashDurationMS, dashPrediction, dashAvoidRange, dashImpulse,
                    plungeCooldownMS
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
                parsePattern(obj.optJSONObject("pattern")),
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