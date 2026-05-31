package uni.gaben.iscat.universe.enemies.master;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.universe.lib.interfaces.model.HasSprite;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

public class IscatMasterModel extends LivingEntityModel implements HasShockwave, Updatable, HasSprite {

    private static final String ENTITY_KEY = "iscat_master";

    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    @Override
    public String getSpritePath() {
        return settings.spritePath;
    }

    @Override
    public int getSpriteFrameWidth() {
        return settings.frameW;
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK*6;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return getFrameDuration();
    }

    @Override
    public int getSpriteFrameHeight() {
        return settings.frameH;
    }

    @Override
    public double getVisualScale() {
        return settings.scale;
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 0;
    }

    // The ENTRANCE state replaces the boolean entranceDone flag
    public enum AnimationState { ENTRANCE, IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.ENTRANCE;

    private boolean completeKillCalled = false;

    private final UniverseWaveController waveController;
    private final GenericEntitySettings settings;

    public IscatMasterModel(double x, double y, UniverseWaveController waveController) {
        this(x, y, waveController, loadSettings());
    }

    private IscatMasterModel(double x, double y, UniverseWaveController waveController,
                             GenericEntitySettings s) {
        super(x, y, s.initLife, s.initLife);
        this.waveController = waveController;
        this.settings = s;

        setEntityKey(ENTITY_KEY);
        setXpReward(s.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(s.dimSprite * s.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.FIXED_ANGULAR_VELOCITY);
        setLinearDamping(s.dampingLineare);

        // Disabilitato di default fino al completamento dell'animazione di spawn intro
        setEnabled(false);
    }

    private static GenericEntitySettings loadSettings() {
        return IscatDB.getInstance().getEnemyDAO().findByKey(ENTITY_KEY).orElseGet(() -> {
            GenericEntitySettings s = new GenericEntitySettings();
            s.initLife       = 1;
            s.dimSprite      = 1;
            s.scale          = 1;
            s.dampingLineare = 1;
            s.maxVelocity    = 1;
            s.xpReward       = 1;
            return s;
        });
    }

    public GenericEntitySettings getSettings() { return settings; }

    @Override
    public ShockwaveModel shockwave() { return shockwaveModel; }

    @Override
    public void update(double dt) {
        super.update(dt); // Crucial: Advances the stateTime
        shockwaveModel.update(dt);
    }

    public AnimationState getAnimationState() { return animationState; }

    // State changes automatically reset the animation timer so the View always starts at frame 0
    public void setAnimationState(AnimationState state) {
        if (this.animationState != state) {
            this.animationState = state;
            this.setStateTime(0.0);
        }
    }

    // Single source of truth for animation durations
    public int getFramesForState(AnimationState state) {
        return switch (state) {
            case ENTRANCE -> 21;
            case IDLE     -> 4;
            case ATTACK1  -> 3;
            case ATTACK2  -> 7;
            case ATTACK3  -> 8;
            case ATTACK4  -> 5;
            case DEATH    -> 6;
        };
    }

    @Override
    public void kill() { this.kill(true); }

    @Override
    public void kill(boolean silent) {
        if (animationState == AnimationState.DEATH) return;
        setAnimationState(AnimationState.DEATH);
    }

    @Override
    public boolean shouldRemove() {
        if (animationState == AnimationState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    public void completeKill() {
        if (completeKillCalled) return;
        completeKillCalled = true;

        try {
            var user = uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                IscatDB.getInstance().getEnemyDAO().incrementKill(user.id(), ENTITY_KEY);
            }
        } catch (Exception e) {
            System.err.println("[ERRORE REGISTRAZIONE BOSS] Impossibile aggiornare i record di morte su DB");
            e.printStackTrace();
        }

        if (waveController != null) {
            waveController.notifyBossDead();
        }

        super.kill(true);
    }

    @Override
    public double getTerminalVelocity() { return settings.maxVelocity; }
}