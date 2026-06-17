package uni.gaben.iscat.universe.entities;

import java.util.List;

public class EntityRecordBuilder {
    // Identity
    private String entityKey = "";
    private String name = "";
    private String description = "";

    private Integer bestiaryOrder = 0;
    private ThreatLevel threatLevel = ThreatLevel.NORMAL;

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
    private double angularOffsetRad = 0.0;
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
    public EntityRecordBuilder entityKey(String v) {
        entityKey = v;
        return this;
    }

    public EntityRecordBuilder name(String v) {
        name = v;
        return this;
    }

    public EntityRecordBuilder description(String v) {
        description = v;
        return this;
    }

    public EntityRecordBuilder bestiaryOrder(Integer v) {
        bestiaryOrder = v;
        return this;
    }

    public EntityRecordBuilder threatLevel(ThreatLevel v) {
        threatLevel = v;
        return this;
    }

    public EntityRecordBuilder spritePath(String v) {
        spritePath = v;
        return this;
    }

    public EntityRecordBuilder frameW(int v) {
        frameW = v;
        return this;
    }

    public EntityRecordBuilder frameH(int v) {
        frameH = v;
        return this;
    }

    public EntityRecordBuilder scale(double v) {
        scale = v;
        return this;
    }

    public EntityRecordBuilder animationFrames(int[] v) {
        animationFrames = v;
        return this;
    }

    public EntityRecordBuilder isBoss(boolean v) {
        isBoss = v;
        return this;
    }

    public EntityRecordBuilder hasEntranceAnimation(boolean v) {
        hasEntranceAnimation = v;
        return this;
    }

    public EntityRecordBuilder initLife(double v) {
        initLife = v;
        return this;
    }

    public EntityRecordBuilder linearDamping(double v) {
        linearDamping = v;
        return this;
    }

    public EntityRecordBuilder mass(double v) {
        mass = v;
        return this;
    }

    public EntityRecordBuilder maxVelocity(double v) {
        maxVelocity = v;
        return this;
    }

    public EntityRecordBuilder maxForce(double v) {
        maxForce = v;
        return this;
    }

    public EntityRecordBuilder maxAngularVelocity(double v) {
        maxAngularVelocity = v;
        return this;
    }

    public EntityRecordBuilder xpReward(int v) {
        xpReward = v;
        return this;
    }

    public EntityRecordBuilder shapeType(EntityRecord.ShapeType v) {
        shapeType = v;
        return this;
    }

    public EntityRecordBuilder detectionRange(double v) {
        detectionRange = v;
        return this;
    }

    public EntityRecordBuilder combatRange(double v) {
        combatRange = v;
        return this;
    }

    public EntityRecordBuilder preferredRange(double v) {
        preferredRange = v;
        return this;
    }

    public EntityRecordBuilder actionCooldownSec(double v) {
        actionCooldownSec = v;
        return this;
    }

    public EntityRecordBuilder audio(EntityRecord.AudioProfile v) {
        audio = v;
        return this;
    }

    public EntityRecordBuilder brain(EntityRecord.BrainRecord v) {
        brain = v;
        return this;
    }

    public EntityRecordBuilder player(EntityRecord.PlayerRecord v) {
        player = v;
        return this;
    }

    public EntityRecordBuilder angularOffsetRad(double angularOffsetRad) {
        this.angularOffsetRad = angularOffsetRad;
        return this;
    }

    public EntityRecord build() {
        return new EntityRecord(
                entityKey, name, description, bestiaryOrder, threatLevel,
                spritePath, frameW, frameH, scale,
                animationFrames, isBoss, hasEntranceAnimation,
                initLife, linearDamping, mass, maxVelocity, angularOffsetRad, maxForce, maxAngularVelocity, xpReward, shapeType,
                detectionRange, combatRange, preferredRange, actionCooldownSec,
                audio, brain, player
        );
    }


}