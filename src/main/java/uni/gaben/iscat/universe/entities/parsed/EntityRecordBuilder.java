package uni.gaben.iscat.universe.entities.parsed;

import uni.gaben.iscat.universe.entities.EntityType;
import uni.gaben.iscat.universe.entities.ThreatLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder flessibile per la costruzione fluida e controllata di istanze di {@link EntityRecord}.
 * Fornisce valori di default predefiniti per tutti i parametri fisici, grafici e comportamentali.
 */
public class EntityRecordBuilder {
    private String entityKey = "";
    private String name = "";
    private String description = "";
    private Integer bestiaryOrder = 0;
    private ThreatLevel threatLevel = ThreatLevel.NORMAL;
    private EntityType type = null;

    private String spritePath = "";
    private int frameW = 32;
    private int frameH = 32;
    private double scale = 1.0;
    private List<EntityRecord.AnimationRecord> animations = new ArrayList<>();
    private boolean isBoss = false;
    private boolean hasEntranceAnimation = false;

    private double initLife = 100;
    private double linearDamping = 2.0;
    private double mass = 1.0;
    private double maxVelocity = 10.0;
    private double maxForce = 10.0;
    private double maxAngularVelocity = 5.0;
    private double visualAngularOffset = 0.0;
    private int xpReward = 10;
    private EntityRecord.ShapeType shapeType = EntityRecord.ShapeType.CIRCLE;

    private double detectionRange = 15.0;
    private double combatRange = 10.0;
    private double preferredRange = 7.0;
    private double actionCooldownSec = 0.8;

    private double dannoProiettile = 4.0;

    private EntityRecord.AudioProfile audio = new EntityRecord.AudioProfile(List.of(), List.of(), List.of(), List.of(), List.of());

    private EntityRecord.BrainRecord brain = null;

    private EntityRecord.PlayerRecord player = null;

    public EntityRecordBuilder entityKey(String v) { entityKey = v; return this; }
    public EntityRecordBuilder name(String v) { name = v; return this; }
    public EntityRecordBuilder description(String v) { description = v; return this; }
    public EntityRecordBuilder bestiaryOrder(Integer v) { bestiaryOrder = v; return this; }
    public EntityRecordBuilder threatLevel(ThreatLevel v) { threatLevel = v; return this; }
    public EntityRecordBuilder spritePath(String v) { spritePath = v; return this; }
    public EntityRecordBuilder frameW(int v) { frameW = v; return this; }
    public EntityRecordBuilder frameH(int v) { frameH = v; return this; }
    public EntityRecordBuilder scale(double v) { scale = v; return this; }

    /**
     * Assegna la lista completa di record di animazione associati alle righe dello spritesheet.
     */
    public EntityRecordBuilder animations(List<EntityRecord.AnimationRecord> v) {
        this.animations = v;
        return this;
    }

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
    public EntityRecordBuilder dannoProiettile(double v) { dannoProiettile = v; return this; }
    public EntityRecordBuilder audio(EntityRecord.AudioProfile v) { audio = v; return this; }
    public EntityRecordBuilder brain(EntityRecord.BrainRecord v) { brain = v; return this; }
    public EntityRecordBuilder player(EntityRecord.PlayerRecord v) { player = v; return this; }
    public EntityRecordBuilder visualAngularOffset(double deg) { this.visualAngularOffset = deg; return this; }

    /**
     * Compila e restituisce l'istanza immutabile di EntityRecord configurata.
     */
    public EntityRecord build() {
        return new EntityRecord(
                entityKey, name, type, description, bestiaryOrder, threatLevel,
                spritePath, frameW, frameH, scale,
                animations, isBoss, hasEntranceAnimation,
                initLife, linearDamping, mass, maxVelocity, maxForce, maxAngularVelocity, visualAngularOffset, xpReward, shapeType,
                detectionRange, combatRange, preferredRange, actionCooldownSec, dannoProiettile,
                audio, brain, player
        );
    }
}