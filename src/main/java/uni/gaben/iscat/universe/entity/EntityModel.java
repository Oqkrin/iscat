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

public class GenericEntityModel extends LivingEntityModel implements HasSprite, HasShockwave {

    public static final int STATE_ENTRANCE = 0;
    public static final int STATE_IDLE     = 1;
    public static final int STATE_DEATH    = 6;

    private final GenericEntitySettings settings;
    private final Shockwave shockwave = new Shockwave();

    private int currentState = STATE_IDLE;
    private boolean completeKillCalled = false;
    private UniverseWaveController waveController; // Iniettato dinamicamente se l'entità è un boss

    public GenericEntityModel(double x, double y, GenericEntitySettings settings) {
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
    }

    /** Permette di agganciare il wave controller dopo lo spawn (es. da UniverseSpawner) */
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
        return 1; // Fallback se non specificato nel JSON
    }

    public void update(double dt) {
        updateStateTime(dt);
        shockwave.update(dt);

        double duration = getFramesForState(currentState) * getFrameDuration();

        if (currentState == STATE_ENTRANCE) {
            if (getStateTime() >= duration) {
                setEnabled(true);
                setCurrentState(STATE_IDLE);

                // Effetti di spawn del Boss presi dinamicamente dal JSON
                if (settings.isBoss) {
                    shockwave.trigger(2.0, 1500, 15);
                    try {
                        if (settings.audio != null && settings.audio.spawn != null) {
                            for (String sfx : settings.audio.spawn) {
                                uni.gaben.iscat.utils.AudioManager.getInstance().playSFX(sfx);
                            }
                        }
                    } catch (Exception e) {
                        // Ignora se l'audio non è disponibile
                    }
                }
            }
        }
        else if (currentState != STATE_IDLE && currentState != STATE_DEATH) {
            // Qualsiasi stato di attacco intermedio torna a IDLE a fine animazione
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
        // Se l'entità ha un'animazione di morte definita nel JSON, passa allo stato DEATH
        if (settings.animationFrames != null && settings.animationFrames.length > STATE_DEATH) {
            if (currentState == STATE_DEATH) return;
            setCurrentState(STATE_DEATH);
        } else {
            // Morte istantanea standard per i mob normali senza animazioni complesse
            super.kill(silent);
            completeKill();
        }
    }

    @Override
    public boolean shouldRemove() {
        // Impedisce la rimozione immediata se l'animazione di morte è ancora in esecuzione
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
    public GenericEntitySettings getSettings() { return settings; }
    @Override public String getSpritePath() { return settings.spritePath; }
    @Override public int getSpriteFrameWidth() { return settings.frameW; }
    @Override public int getSpriteFrameHeight() { return settings.frameW; }
    @Override public double getVisualScale() { return settings.scale; }
    @Override public double getVisualAngularOffsetDeg() { return 0; }
    @Override public double getFrameDuration() { return UU.UNIVERSE_TICK * 3; }
    @Override public double getFrameDuration(int state, int frame) { return getFrameDuration(); }
}