package uni.gaben.iscat.universe.entity;

import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EntityFactory {

    private EntityFactory() {}

    private static final Map<String, EntitySettings> cache = new ConcurrentHashMap<>();
    private static final String JSON_PATH = "/uni/gaben/iscat/json/enemies.json";

    public static EntityModel spawn(
            String entityKey,
            double x, double y,
            UniverseModel universe,
            UniverseController controller) {

        if (entityKey == null) return null;
        String normalizedKey = entityKey.toLowerCase().trim();

        EntitySettings settings = loadSettings(normalizedKey);
        if (settings == null) {
            System.err.println("[EntityFactory] Impossibile spawnare: EntityKey sconosciuta '" + normalizedKey + "'");
            return null;
        }

        EntityModel model = new EntityModel(x, y, settings);
        EntityBrain brain = new EntityBrain(model);

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

                // Legge tutto il file JSON in una stringa
                String jsonText = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                        .lines().collect(Collectors.joining("\n"));

                JSONObject root = new JSONObject(jsonText);
                JSONArray enemies = root.getJSONArray("enemies");

                // Svuotiamo la vecchia cache prima del ricaricamento
                cache.clear();

                for (int i = 0; i < enemies.length(); i++) {
                    JSONObject jsonEnemy = enemies.getJSONObject(i);
                    EntitySettings settings = parseEnemySettings(jsonEnemy);

                    if (settings.entityKey != null) {
                        cache.put(settings.entityKey.toLowerCase().trim(), settings);
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


    private static EntitySettings loadSettings(String entityKey) {
        EntitySettings cached = cache.get(entityKey);
        if (cached != null) {
            return cached;
        }

        System.err.println("[PERFORMANCE WARNING] Cache-miss per '" + entityKey + "'! Ricarico il file JSON in modo sincrono.");

        // Esegue il precaricamento sincrono di blocco per recuperare il dato mancante
        try {
            preloadAllAsync().get(); // Attende il completamento della transazione
            return cache.get(entityKey);
        } catch (Exception e) {
            System.err.println("[EntityFactory] Impossibile recuperare l'entità dopo il cache-miss.");
            return null;
        }
    }

    /**
     * Mappa i campi PascalCase del JSON nei campi camelCase dell'oggetto Java
     */
    private static EntitySettings parseEnemySettings(JSONObject json) {
        EntitySettings s = new EntitySettings();

        s.entityKey = json.optString("EntityKey", "");
        s.name = json.optString("Name", "");
        s.description = json.optString("Description", "");

        // Mappatura SpriteName -> spritePath (aggiungendo l'estensione se necessario)
        String spriteName = json.optString("SpriteName", "").trim();

        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }

        s.spritePath = "/uni/gaben/iscat/sprites/enemies/" + spriteName + ".png";

        s.frameW = json.optInt("FrameW", 32);
        s.frameH = json.optInt("FrameH", 32);
        s.shapeType = PhysicalEntitySettings.ShapeType.valueOf(json.optString("ShapeType", "CIRCLE"));
        s.scale = json.optDouble("Scale", 1.0);

        s.initLife = json.optInt("InitLife", 100);
        s.linearDamping = json.optDouble("LinearDamping", 2.0);
        s.mass = json.optDouble("mass", 1.0);
        s.maxVelocity = json.optDouble("MaxVelocity", 10.0);
        s.maxForce = json.optDouble("MaxForce", 30.0);
        s.maxAngularVelocity = json.optDouble("MaxAngularVelocity", 5.0);
        s.xpReward = json.optInt("XPReward", 10);

        s.detectionRange = json.optDouble("DetectionRange", 15.0);
        s.combatRange = json.optDouble("CombatRange", 10.0);
        s.preferredRange = json.optDouble("PreferredRange", 10.0);
        s.actionCooldownMS = json.optDouble("actionCooldownS", 800.0);

        s.isBoss = json.optBoolean("IsBoss", false);
        s.hasEntranceAnimation = json.optBoolean("HasEntranceAnimation", false);

        if (json.has("AnimationFrames")) {
            JSONArray framesArray = json.getJSONArray("AnimationFrames");
            s.animationFrames = new int[framesArray.length()];
            for (int i = 0; i < framesArray.length(); i++) {
                s.animationFrames[i] = framesArray.optInt(i, 1); // Fallback a 1 frame se c'è un errore
            }
        }

        // Parsing dell'oggetto audio
        if (json.has("audio")) {
            JSONObject audioJson = json.getJSONObject("audio");
            s.audio.attack = jsonArrayToList(audioJson.optJSONArray("attack"));
            s.audio.idle = jsonArrayToList(audioJson.optJSONArray("idle"));
            s.audio.hurt = jsonArrayToList(audioJson.optJSONArray("hurt"));
            s.audio.death = jsonArrayToList(audioJson.optJSONArray("death"));
            s.audio.spawn = jsonArrayToList(audioJson.optJSONArray("spawn"));
        }

        return s;
    }

    private static java.util.List<String> jsonArrayToList(JSONArray array) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        }
        return list;
    }

    public static Map<String, EntitySettings> getCache() {
        return cache;
    }
}