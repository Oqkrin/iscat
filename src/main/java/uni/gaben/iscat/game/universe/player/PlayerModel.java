package uni.gaben.iscat.game.universe.player;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.utils.Cooldown;

public class PlayerModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Cooldown weaponCooldown = new Cooldown();

    private final Projectile projectile = new Projectile();

    public PlayerModel(double x, double y) {
        super(x, y, PlayerSettings.HP_INIZIALE, PlayerSettings.HP_MASSIMO);

        // Convert physics collision metrics through the UU boundary helper
        double radiusInMeters = UU.pxToM(PlayerSettings.RAGGIO_COLLISIONE);
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusInMeters));

        fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(PlayerSettings.LINEAR_DAMPING);
    }

    public void update(double dt) {
        // Uniform clock ticking processing via dt seconds
        dashCooldown.update(dt);
        dashDuration.update(dt);
        weaponCooldown.update(dt);

        if (isInScatto()) {
            setLinearDamping(PlayerSettings.LINEAR_DAMPING_SCATTO);
        } else {
            setLinearDamping(PlayerSettings.LINEAR_DAMPING);
        }
    }

    public void executeScatto(double angle) {
        Vector2 dashDir = new Vector2(Math.cos(angle), Math.sin(angle));

        // Directional Snap: instantly counter current momentum if dashing backwards
        if (getLinearVelocity().dot(dashDir) < 0) {
            setLinearVelocity(new Vector2(0, 0));
        }

        applyImpulse(dashDir.multiply(PlayerSettings.IMPULSO_SCATTO * getMass().getMass()));

        dashDuration.start(PlayerSettings.DURATA_SCATTO_SEC);
        dashCooldown.start(PlayerSettings.COOLDOWN_SCATTO_SEC);

        // Riproduce un SFX di fart casuale per lo scatto!
        int randFart = new java.util.Random().nextInt(3) + 1;
        IscatAudioManager.getInstance().playSFX("fart_alt" + randFart);
    }

    public boolean isScattoDisponibile() { return dashCooldown.isReady(); }
    public boolean isInScatto() { return dashDuration.isCoolingDown(); }
    public boolean isSparoDisponibile() { return weaponCooldown.isReady(); }

    public void startCooldownFuoco() {
        weaponCooldown.start(PlayerSettings.COOLDOWN_FUOCO_SEC);
    }

    /** Managed safe retrieval of current cooldown fraction for visual interface bars */
    public double getDashMeter() {
        return dashCooldown.getProgress();
    }

    @Override
    public double getTerminalVelocity() {
        return PlayerSettings.VELOCITA_MAX;
    }

    @Override
    public Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean hasAmmo() {
        return true;
    }

    @Override
    public Cooldown projectileCooldown() {
        return weaponCooldown;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return (int) UU.sToTicks(PlayerSettings.COOLDOWN_FUOCO_SEC);
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        this.weaponCooldown.start(UU.ticksToS(tickCount));
    }

    @Override
    public void onDeath() {
        // Managed downstream via spatial listeners
    }
}