package uni.gaben.iscat.universe.entities;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidShapeFactory;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;
import uni.gaben.iscat.utils.EntityAudioManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityModel extends AbstractLivingEntityModel implements HasSprite, HasShockwave {
    private final Shockwave shockwave = new Shockwave();
    private static final Random RNG = new Random();

    private EntityState currentEntityState = EntityState.IDLE;
    private boolean completeKillCalled = false;
    private UniverseWaveController waveController;
    private double idleAudioTimer = 5.0 + Math.random() * 10.0;
    private int currentAnimationRow;

    public EntityModel(double x, double y, EntityRecord entity) {
        super(x, y, entity);
        setXpReward(entity.xpReward());

        if (entity.hasEntranceAnimation()) {
            this.currentEntityState = EntityState.ENTRANCE;
            this.currentAnimationRow = findRowByType("ENTRANCE", EntityState.ENTRANCE.ordinal());
            setEnabled(false);
        } else {
            this.currentAnimationRow = findRowByType("IDLE", EntityState.IDLE.ordinal());
            setState(this.currentAnimationRow);
        }

        double collisionSize = UU.pxToM(entity.frameW() * entity.scale() * 0.9);
        BodyFixture fixture = switch (entity.shapeType()) {
            case CIRCLE -> addFixture(Geometry.createCircle(collisionSize / 2.0));
            case SQUARE -> addFixture(Geometry.createSquare(collisionSize));
            case POLYGON -> addFixture(new Polygon(AsteroidShapeFactory.getScaledShape(collisionSize)));
        };

        if (entity.isBoss() || "iscat-master".equals(entity.entityKey())) {
            fixture.setFilter(UniverseCollisionLayers.MASTER_FILTER);
        } else {
            fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        }
        setMass(entity.maxAngularVelocity() > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        setLinearDamping(entity.linearDamping());


        if (!entity.hasEntranceAnimation()) {
            EntityAudioManager.playEventAudio(this, "spawn");
        }
    }

    public void setWaveController(UniverseWaveController waveController) {
        this.waveController = waveController;
    }

    @Override
    public int getState() {
        return currentAnimationRow;
    }

    public void setEntityState(EntityState state) {
        // Fallback sul vecchio sistema se chiamato senza riga esplicita (usa la row associata al tipo)
        setEntityState(state, findRowByType(state.name(), state.ordinal()));
    }

    public void setCollisionFilter(org.dyn4j.collision.CategoryFilter filter) {
        for (int i = 0; i < getFixtureCount(); i++) {
            getFixture(i).setFilter(filter);
        }
    }

    public void setEntityState(EntityState state, int animationRow) {
        if (this.currentEntityState != state || this.currentAnimationRow != animationRow) {
            this.currentEntityState = state;
            this.currentAnimationRow = animationRow;
            setState(animationRow);
            setStateTime(0);
        }
    }

    /**
     * Recupera l'oggetto di configurazione dell'animazione analizzando la riga fisica corrente
     */
    private EntityRecord.AnimationRecord getCurrentAnimationRecord() {
        if (entity.animations() == null) return null;
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.row() == currentAnimationRow) {
                return anim;
            }
        }
        return null;
    }

    /**
     * Cerca nel JSON tutte le righe fisiche associate a un tipo (es. "ATTACK").
     * Se ne trova più di una le pesca a caso, così un nemico con 3 righe ATTACK
     * le ruota in modo casuale. Se non ne trova nessuna usa fallbackRow.
     */
    private int findRowByType(String typeName, int fallbackRow) {
        if (entity.animations() == null) return fallbackRow;
        List<Integer> candidates = new ArrayList<>();
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.type().equalsIgnoreCase(typeName)) {
                candidates.add(anim.row());
            }
        }
        if (candidates.isEmpty()) return fallbackRow;
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    public int getFramesForState(int rowIndex) {
        if (entity.animations() == null) return 1;
        for (EntityRecord.AnimationRecord anim : entity.animations()) {
            if (anim.row() == rowIndex) return anim.frames();
        }
        return 1;
    }

    @Override
    public void update(double dt) {
        updateStateTime(dt);
        shockwave.update(dt);

        if (currentEntityState == EntityState.IDLE) {
            idleAudioTimer -= dt;
            if (idleAudioTimer <= 0) {
                EntityAudioManager.playEventAudio(this, "idle");
                idleAudioTimer = 8.0 + Math.random() * 14.0;
            }
        }

        // Calcola la durata totale leggendo la configurazione della riga corrente
        double duration = 0;
        EntityRecord.AnimationRecord anim = getCurrentAnimationRecord();
        if (anim != null) {
            duration = anim.durationSec() > 0 ? anim.durationSec() : anim.frames() * getFrameDuration();
        }

        if (currentEntityState == EntityState.ENTRANCE) {
            if (getFramesForState(currentAnimationRow) <= 0 || getStateTime() >= duration) {
                setEnabled(true);
                setEntityState(EntityState.IDLE);
                if (entity.isBoss()) {
                    shockwave.trigger(2.0, 1500, 15);
                }
                EntityAudioManager.playEventAudio(this, "spawn");
            }
        }
        else if (currentEntityState != EntityState.IDLE && currentEntityState != EntityState.DEATH) {
            if (getStateTime() >= duration) {
                setEntityState(EntityState.IDLE);
            }
        }
        else if (currentEntityState == EntityState.DEATH) {
            if (getStateTime() >= duration && !completeKillCalled) {
                completeKill();
            }
        }
    }

    @Override
    public void extinguish(boolean silent) {
        boolean hasDeathAnimation = false;
        if (entity.animations() != null) {
            for (EntityRecord.AnimationRecord anim : entity.animations()) {
                if (anim.type().equalsIgnoreCase("DEATH")) {
                    hasDeathAnimation = true;
                    break;
                }
            }
        }

        if (hasDeathAnimation) {
            if (currentEntityState == EntityState.DEATH) return;
            setEntityState(EntityState.DEATH, findRowByType("DEATH", EntityState.DEATH.ordinal()));
        } else {
            super.extinguish(silent);
            completeKill();
        }
    }

    @Override
    public boolean shouldRemove() {
        if (currentEntityState == EntityState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    public void completeKill() {
        if (completeKillCalled) return;
        completeKillCalled = true;

        if (entity.isBoss() && waveController != null) {
            waveController.notifyBossDead();
        }

        setShouldRemove(true);
    }

    @Override public Shockwave shockwave() { return shockwave; }
    @Override public String getSpritePath() { return entity.spritePath(); }
    @Override public int getSpriteFrameWidth() { return entity.frameW(); }
    @Override public int getSpriteFrameHeight() { return entity.frameH(); }
    @Override public double getVisualScale() { return entity.scale(); }
    @Override public double getVisualAngularOffsetDeg() { return entity.angularOffsetDeg(); }

    @Override
    public double getFrameDuration() {
        EntityRecord.AnimationRecord anim = getCurrentAnimationRecord();
        if (anim != null && anim.durationSec() > 0) {
            return anim.durationSec() / (double) anim.frames();
        }
        return UU.UNIVERSE_TICK * 6;
    }

    @Override public double getFrameDuration(int state, int frame) { return getFrameDuration(); }

    @Override
    public boolean isInalterable() {
        return false;
    }

    public EntityState getCurrentEntityState() {
        return currentEntityState;
    }
}