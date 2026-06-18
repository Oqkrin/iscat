package uni.gaben.iscat.universe.entities;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.json.JSONArray;
import org.json.JSONObject;
import uni.gaben.iscat.universe.entities.brain.abilities.*;
import uni.gaben.iscat.universe.entities.brain.abilities.shoot.RandomizedShootAbility;
import uni.gaben.iscat.universe.entities.brain.abilities.shoot.ShootAbility;
import uni.gaben.iscat.universe.entities.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringModifier;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.shooters.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe di utilità responsabile del parsing dei file di configurazione JSON
 * per la generazione di oggetti {@link EntityRecord}.
 * Centralizza la conversione dei parametri fisici, visivi, audio e delle logiche
 * di intelligenza artificiale (AI) di gioco.
 */
public final class EntityRecordParser {

    /**
     * Costruttore privato per impedire l'istanza della classe di utilità.
     */
    private EntityRecordParser() {
    }

    /**
     * Analizza un oggetto JSON completo e restituisce l'immutabile {@link EntityRecord} risultante.
     *
     * @param json L'oggetto JSON contenente i dati dell'entità.
     * @return     Un record di entità completamente configurato.
     */
    public static EntityRecord parse(JSONObject json) {
        EntityRecordBuilder builder = new EntityRecordBuilder();
        parseIdentity(json, builder);
        parseBestiaryOrder(json, builder);
        parseThreatLevel(json, builder);
        parseSpritePath(json, builder);
        parseVisuals(json, builder);
        parsePhysics(json, builder);
        parseBehavioural(json, builder);
        parseBossFlags(json, builder);
        parseAnimationFrames(json, builder);
        parseAudioProfiles(json, builder);
        parseAIConfig(json, builder);
        parsePlayerConfig(json, builder);
        return builder.build();
    }

    /**
     * Estrae i dati identificativi di base dell'entità.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseIdentity(JSONObject json, EntityRecordBuilder builder) {
        builder.entityKey(json.optString("EntityKey", ""))
                .name(json.optString("Name", ""))
                .description(json.optString("Description", ""));
    }

    /**
     * Estrae l'ordinamento dell'entità all'interno del Bestiario.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseBestiaryOrder(JSONObject json, EntityRecordBuilder builder) {
        builder.bestiaryOrder(json.optInt("BestiaryOrder", 0));
    }

    /**
     * Determina il livello di minaccia dell'entità convertendo la stringa JSON nell'apposita Enum.
     * In caso di errore o valore non mappato, applica il valore di fallback {@link ThreatLevel#NONE}.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseThreatLevel(JSONObject json, EntityRecordBuilder builder) {
        String threatStr = json.optString("ThreatLevel", "NONE").toUpperCase().trim();
        try {
            builder.threatLevel(ThreatLevel.valueOf(threatStr));
        } catch (IllegalArgumentException e) {
            System.err.println("[EntityRecordParser] ThreatLevel invalido '" + threatStr +
                    "' per " + json.optString("EntityKey") + ". Uso NONE.");
            builder.threatLevel(ThreatLevel.NONE);
        }
    }

    /**
     * Ricava il percorso relativo della texture associata all'entità, distinguendo tra giocatori ed elementi ostili.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseSpritePath(JSONObject json, EntityRecordBuilder builder) {
        String spriteName = json.optString("SpriteName", "").trim();
        if (spriteName.toLowerCase().endsWith(".png")) {
            spriteName = spriteName.substring(0, spriteName.length() - 4);
        }
        String spriteFolder = json.has("player") ? "players" : "enemies";
        builder.spritePath("/uni/gaben/iscat/sprites/" + spriteFolder + "/" + spriteName + ".png");
    }

    /**
     * Estrae le configurazioni geometriche e visuali dell'entità, inclusi l'offset angolare di rendering.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseVisuals(JSONObject json, EntityRecordBuilder builder) {
        builder.frameW(json.optInt("FrameW", 32))
                .frameH(json.optInt("FrameH", 32))
                .shapeType(EntityRecord.ShapeType.valueOf(json.optString("ShapeType", "CIRCLE")))
                .scale(json.optDouble("Scale", 1.0))
                .visualAngularOffset(json.optDouble("AngularOffsetDeg", 0.0));
    }

    /**
     * Estrae i parametri della simulazione fisica del corpo rigido e i valori di ricompensa.
     * Corretto il doppio inserimento di visualAngularOffset mappandolo correttamente sulla chiave del JSON.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parsePhysics(JSONObject json, EntityRecordBuilder builder) {
        builder .initLife(json.optDouble("InitLife", 100.0))
                .linearDamping(json.optDouble("LinearDamping", 2.0))
                .mass(json.optDouble("mass", 1.0))
                .maxVelocity(json.optDouble("MaxVelocity", 10.0))
                .maxForce(json.optDouble("MaxForce", 30.0))
                .maxAngularVelocity(json.optDouble("MaxAngularVelocity", 5.0))
                .xpReward(json.optInt("XPReward", 10))
                .dannoProiettile(json.optDouble("DannoProiettile", 4.0));
    }

    /**
     * Configura le distanze di ingaggio dell'entità e i tempi di ricarica delle abilità principali.
     * Rimossa la divisione errata per 1000.0 poiché il cooldown nel JSON è già espresso in secondi.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseBehavioural(JSONObject json, EntityRecordBuilder builder) {
        builder.detectionRange(json.optDouble("DetectionRange", 15.0))
                .combatRange(json.optDouble("CombatRange", 10.0))
                .preferredRange(json.optDouble("PreferredRange", 10.0))
                .actionCooldownSec(json.optDouble("actionCooldownS", 1.0));
    }

    /**
     * Analizza e setta i flag booleani relativi allo stato di boss dell'entità.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseBossFlags(JSONObject json, EntityRecordBuilder builder) {
        builder.isBoss(json.optBoolean("IsBoss", false))
                .hasEntranceAnimation(json.optBoolean("HasEntranceAnimation", false));
    }

    /**
     * Estrae l'array numerico contenente la mappatura dei frame di animazione dell'entità.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseAnimationFrames(JSONObject json, EntityRecordBuilder builder) {
        if (json.has("AnimationFrames")) {
            JSONArray framesArray = json.getJSONArray("AnimationFrames");
            int[] frames = new int[framesArray.length()];
            for (int i = 0; i < framesArray.length(); i++) {
                frames[i] = framesArray.optInt(i, 1);
            }
            builder.animationFrames(frames);
        }
    }

    /**
     * Associa le liste di file audio ai rispettivi trigger ed eventi dell'entità viva.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
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

    /**
     * Inizializza la configurazione decisionale del cervello dell'AI se presente nel sotto-nodo JSON dedicato.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parseAIConfig(JSONObject json, EntityRecordBuilder builder) {
        if (json.has("ai")) {
            builder.brain(parseBrain(json.getJSONObject("ai")));
        }
    }

    /**
     * Inizializza i dati specifici del giocatore qualora la struttura descriva un'entità utente controllabile.
     *
     * @param json    L'oggetto JSON sorgente.
     * @param builder Il builder di destinazione dei dati.
     */
    private static void parsePlayerConfig(JSONObject json, EntityRecordBuilder builder) {
        if (json.has("player")) {
            builder.player(parsePlayerRecord(json.getJSONObject("player")));
        }
    }

    /**
     * Esegue il parsing atomico dell'oggetto di configurazione dei parametri di movimento e abilità del giocatore.
     *
     * @param json L'oggetto JSON parziale relativo al nodo del giocatore.
     * @return     Un'istanza strutturata di {@link EntityRecord.PlayerRecord}.
     */
    private static EntityRecord.PlayerRecord parsePlayerRecord(JSONObject json) {
        return new EntityRecord.PlayerRecord(
                json.optDouble("dashImpulse", 35.0),
                json.optDouble("dashDurationSec", 0.666),
                json.optDouble("dashCooldownSec", 0.8),
                json.optDouble("stunDurationSec", 1.0),
                json.optDouble("baseCooldownSec", 0.3),
                json.optDouble("meleeDamage", 0),
                json.optDouble("meleeCooldownSec", 0.5),
                json.optDouble("baseSpeed", 10.0),
                json.optDouble("baseThrustForce", 30.0),
                json.optDouble("baseXPNeeded", 100.0),
                json.optDouble("dannoProiettile", 50.0),
                parseLevelAbilities(json.optJSONArray("levelAbilities"))
        );
    }

    /**
     * Genera l'elenco delle abilità sbloccabili del giocatore indicizzate per livello.
     *
     * @param arr L'array JSON contenente i nodi delle abilità di livello.
     * @return    Una lista ordinata decrescente per livello minimo richiesto di {@link EntityRecord.LevelAbility}.
     */
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

    /**
     * Costruisce il record logico complessivo del comportamento dell'AI (movimento, puntamento, pattern e modificatori).
     *
     * @param aiJson L'oggetto JSON corrispondente alla chiave principale "ai".
     * @return       Un'istanza completa di {@link EntityRecord.BrainRecord}.
     */
    private static EntityRecord.BrainRecord parseBrain(JSONObject aiJson) {
        EntityRecord.SteeringRecord steering = parseSteering(aiJson.optJSONObject("steering"));
        EntityRecord.RotationRecord rotation = parseRotation(aiJson.optJSONObject("rotation"));
        List<EntityRecord.AbilityRecord> abilities = parseAbilities(aiJson.optJSONArray("abilities"));
        List<EntityRecord.ModifierRecord> modifiers = parseModifiers(aiJson.optJSONArray("modifiers"));
        return new EntityRecord.BrainRecord(steering, rotation, abilities, modifiers);
    }

    /**
     * Parsifica i vincoli e gli obiettivi di posizionamento algoritmico della forza di sterzata (Steering).
     *
     * @param obj L'oggetto JSON parziale relativo allo steering.
     * @return    Un record pre-compilato {@link EntityRecord.SteeringRecord}.
     */
    private static EntityRecord.SteeringRecord parseSteering(JSONObject obj) {
        if (obj == null) return new EntityRecord.SteeringRecord(SteeringGoalIndex.IDLE, 2.5, 2.5, 10.0, 5.0);
        return new EntityRecord.SteeringRecord(
                SteeringGoalIndex.fromJson(obj.optString("type", SteeringGoalIndex.IDLE.jsonKey)),
                obj.optDouble("maxPredictionTime", 2.5),
                obj.optDouble("minDistance", 2.5),
                obj.optDouble("maxDistance", 10.0),
                obj.optDouble("safetyDistance", 5.0)
        );
    }

    /**
     * Parsifica le modalità di rotazione angolare sul proprio asse del modello dell'entità.
     *
     * @param obj L'oggetto JSON parziale relativo alla rotazione.
     * @return    Un record pre-compilato {@link EntityRecord.RotationRecord}.
     */
    private static EntityRecord.RotationRecord parseRotation(JSONObject obj) {
        if (obj == null) return new EntityRecord.RotationRecord(RotationGoalIndex.IDLE, 0.0, 8, 0.1, "player");
        return new EntityRecord.RotationRecord(
                RotationGoalIndex.fromJson(obj.optString("type", RotationGoalIndex.IDLE.jsonKey)),
                obj.optDouble("spinSpeedRadPerSec", 0.0),
                obj.optInt("spinSteps", 8),
                obj.optDouble("stepPauseSec", 0.1),
                obj.optString("target", "player")
        );
    }

    /**
     * Estrae la lista sequenziale di attacchi, cure, spawn o abilità attive assegnate all'entità.
     *
     * @param arr L'array JSON contenente i blocchi delle singole abilità.
     * @return    Una lista di oggetti di tipo {@link EntityRecord.AbilityRecord}.
     */
    private static List<EntityRecord.AbilityRecord> parseAbilities(JSONArray arr) {
        List<EntityRecord.AbilityRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            AbilityIndex type = AbilityIndex.fromJson(obj.optString("type"));
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

    /**
     * Mappa un sotto-array JSON di configurazioni balistiche ricorsive o nidificate.
     *
     * @param arr L'array JSON dei pattern balistici o di sventagliata.
     * @return    Una lista strutturata di {@link EntityRecord.PatternRecord}.
     */
    private static List<EntityRecord.PatternRecord> parsePatternList(JSONArray arr) {
        List<EntityRecord.PatternRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            list.add(parsePattern(arr.getJSONObject(i)));
        }
        return list;
    }

    /**
     * Parsifica un singolo schema balistico o di evocazione, supportando la ricorsione di sotto-pattern.
     *
     * @param obj L'oggetto JSON parziale relativo al pattern del proiettile.
     * @return    Un oggetto descrittivo {@link EntityRecord.PatternRecord}, o null se l'input è assente.
     */
    private static EntityRecord.PatternRecord parsePattern(JSONObject obj) {
        if (obj == null) return null;
        return new EntityRecord.PatternRecord(
                PatternIndex.fromJson(obj.optString("type")),
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

    /**
     * Estrae i modificatori di comportamento di gruppo (flocking) quali coesione, separazione o evitamento ostacoli.
     *
     * @param arr L'array JSON contenente l'elenco dei modificatori fisici ambientali.
     * @return    Una lista riempita di elementi {@link EntityRecord.ModifierRecord}.
     */
    private static List<EntityRecord.ModifierRecord> parseModifiers(JSONArray arr) {
        List<EntityRecord.ModifierRecord> list = new ArrayList<>();
        if (arr == null) return list;
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            list.add(new EntityRecord.ModifierRecord(
                    ModifierIndex.fromJson(obj.optString("type")),
                    obj.optDouble("radius", 2.0),
                    obj.optDouble("weight", 1.0),
                    obj.optDouble("maxPredictionTime", 1.0),
                    obj.optDouble("avoidRadius", 2.0)
            ));
        }
        return list;
    }

    /**
     * Metodo di supporto per convertire in modo sicuro un {@link JSONArray} di stringhe in una lista Java nativa.
     *
     * @param array L'array JSON sorgente.
     * @return      Una lista di stringhe corrispondente ai valori estratti.
     */
    private static List<String> jsonArrayToList(JSONArray array) {
        List<String> list = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
        }
        return list;
    }

    /**
     * Fabbrica l'istanza operativa del comportamento di sterzata (SteeringGoal) associandolo al tracciamento del giocatore.
     *
     * @param steeringData I metadati estratti in precedenza durante il parsing del record.
     * @return             L'oggetto pronto per l'esecuzione sul motore fisico {@link SteeringGoal}.
     */
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

    /**
     * Genera un modificatore di sterzata contestuale escludendo in automatico i proiettili amici dai calcoli di vicinato.
     *
     * @param mc     I dati descrittivi del modificatore da generare.
     * @param entity L'entità proprietaria del comportamento che richiede la scansione di prossimità.
     * @return       Il comportamento applicabile finale {@link SteeringModifier}.
     */
    public static SteeringModifier createModifier(EntityRecord.ModifierRecord mc, EntityModel entity) {
        if (mc.type() == null) return null;
        DoubleProperty weight = new SimpleDoubleProperty(mc.weight());
        Target neighbors = Target.neighboursCached(entity, mc.radius(), EntityFilters.isNot(entity));
        Target everythingButEnemyProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof ProjectileModel pm && pm.getType() == ProjectileType.ENEMY_BULLET));
        Target everythingButProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof AbstractPhysicalProjectileModel));
        return switch (mc.type()) {
            case SEPARATION -> SteeringModifier.separation(everythingButEnemyProjectiles, mc.radius(), weight);
            case ALIGNMENT -> SteeringModifier.alignment(everythingButProjectiles, weight);
            case COHESION -> SteeringModifier.cohesion(everythingButProjectiles, weight);
            case COLLISION_AVOIDANCE ->
                    SteeringModifier.collisionAvoidance(everythingButEnemyProjectiles, mc.maxPredictionTime(), mc.avoidRadius(), weight);
        };
    }

    /**
     * Instanzia l'algoritmo di rotazione e puntamento dell'asse del corpo in base ai requisiti dell'AI.
     *
     * @param rotation     I dati di configurazione della rotazione.
     * @param entityRecord
     * @return Un modulo operativo pronto {@link RotationGoal}.
     */
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

    /**
     * Fabbrica l'abilità attiva di attacco o utilità iniettandovi i parametri balistici e i modificatori di danno dell'entità.
     *
     * @param ac     Il record descrittivo dell'abilità estratto dal file JSON.
     * @param entity L'istanza dell'entità di gioco che andrà ad eseguire l'azione.
     * @return       L'oggetto operativo estensione dell'interfaccia {@link Ability}.
     */
    public static Ability createAbility(EntityRecord.AbilityRecord ac, EntityModel entity) {
        if (ac.type() == null) return null;
        Target target = Target.ofPlayer();

        // Recuperiamo il DannoProiettile configurato nella radice del JSON dell'entità (es. 100 per Master, 5 per Mob)
        double dannoEntita = entity.getEntityRecord().dannoProiettile();

        return switch (ac.type()) {
            case SHOOT -> {
                Pattern shooter = createPattern(ac.pattern());
                yield new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), shooter,
                        target, ac.aimAtTarget(), ac.nerfPrediction(), dannoEntita);
            }
            case RANDOMIZED_SHOOT -> {
                List<Pattern> patterns = new ArrayList<>();
                for (EntityRecord.PatternRecord pc : ac.patterns()) patterns.add(createPattern(pc));
                yield RandomizedShootAbility.targetingPlayer(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), ac.aimAtTarget(),
                        ac.nerfPrediction(), dannoEntita, patterns.toArray(new Pattern[0]));
            }
            case HEAL -> new HealAbility(ac.cooldownSec(), ac.combatRange(), ac.healAmount());
            case SUMMON -> {
                yield new ShootAbility(ac.combatRange(), ac.cooldownSec(),
                        ProjectileType.valueOf(ac.bulletType()), new SummonPattern(ac.summonCount(), ac.summonEntityKey(), ac.summonRadiusPx()),
                        target, ac.aimAtTarget(), ac.nerfPrediction(), dannoEntita);
            }
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

    /**
     * Risolve ricorsivamente le strutture dei sotto-pattern balistici istanziando le classi logiche dedicate ai proiettili o alle evocazioni.
     *
     * @param pc I metadati descrittivi della sventagliata o ripetizione.
     * @return   L'oggetto di calcolo geometrico {@link Pattern}.
     */
    public static Pattern createPattern(EntityRecord.PatternRecord pc) {
        if (pc == null) return new SingleShotPattern();
        return switch (pc.type()) {
            case SINGLE_SHOT -> new SingleShotPattern();
            case SPREAD -> new SpreadPattern(pc.count(), pc.angleStepDeg());
            case MULTI_DIRECTION ->
                    new MultiDirectionPattern(pc.count(), Math.toRadians(pc.angleStepDeg()), createPattern(pc.innerPattern()));
            case RING -> new RingPattern(pc.count());
            case REPEATER ->
                    new RepeaterPattern(pc.repeats(), pc.intervalSec(), createPattern(pc.innerPattern()));
            case PARALLEL_LINE ->
                    new ParallelLinePattern(pc.count(), pc.angleStepDeg());
            case SUMMON -> new SummonPattern(pc.count(), pc.summonedEntityKey(), pc.summonRadiusPx());
            case FIGURE -> new FigurePattern(pc.count(), FigurePattern.FigureType.valueOf(pc.figureType()));
        };
    }
}