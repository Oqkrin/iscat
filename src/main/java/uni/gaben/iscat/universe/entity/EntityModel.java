package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.Shockwave;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;
import uni.gaben.iscat.utils.EnemyAudioManager;

public class EntityModel extends AbstractLivingModel implements HasSprite, HasShockwave {

    public static final int STATE_ENTRANCE = 0;
    public static final int STATE_IDLE     = 1;
    public static final int STATE_DEATH    = 6;

    private final EntitySettings settings;
    private final Shockwave shockwave = new Shockwave();

    private int currentState = STATE_IDLE;
    private boolean completeKillCalled = false;
    private UniverseWaveController waveController;

    public EntityModel(double x, double y, EntitySettings settings) {
        super(x, y, settings.initLife, settings.initLife);
        this.settings = settings;

        if (settings != null && settings.entityKey != null) {
            setEntityKey(settings.entityKey);
        }

        setXpReward(settings.xpReward);

        // Gestione stato iniziale ed Entrance Animation
        if (settings.hasEntranceAnimation) {
            this.currentState = STATE_ENTRANCE;
            setEnabled(false); // Disabilitato fino alla fine dell'animazione di spawn
        }

        double collisionSize = UU.pxToM(settings.frameW * settings.scale * 0.9);
        BodyFixture fixture = switch (settings.shapeType) {
            case CIRCLE -> addFixture(Geometry.createCircle(collisionSize / 2.0));
            case SQUARE -> addFixture(Geometry.createSquare(collisionSize));
        };

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(settings.maxAngularVelocity > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        setLinearDamping(settings.linearDamping);

        // Logica danno da sfondamento (RAM)
        if (settings.mass > 2.0) {
            final double heavyDamage = settings.mass * 5.0;
            final double lightDamage = settings.mass * 2.0;

            setOnCollision(other -> {
                if (other instanceof PlayerModel player) {
                    double speed = this.getLinearVelocity().getMagnitude();
                    if (speed > settings.maxVelocity * 0.85) {
                        player.deltaToLife(-heavyDamage);
                    } else {
                        player.deltaToLife(-lightDamage);
                    }
                }
            });
        }

        if (!settings.hasEntranceAnimation) {
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
        if (settings.animationFrames != null && state >= 0 && state < settings.animationFrames.length) {
            return settings.animationFrames[state];
        }
        return 1;
    }

    public void update(double dt) {
        updateStateTime(dt);
        shockwave.update(dt);

        double duration = getFramesForState(currentState) * getFrameDuration();

        if (currentState == STATE_ENTRANCE) {
            if (getStateTime() >= duration) {
                setEnabled(true);
                setCurrentState(STATE_IDLE);

                // Il boss finisce l'animazione di ingresso: genera l'onda d'urto
                if (settings.isBoss) {
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
    public void kill(boolean silent) {
        if (settings.animationFrames != null && settings.animationFrames.length > STATE_DEATH) {
            if (currentState == STATE_DEATH) return;
            setCurrentState(STATE_DEATH);
        } else {
            super.kill(silent);
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

        if (settings.isBoss && waveController != null) {
            waveController.notifyBossDead();
        }

        setShouldRemove(true);
    }

    @Override public Shockwave shockwave() { return shockwave; }
    @Override public double getTerminalVelocity() { return settings.maxVelocity; }
    @Override public double getMaxVelocity() { return settings.maxVelocity; }
    @Override public double getMaxForce() { return settings.maxForce; }
    @Override public double getMaxAngularVelocity() { return settings.maxAngularVelocity; }
    public EntitySettings getSettings() { return settings; }
    @Override public String getSpritePath() { return settings.spritePath; }
    @Override public int getSpriteFrameWidth() { return settings.frameW; }
    @Override public int getSpriteFrameHeight() { return settings.frameW; }
    @Override public double getVisualScale() { return settings.scale; }
    @Override public double getVisualAngularOffsetDeg() { return 0; }
    @Override public double getFrameDuration() { return UU.UNIVERSE_TICK * 3; }
    @Override public double getFrameDuration(int state, int frame) { return getFrameDuration(); }
}