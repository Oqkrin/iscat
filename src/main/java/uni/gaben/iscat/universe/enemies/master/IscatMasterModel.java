package uni.gaben.iscat.universe.enemies.master;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.ISCATMASTER;

public class IscatMasterModel extends LivingEntityModel implements HasProjectile<Projectile>, HasShockwave, Updatable {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();
    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    @Override
    public ShockwaveModel shockwave() {
        return shockwaveModel;
    }

    public enum AnimationState { IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.IDLE;

    private boolean entranceDone = false;
    private boolean completeKillCalled = false;

    private final UniverseWaveController waveController;

    public IscatMasterModel(double x, double y, UniverseWaveController waveController) {
        super(x, y, ISCATMASTER.initLife, ISCATMASTER.initLife);
        this.waveController = waveController;
        setXpReward(ISCATMASTER.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATMASTER.dimSprite
                                * ISCATMASTER.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.FIXED_ANGULAR_VELOCITY);
        setLinearDamping(ISCATMASTER.dampingLineare);

        // Collisioni disabilitate finché l'entrata non è completata
        setEnabled(false);
    }

    @Override
    public void update(double dt) {
        weaponCooldown.update(dt);
        shockwaveModel.update(dt);
    }

    // ── ANIMATION STATE ───────────────────────────────────────────────────────

    public AnimationState getAnimationState() { return animationState; }
    public void setAnimationState(AnimationState state) { this.animationState = state; }

    // ── ENTRANCE ──────────────────────────────────────────────────────────────

    public boolean isEntranceDone() { return entranceDone; }
    public void setEntranceDone(boolean done) { this.entranceDone = done; }

    // ── DEATH ─────────────────────────────────────────────────────────────────

    @Override
    public void kill(boolean b) {
        if (animationState == AnimationState.DEATH) return; // già in death, ignora
        setAnimationState(AnimationState.DEATH);
        // NON chiamare super.kill() — aspetta la fine dell'animazione
    }

    @Override
    public boolean shouldRemove() {
        // Blocca la rimozione finché l'animazione death non è completata
        if (animationState == AnimationState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    /** Chiamato dalla View quando l'animazione death è terminata. */
    public void completeKill() {
        if (completeKillCalled) return; // guard contro doppia chiamata
        completeKillCalled = true;
        if (waveController != null) waveController.notifyBossDead();
        super.kill(true);
    }

    // ── HAS PROJECTILE ────────────────────────────────────────────────────────

    @Override
    public Projectile getProjectile() { return projectile; }

    @Override
    public boolean hasAmmo() { return true; }

    @Override
    public Cooldown projectileCooldown() { return weaponCooldown; }

    @Override
    public int getProjectileCooldownTickCount() { return 0; }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(UU.ticksToS(tickCount));
    }

    @Override
    public double getTerminalVelocity() {
        return ISCATMASTER.maxVelocity;
    }
}