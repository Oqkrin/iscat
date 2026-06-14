package uni.gaben.iscat.universe.entity;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.ModifierType;
import uni.gaben.iscat.universe.entity.brain.RotationGoalType;
import uni.gaben.iscat.universe.entity.brain.SteeringGoalType;
import uni.gaben.iscat.universe.entity.brain.abilities.AbilityType;
import uni.gaben.iscat.universe.entity.shooters.PatternType;

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
    private static final String PLAYERS_DIR = "/uni/gaben/iscat/json/players/";

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

            // Scansioniamo ed estraiamo i file da entrambe le cartelle
            int enemiesLoaded = scanAndLoadDirectory(ENEMIES_DIR);
            int playersLoaded = scanAndLoadDirectory(PLAYERS_DIR);

            System.out.println("[EntityFactory] Cache JSON pronta.");
            System.out.println(" -> Nemici caricati: " + enemiesLoaded);
            System.out.println(" -> Skin Giocatore caricate: " + playersLoaded);
        });
    }

    /**
     * Metodo helper generico che scansiona una cartella di risorse (sia IDE che JAR)
     * e popola la cache usando le EntityKey interne ai file JSON.
     */
    private static int scanAndLoadDirectory(String dirPath) {
        int loadedCount = 0;
        try {
            URL dirUrl = EntityFactory.class.getResource(dirPath);
            if (dirUrl == null) {
                System.err.println("[EntityFactory] Cartella di risorse non trovata: " + dirPath);
                return 0;
            }

            URI uri = dirUrl.toURI();
            Path myPath;

            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem;
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException e) {
                    fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                }
                myPath = fileSystem.getPath(dirPath);
            } else {
                myPath = Paths.get(uri);
            }

            try (Stream<Path> walk = Files.walk(myPath, 1)) {
                List<Path> jsonFiles = walk
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .collect(Collectors.toList());

                for (Path filePath : jsonFiles) {
                    String jsonText = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                    JSONObject json = new JSONObject(jsonText);

                    EntityRecord record = parseEnemySettings(json);

                    if (record.entityKey() != null && !record.entityKey().isEmpty()) {
                        String finalKey = record.entityKey().toLowerCase().trim();
                        cache.put(finalKey, record);
                        loadedCount++;
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("[EntityFactory] Errore critico nella scansione di " + dirPath + ": " + ex.getMessage());
            ex.printStackTrace();
        }
        return loadedCount;
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

        // Gestione BestiaryOrder con valore di default a 0
        builder.bestiaryOrder(json.optInt("BestiaryOrder", 0));

        // Legge il ThreatLevel dal JSON. Se manca o è vuoto, usa "NONE" come fallback di sicurezza per i Player
        String threatStr = json.optString("ThreatLevel", "NONE").toUpperCase().trim();
        try {
            builder.threatLevel(ThreatLevel.valueOf(threatStr));
        } catch (IllegalArgumentException e) {
            System.err.println("[EntityFactory] ThreatLevel invalido '" + threatStr + "' per l'entità " + json.optString("EntityKey") + ". Imposto NONE.");
            builder.threatLevel(ThreatLevel.NONE);
        }

        // Sprite path (Dinamico: cambia cartella in base al tipo di entità)
        String spriteName = json.optString("SpriteName", "").trim();
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }

        // Se contiene il blocco "player", la skin si trova in /sprites/players/, altrimenti in /sprites/enemies/
        String spriteFolder = json.has("player") ? "players" : "enemies";
        builder.spritePath("/uni/gaben/iscat/sprites/" + spriteFolder + "/" + spriteName + ".png");

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

        // Behavioural (Range e Cooldown generali)
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

        // Audio Profiles
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

        // AI configuration
        if (json.has("ai")) {
            builder.brain(parseBrain(json.getJSONObject("ai")));
        }

        // Configurazione specifica del Player
        if (json.has("player")) {
            builder.player(parsePlayerRecord(json.getJSONObject("player")));
        }

        return builder.build();
    }

    private static EntityRecord.PlayerRecord parsePlayerRecord(JSONObject json) {
        return new EntityRecord.PlayerRecord(
                json.optDouble("dashImpulse", 35.0),
                json.optDouble("dashDurationSec", 0.666),
                json.optDouble("dashCooldownSec", 0.8),
                json.optDouble("stunDurationSec", 1.0),
                json.optDouble("baseCooldownSec", 0.3),
                parseLevelAbilities(json.optJSONArray("levelAbilities"))
        );
    }

    private static List<EntityRecord.LevelAbility> parseLevelAbilities(JSONArray arr) {
        List<EntityRecord.LevelAbility> list = new ArrayList<>();
        if (arr == null) return list;

        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            int minLevel = obj.optInt("minLevel", 1);
            double cooldownSec = obj.optDouble("cooldownSec", 0.3);

            EntityRecord.PatternRecord pattern = parsePattern(obj.optJSONObject("pattern"));

            list.add(new EntityRecord.LevelAbility(minLevel, pattern, cooldownSec));
        }

        list.sort((a, b) -> Integer.compare(b.minLevel(), a.minLevel()));
        return list;
    }

    private static EntityRecord.BrainRecord parseBrain(JSONObject aiJson) {
        EntityRecord.SteeringRecord steering = parseSteering(aiJson.optJSONObject("steering"));
        EntityRecord.RotationRecord rotation = parseRotation(aiJson.optJSONObject("rotation"));
        List<EntityRecord.AbilityRecord> abilities = parseAbilities(aiJson.optJSONArray("abilities"));
        List<EntityRecord.ModifierRecord> modifiers = parseModifiers(aiJson.optJSONArray("modifiers"));
        return new EntityRecord.BrainRecord(steering, rotation, abilities, modifiers);
    }

    private static EntityRecord.SteeringRecord parseSteering(JSONObject obj) {
        if (obj == null) return new EntityRecord.SteeringRecord(SteeringGoalType.IDLE, 2.5, 2.5, 10.0, 5.0);
        return new EntityRecord.SteeringRecord(
                SteeringGoalType.fromJson(obj.optString("type", SteeringGoalType.IDLE.jsonKey)),
                obj.optDouble("maxPredictionTime", 2.5),
                obj.optDouble("minDistance", 2.5),
                obj.optDouble("maxDistance", 10.0),
                obj.optDouble("safetyDistance", 5.0)
        );
    }

    private static EntityRecord.RotationRecord parseRotation(JSONObject obj) {
        if (obj == null) return new EntityRecord.RotationRecord(RotationGoalType.IDLE, 0.0, 8, 0.1, "player");
        return new EntityRecord.RotationRecord(
                RotationGoalType.fromJson(obj.optString("type", RotationGoalType.IDLE.jsonKey)),
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

            AbilityType type = AbilityType.fromJson(obj.optString("type"));
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
                PatternType.fromJson(obj.optString("type")),
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
                    ModifierType.fromJson(obj.optString("type")),
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