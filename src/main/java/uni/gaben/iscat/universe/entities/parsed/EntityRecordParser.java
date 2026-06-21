package uni.gaben.iscat.universe.entities.parsed;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.EntityFilters;
import uni.gaben.iscat.universe.entities.ThreatLevel;
import uni.gaben.iscat.universe.entities.brain.abilities.*;
import uni.gaben.iscat.universe.entities.brain.abilities.shoot.RandomizedShootAbility;
import uni.gaben.iscat.universe.entities.brain.abilities.shoot.ShootAbility;
import uni.gaben.iscat.universe.entities.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringModifier;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.shooters.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Classe di utilità responsabile del parsing dei file di configurazione JSON
 * per la generazione di oggetti {@link EntityRecord}.
 * Centralizza la conversione dei parametri fisici, visivi, audio e delle logiche
 * di intelligenza artificiale (AI) di gioco.
 * * <p>Le chiavi del JSON vengono automaticamente normalizzate in lowercase per garantire
 * l'insensibilità al case-sensitive.</p>
 */
public final class EntityRecordParser {

    private EntityRecordParser() {
    }

    /**
     * Analizza un oggetto JSON completo, normalizza le chiavi in minuscolo e restituisce l'immutabile {@link EntityRecord}.
     */
    public static EntityRecord parse(JSONObject json) {
        // Normalizzazione globale del JSON in lowercase
        JSONObject lowerJson = convertKeysToLowerCase(json);

        EntityRecordBuilder builder = new EntityRecordBuilder();
        parseIdentity(lowerJson, builder);
        parseBestiaryOrder(lowerJson, builder);
        parseThreatLevel(lowerJson, builder);
        parseSpritePath(lowerJson, builder);
        parseVisuals(lowerJson, builder);
        parsePhysics(lowerJson, builder);
        parseBehavioural(lowerJson, builder);
        parseBossFlags(lowerJson, builder);
        parseAnimationFrames(lowerJson, builder);
        parseAudioProfiles(lowerJson, builder);
        parseAIConfig(lowerJson, builder);
        parsePlayerConfig(lowerJson, builder);
        return builder.build();
    }

    /**
     * Helper ricorsivo che trasforma tutte le chiavi di un JSONObject (e relativi sotto-oggetti/array) in lowercase.
     */
    public static JSONObject convertKeysToLowerCase(JSONObject obj) {
        if (obj == null) return null;
        JSONObject normalized = new JSONObject();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = obj.get(key);

            if (value instanceof JSONObject) {
                value = convertKeysToLowerCase((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = convertKeysToLowerCase((JSONArray) value);
            }

            normalized.put(key.toLowerCase(), value);
        }
        return normalized;
    }

    public static JSONArray convertKeysToLowerCase(JSONArray arr) {
        if (arr == null) return null;
        JSONArray normalized = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            Object value = arr.get(i);
            if (value instanceof JSONObject) {
                normalized.put(convertKeysToLowerCase((JSONObject) value));
            } else if (value instanceof JSONArray) {
                normalized.put(convertKeysToLowerCase((JSONArray) value));
            } else {
                normalized.put(value);
            }
        }
        return normalized;
    }

    private static void parseIdentity(JSONObject json, EntityRecordBuilder builder) {
        builder.entityKey(json.optString("entitykey", ""))
                .name(json.optString("name", ""))
                .description(json.optString("description", ""));
    }

    private static void parseBestiaryOrder(JSONObject json, EntityRecordBuilder builder) {
        builder.bestiaryOrder(json.optInt("bestiaryorder", 0));
    }

    private static void parseThreatLevel(JSONObject json, EntityRecordBuilder builder) {
        String threatStr = json.optString("threatlevel", "NONE").toUpperCase().trim();
        try {
            builder.threatLevel(ThreatLevel.valueOf(threatStr));
        } catch (IllegalArgumentException e) {
            System.err.println("[EntityRecordParser] ThreatLevel invalido '" + threatStr +
                    "' per " + json.optString("entitykey") + ". Uso NONE.");
            builder.threatLevel(ThreatLevel.NONE);
        }
    }

    private static void parseSpritePath(JSONObject json, EntityRecordBuilder builder) {
        String spriteName = json.optString("spritename", "").trim();
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }
        String spriteFolder = json.has("player") ? "players" : "enemies";
        builder.spritePath("/uni/gaben/iscat/sprites/" + spriteFolder + "/" + spriteName + ".png");
    }

    private static void parseVisuals(JSONObject json, EntityRecordBuilder builder) {
        builder.frameW(json.optInt("framew", 32))
                .frameH(json.optInt("frameh", 32))
                .shapeType(EntityRecord.ShapeType.valueOf(json.optString("shapetype", "CIRCLE").toUpperCase()))
                .scale(json.optDouble("scale", 1.0))
                .visualAngularOffset(json.optDouble("angularoffsetdeg", 0.0));
    }

    private static void parsePhysics(JSONObject json, EntityRecordBuilder builder) {
        builder .initLife(json.optDouble("initlife", 100.0))
                .linearDamping(json.optDouble("lineardamping", 2.0))
                .mass(json.optDouble("mass", 1.0))
                .maxVelocity(json.optDouble("maxvelocity", 10.0))
                .maxForce(json.optDouble("maxforce", 30.0))
                .maxAngularVelocity(json.optDouble("maxangularvelocity", 5.0))
                .xpReward(json.optInt("xpreward", 10))
                .dannoProiettile(json.optDouble("dannoproiettile", 4.0));
    }

    private static void parseBehavioural(JSONObject json, EntityRecordBuilder builder) {
        builder.detectionRange(json.optDouble("detectionrange", 15.0))
                .combatRange(json.optDouble("combatrange", 10.0))
                .preferredRange(json.optDouble("preferredrange", 10.0))
                .actionCooldownSec(json.optDouble("actioncooldowns", 1.0));
    }

    private static void parseBossFlags(JSONObject json, EntityRecordBuilder builder) {
        builder.isBoss(json.optBoolean("isboss", false))
                .hasEntranceAnimation(json.optBoolean("hasentranceanimation", false));
    }

    private static void parseAnimationFrames(JSONObject json, EntityRecordBuilder builder) {
        List<EntityRecord.AnimationRecord> animationList = new ArrayList<>();

        if (json.has("animations")) {
            JSONArray animsArray = json.getJSONArray("animations");
            for (int i = 0; i < animsArray.length(); i++) {
                JSONObject animJson = animsArray.getJSONObject(i);
                String type = animJson.optString("type", "IDLE").toUpperCase();

                int row = animJson.optInt("row", i);
                int frames = animJson.optInt("frames", 1);
                double durationSec = animJson.optDouble("durationsec", 0.0);

                animationList.add(new EntityRecord.AnimationRecord(type, row, frames, durationSec));
            }
        }
        builder.animations(animationList);
    }

    private static void parseAudioProfiles(JSONObject json, EntityRecordBuilder builder) {
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
    }

    private static void parseAIConfig(JSONObject json, EntityRecordBuilder builder) {
        if (json.has("ai")) {
            builder.brain(parseBrain(json.getJSONObject("ai")));
        }
    }

    private static void parsePlayerConfig(JSONObject json, EntityRecordBuilder builder) {
        if (json.has("player")) {
            builder.player(parsePlayerRecord(json.getJSONObject("player")));
        }
    }

    private static EntityRecord.PlayerRecord parsePlayerRecord(JSONObject json) {
        return new EntityRecord.PlayerRecord(
                json.optDouble("dashimpulse", 35.0),
                json.optDouble("dashdurationsec", 0.666),
                json.optDouble("dashcooldownsec", 0.8),
                json.optDouble("stundurationsec", 1.0),
                json.optDouble("basecooldownsec", 0.3),
                json.optDouble("meleedamage", 0),
                json.optDouble("meleecooldownsec", 0.5),
                json.optDouble("basespeed", 10.0),
                json.optDouble("basethrustforce", 30.0),
                json.optDouble("basexpneeded", 100.0),
                json.optDouble("dannoproiettile", 50.0),
                parseLevelAbilities(json.optJSONArray("levelabilities"))
        );
    }

    private static List<EntityRecord.LevelAbility> parseLevelAbilities(JSONArray arr) {
        List<EntityRecord.LevelAbility> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            int minLevel = obj.optInt("minlevel", 1);
            double cooldownSec = obj.optDouble("cooldownsec", 0.3);
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
        if (obj == null) return new EntityRecord.SteeringRecord(SteeringGoalIndex.IDLE, 2.5, 2.5, 10.0, 5.0);
        return new EntityRecord.SteeringRecord(
                SteeringGoalIndex.fromJson(obj.optString("type", SteeringGoalIndex.IDLE.jsonKey)),
                obj.optDouble("maxpredictiontime", 2.5),
                obj.optDouble("mindistance", 2.5),
                obj.optDouble("maxdistance", 10.0),
                obj.optDouble("safetydistance", 5.0)
        );
    }

    private static EntityRecord.RotationRecord parseRotation(JSONObject obj) {
        if (obj == null) return new EntityRecord.RotationRecord(RotationGoalIndex.IDLE, 0.0, 8, 0.1, "player");
        return new EntityRecord.RotationRecord(
                RotationGoalIndex.fromJson(obj.optString("type", RotationGoalIndex.IDLE.jsonKey)),
                obj.optDouble("spinspeedradpersec", 0.0),
                obj.optInt("spinsteps", 8),
                obj.optDouble("steppausesec", 0.1),
                obj.optString("target", "player")
        );
    }

    private static List<EntityRecord.AbilityRecord> parseAbilities(JSONArray arr) {
        List<EntityRecord.AbilityRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            AbilityIndex type = AbilityIndex.fromJson(obj.optString("type"));
            double combatRange = obj.optDouble("combatrange", 10.0);
            double cooldownSec = obj.optDouble("cooldownsec", 0.8);
            String bulletType = obj.optString("bullettype", "ENEMY_BULLET");
            boolean aimAtTarget = obj.optBoolean("aimattarget", true);
            double nerfPrediction = obj.optDouble("nerfprediction", 0.0);

            List<EntityRecord.PatternRecord> patterns = parsePatternList(obj.optJSONArray("patterns"));
            EntityRecord.PatternRecord pattern = parsePattern(obj.optJSONObject("pattern"));

            double healAmount = obj.optDouble("healamount", 0.0);
            String summonEntityKey = obj.optString("summonentitykey", null);
            int summonCount = obj.optInt("summoncount", 1);
            double summonRadiusPx = obj.optDouble("summonradiuspx", 100.0);
            double meleeDamage = obj.optDouble("meleedamage", 0.0);
            int attackStateIndex = obj.optInt("attackstateindex", 4);

            double dashCooldownMS = obj.optDouble("dashcooldownms", 0.0);
            double dashDurationMS = obj.optDouble("dashdurationms", 0.0);
            double dashPrediction = obj.optDouble("dashprediction", 0.0);
            double dashAvoidRange = obj.optDouble("dashavoidrange", 0.0);
            double dashImpulse = obj.optDouble("dashimpulse", 0.0);
            double plungeCooldownMS = obj.optDouble("plungecooldownms", 0.0);

            list.add(new EntityRecord.AbilityRecord(
                    type, combatRange, cooldownSec, bulletType, aimAtTarget, nerfPrediction,
                    patterns, pattern, healAmount, summonEntityKey, summonCount, summonRadiusPx,
                    meleeDamage, attackStateIndex,
                    dashCooldownMS, dashDurationMS, dashPrediction, dashAvoidRange, dashImpulse,
                    plungeCooldownMS
            ));
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

        double angleOrSpacing = obj.has("spacingpx") ? obj.optDouble("spacingpx") : obj.optDouble("anglestepdeg", 30.0);
        double intervalOrStepBack = obj.has("stepbackpx") ? obj.optDouble("stepbackpx") : obj.optDouble("intervalsec", 0.15);

        return new EntityRecord.PatternRecord(
                PatternIndex.fromJson(obj.optString("type")),
                obj.optInt("count", 1),
                angleOrSpacing,
                intervalOrStepBack,
                obj.optInt("repeats", 1),
                parsePattern(obj.optJSONObject("pattern")),
                obj.optString("summonedentitykey"),
                obj.optDouble("summonradiuspx", 100.0),
                obj.optString("figuretype", "CIRCLE")
        );
    }

    private static List<EntityRecord.ModifierRecord> parseModifiers(JSONArray arr) {
        List<EntityRecord.ModifierRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new EntityRecord.ModifierRecord(
                    ModifierIndex.fromJson(obj.optString("type")),
                    obj.optDouble("radius", 2.0),
                    obj.optDouble("weight", 1.0),
                    obj.optDouble("maxpredictiontime", 1.0),
                    obj.optDouble("avoidradius", 2.0)
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

    public static SteeringGoal createSteeringGoal(EntityRecord.SteeringRecord steeringData) {
        Target target = Target.ofPlayer();
        return switch (steeringData.type()) {
            case PURSUIT -> SteeringGoal.pursuit(target, steeringData.maxPredictionTime());
            case EVADE -> SteeringGoal.evade(target, steeringData.maxPredictionTime());
            case PURSUIT_WITH_RANGE ->
                    SteeringGoal.pursuitWithRange(target, steeringData.maxPredictionTime(), steeringData.minDistance(), steeringData.maxDistance());
            case EVADE_WITH_RANGE -> SteeringGoal.evadeWithRange(target, steeringData.maxPredictionTime(), steeringData.safetyDistance());
            default -> SteeringGoal.idle();
        };
    }

    public static SteeringModifier createModifier(EntityRecord.ModifierRecord mc, EntityModel entity) {
        if (mc.type() == null) return null;
        DoubleProperty weight = new SimpleDoubleProperty(mc.weight());
        Target neighbors = Target.neighboursCached(entity, mc.radius(), EntityFilters.isNot(entity));
        Target everythingButEnemyProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof PlayerModel || (entityModel instanceof ProjectileModel pm&&pm.getType()!=ProjectileType.PLAYER_BULLET)));
        Target everythingButProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof PlayerModel || entityModel instanceof AbstractPhysicalProjectileModel));
        return switch (mc.type()) {
            case SEPARATION -> SteeringModifier.separation(everythingButEnemyProjectiles, mc.radius(), weight);
            case ALIGNMENT -> SteeringModifier.alignment(everythingButProjectiles, weight);
            case COHESION -> SteeringModifier.cohesion(everythingButProjectiles, weight);
            case COLLISION_AVOIDANCE ->
                    SteeringModifier.collisionAvoidance(everythingButEnemyProjectiles, mc.maxPredictionTime(), mc.avoidRadius(), weight);
        };
    }

    public static RotationGoal createRotationGoal(EntityRecord.RotationRecord rotation) {
        if (rotation == null) return RotationGoal.idle();
        return switch (rotation.type()) {
            case STILL -> RotationGoal.still();
            case MOVEMENT -> RotationGoal.movement();
            case TARGET -> RotationGoal.target(Target.ofPlayer());
            case CONTINUES_SPIN -> RotationGoal.continuesSpin(rotation.spinSpeedRadPerSec());
            case INTERVAL_SPIN -> RotationGoal.intervalSpin(rotation.spinSteps(), rotation.stepPauseSec(), rotation.spinSpeedRadPerSec());
            case LOCKED -> RotationGoal.fixedAngle(0.0);
            default -> RotationGoal.idle();
        };
    }

    public static Ability createAbility(EntityRecord.AbilityRecord ac, EntityModel entity) {
        if (ac.type() == null) return null;
        Target target = Target.ofPlayer();

        double dannoEntita = entity.getEntityRecord().dannoProiettile();

        return switch (ac.type()) {
            case SHOOT -> {
                Pattern shooter = createPattern(ac.pattern());
                yield new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), shooter,
                        target, ac.aimAtTarget(), ac.nerfPrediction(),
                        dannoEntita, ac.attackStateIndex());
            }
            case RANDOMIZED_SHOOT -> {
                List<Pattern> patterns = new ArrayList<>();
                for (EntityRecord.PatternRecord pc : ac.patterns()) patterns.add(createPattern(pc));
                yield RandomizedShootAbility.targetingPlayer(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), ac.aimAtTarget(),
                        ac.nerfPrediction(), dannoEntita, ac.attackStateIndex(), patterns.toArray(new Pattern[0]));
            }
            case HEAL -> new HealAbility(ac.cooldownSec(), ac.combatRange(), ac.healAmount());
            case SUMMON -> new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                    ProjectileType.valueOf(ac.bulletType()), new SummonPattern(ac.summonCount(), ac.summonEntityKey(), ac.summonRadiusPx()),
                    target, ac.aimAtTarget(), ac.nerfPrediction(),
                    dannoEntita, ac.attackStateIndex());
            case MELEE -> new MeleeAbility<>(ac.type().jsonKey, entity, ac.cooldownSec(), ac.meleeDamage(), EntityFilters.IS_PLAYER);
            case KAMIKAZE -> new KamikazeAbility(entity, ac.meleeDamage(), EntityFilters.IS_PLAYER);
            case DASH -> new DodgeDashAbility(entity, ac.dashCooldownMS()/1000, ac.dashDurationMS()/1000, ac.dashPrediction(), ac.dashAvoidRange(), ac.dashImpulse(),
                    Target.neighboursCached(entity, ac.dashAvoidRange(), body -> body instanceof ProjectileModel pm && pm.getType() == ProjectileType.PLAYER_BULLET));
            case PLUNGE -> new PlungeAbility(
                    entity,
                    ac.plungeCooldownMS() / 1000,
                    ac.dashDurationMS() / 1000,
                    ac.dashPrediction(),
                    ac.dashImpulse(),
                    Target.ofPlayer()
            );
        };
    }

    public static Pattern createPattern(EntityRecord.PatternRecord pc) {
        if (pc == null) return new SingleShotPattern();
        return switch (pc.type()) {
            case SINGLE_SHOT ->     new SingleShotPattern();
            case SPREAD ->          new SpreadPattern(pc.count(), pc.angleStepDeg());
            case MULTI_DIRECTION -> new MultiDirectionPattern(pc.count(), Math.toRadians(pc.angleStepDeg()), createPattern(pc.innerPattern()));
            case RING ->            new RingPattern(pc.count());
            case REPEATER ->        new RepeaterPattern(pc.repeats(), pc.intervalSec(), createPattern(pc.innerPattern()));
            case PARALLEL_LINE ->   new ParallelLinePattern(pc.count(), pc.angleStepDeg());
            case SUMMON ->          new SummonPattern(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case FIGURE ->          new FigurePattern(pc.count(), FigurePattern.FigureType.valueOf(pc.figureType()));
            case VARROW ->          new VArrowPattern(pc.count(), pc.angleStepDeg(), pc.intervalSec());
        };
    }
}