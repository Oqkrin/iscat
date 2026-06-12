package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.Shockwave;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidShapeFactory;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;
import uni.gaben.iscat.utils.EnemyAudioManager;

public class EntityModel extends AbstractLivingEntityModel implements HasSprite, HasShockwave {

    public static final int STATE_ENTRANCE = 0;
    public static final int STATE_IDLE     = 1;
    public static final int STATE_DEATH    = 6;

    private final EntityRecord entity;
    private final Shockwave shockwave = new Shockwave();

    private int currentState = STATE_IDLE;
    private boolean completeKillCalled = false;
    private UniverseWaveController waveController;

    private double idleAudioTimer = 5.0 + Math.random() * 10.0;

    public EntityModel(double x, double y, EntityRecord entity) {
        super(x, y, entity);
        this.entity = entity;

        setXpReward(entity.xpReward());

        // Gestione stato iniziale ed Entrance Animation
        if (entity.hasEntranceAnimation()) {
            this.currentState = STATE_ENTRANCE;
            setEnabled(false);
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

            setOnCollision(other -> {
                if (other instanceof PlayerModel player) {
                    double speed = this.getLinearVelocity().getMagnitude();
                    if (speed > entity.maxVelocity() * 0.85) {
                        player.alter(-heavyDamage);
                    } else {
                        player.alter(-lightDamage);
                    }
                }
            });
        }

        if (!entity.hasEntranceAnimation()) {
            EnemyAudioManager.playEventAudio(this, "spawn");
        }
    }

    public void setWaveController(UniverseWaveController waveController) {
        this.waveController = waveController;
    }

    @Override
    public int getState() {
        return currentState;
    }

    public void setCurrentState(int state) {
        if (this.currentState != state) {
            this.currentState = state;
            this.setStateTime(0.0);
        }
    }

    public int getFramesForState(int state) {
        if (entity.animationFrames() != null && state >= 0 && state < entity.animationFrames().length) {
            return entity.animationFrames()[state];
        }
        return 1;
    }

    public void update(double dt) {
        updateStateTime(dt);
        shockwave.update(dt);

        double duration = getFramesForState(currentState) * getFrameDuration();

        if (currentState == STATE_IDLE) {
            idleAudioTimer -= dt;
            if (idleAudioTimer <= 0) {
                EnemyAudioManager.playEventAudio(this, "idle");
                idleAudioTimer = 8.0 + Math.random() * 14.0;
            }
        }

        if (currentState == STATE_ENTRANCE) {
            if (getStateTime() >= duration) {
                setEnabled(true);
                setCurrentState(STATE_IDLE);

                if (entity.isBoss()) {
                    shockwave.trigger(2.0, 1500, 15);
                }

                EnemyAudioManager.playEventAudio(this, "spawn");
            }
        }
        else if (currentState != STATE_IDLE && currentState != STATE_DEATH) {
            if (getStateTime() >= duration) {
                setCurrentState(STATE_IDLE);
            }
        }
        else if (currentState == STATE_DEATH) {
            if (getStateTime() >= duration && !completeKillCalled) {
                completeKill();
            }
        }
    }

    @Override
    public void extinguish(boolean silent) {
        if (entity.animationFrames() != null && entity.animationFrames().length > STATE_DEATH) {
            if (currentState == STATE_DEATH) return;
            setCurrentState(STATE_DEATH);
        } else {
            super.extinguish(silent);
            completeKill();
        }
    }

    @Override
    public boolean shouldRemove() {
        if (currentState == STATE_DEATH && !completeKillCalled) return false;
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
    @Override public double getTerminalVelocity() { return entity.maxVelocity(); }
    @Override public double getMaxVelocity() { return entity.maxVelocity(); }
    @Override public double getMaxForce() { return entity.maxForce(); }
    @Override public double getMaxAngularVelocity() { return entity.maxAngularVelocity(); }
    public EntityRecord getEntity() { return entity; }
    @Override public String getSpritePath() { return entity.spritePath(); }
    @Override public int getSpriteFrameWidth() { return entity.frameW(); }
    @Override public int getSpriteFrameHeight() { return entity.frameH(); }  // FIXED
    @Override public double getVisualScale() { return entity.scale(); }
    @Override public double getVisualAngularOffsetDeg() { return 0; }
    @Override public double getFrameDuration() { return UU.UNIVERSE_TICK * 3; }
    @Override public double getFrameDuration(int state, int frame) { return getFrameDuration(); }
}