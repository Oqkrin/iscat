package uni.gaben.iscat.game.universe.enemies.iscat_master;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.game.controller.EnemyWaveController;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.enemies.fake_iscat.FakeIscatSettings;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

public class IscatMasterModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();

    public enum AnimationState { IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.IDLE;

    private boolean entranceDone = false;
    private boolean completeKillCalled = false;

    private final EnemyWaveController waveController;

    public IscatMasterModel(double x, double y, EnemyWaveController waveController) {
        super(x, y, IscatMasterSettings.HP_INIZIALI, IscatMasterSettings.HP_INIZIALI);
        this.waveController = waveController;
        setXpReward(IscatMasterSettings.XP_REWARD);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(IscatMasterSettings.DIM_SPRITE
                                * IscatMasterSettings.SCALE / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(IscatMasterSettings.DAMPING_LINEARE);

        // Collisioni disabilitate finché l'entrata non è completata
        setEnabled(false);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
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
        return IscatMasterSettings.MAX_VELOCITY;
    }
}