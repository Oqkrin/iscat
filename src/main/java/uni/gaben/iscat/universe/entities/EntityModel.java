package uni.gaben.iscat.universe.entities;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.spawn.UniverseWaveController;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidShapeFactory;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;
import uni.gaben.iscat.utils.EntityAudioManager;

public class EntityModel extends AbstractLivingEntityModel implements HasSprite, HasShockwave {
    private final Shockwave shockwave = new Shockwave();

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
            this.currentAnimationRow = EntityState.ENTRANCE.ordinal();
            setEnabled(false);
        } else {
            this.currentAnimationRow = EntityState.IDLE.ordinal();
            setState(this.currentAnimationRow);
        }

        double collisionSize = UU.pxToM(entity.frameW() * entity.scale() * 0.9);
        BodyFixture fixture = switch (entity.shapeType()) {
            case CIRCLE -> addFixture(Geometry.createCircle(collisionSize / 2.0));
            case SQUARE -> addFixture(Geometry.createSquare(collisionSize));
            case POLYGON -> addFixture(new Polygon(AsteroidShapeFactory.getScaledShape(collisionSize)));
        };

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(entity.maxAngularVelocity() > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        setLinearDamping(entity.linearDamping());

        // Logica danno da sfondamento (RAM)
        if (entity.mass() > 2.0) {
            final double heavyDamage = entity.mass() * 5.0;
            final double lightDamage = entity.mass() * 2.0;

            addOnCollision("velocityDamage",other -> {
                if (other instanceof PlayerModel player) {
                    double speed = this.getLinearVelocity().getMagnitude();
                    if (speed > entity.maxVelocity() * 0.85) {
                        player.damage(heavyDamage);
                    } else {
                        player.damage(lightDamage);
                    }
                }
            });
        }

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
        setEntityState(state, state.ordinal());
    }

    public void setEntityState(EntityState state, int animationRow) {
        if (this.currentEntityState != state || this.currentAnimationRow != animationRow) {
            this.currentEntityState = state;
            this.currentAnimationRow = animationRow;
            setState(animationRow);
            setStateTime(0);
        }
    }

    public int getFramesForState(int state) {
        if (entity.animationFrames() != null && state >= 0 && state < entity.animationFrames().length) {
            return entity.animationFrames()[state];
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

        double duration;
        if (currentEntityState == EntityState.ATTACK) {
            duration = 3.0;
        } else {
            duration = getFramesForState(currentEntityState.ordinal()) * getFrameDuration();
        }

        if (currentEntityState == EntityState.ENTRANCE) {
            if (getFramesForState(EntityState.ENTRANCE.ordinal()) <= 0 || getStateTime() >= duration) {
                setEnabled(true);
                setEntityState(EntityState.IDLE);
                if (entity.isBoss()) {
                    shockwave.trigger(2.0, 1500, 15);
                }
                EntityAudioManager.playEventAudio(this, "spawn");
            }
        }
        else if (currentEntityState != EntityState.IDLE && currentEntityState != EntityState.DEATH) {
            // Questo blocco intercetta l'ATTACK: dopo 3 secondi esatti (duration = 3.0), resetta lo stato in IDLE
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
        if (entity.animationFrames() != null && entity.animationFrames().length > EntityState.DEATH.ordinal()) {
            if (currentEntityState == EntityState.DEATH) return;
            setEntityState(EntityState.DEATH);
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
        if (currentEntityState == EntityState.ATTACK) {
            int totalFrames = getFramesForState(currentEntityState.ordinal());
            return totalFrames > 0 ? (3.0 / totalFrames) : UU.UNIVERSE_TICK * 3;
        }
        return UU.UNIVERSE_TICK * 6;
    }
    @Override public double getFrameDuration(int state, int frame) { return getFrameDuration(); }

    @Override
    public boolean isInalterable() {
        return false;
    }
}