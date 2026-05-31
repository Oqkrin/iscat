package uni.gaben.iscat.universe.enemies.master;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.database.sqlite.EnemyDAO;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

public class IscatMasterModel extends LivingEntityModel implements HasShockwave, Updatable {

    private static final String ENTITY_KEY = "iscat_master";

    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    public enum AnimationState { IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.IDLE;

    private boolean entranceDone = false;
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

        setXpReward(s.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(s.dimSprite * s.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.FIXED_ANGULAR_VELOCITY);
        setLinearDamping(s.dampingLineare);

        setEnabled(false);
    }

    private static GenericEntitySettings loadSettings() {
        return EnemyDAO.findByKey(ENTITY_KEY).orElseGet(() -> {
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
    public void update(double dt) { shockwaveModel.update(dt); }


    public AnimationState getAnimationState() { return animationState; }
    public void setAnimationState(AnimationState state) { this.animationState = state; }


    public boolean isEntranceDone() { return entranceDone; }
    public void setEntranceDone(boolean done) { this.entranceDone = done; }


    @Override
    public void kill() { this.kill(true); }

    @Override
    public void kill(boolean b) {
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
        if (waveController != null) waveController.notifyBossDead();
        super.kill(true);
    }

    @Override
    public double getTerminalVelocity() { return settings.maxVelocity; }
}