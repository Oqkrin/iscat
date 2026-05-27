package uni.gaben.iscat.universe.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.universe.enemies.bomber.IscatBomberSettings;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;
public class PlayerModel extends LivingEntityModel implements HasProjectile<Projectile>, Updatable {

    // LEVEL SYSTEM VARIABLES
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final DoubleProperty xp = new SimpleDoubleProperty(0);
    private double xpNeeded = PlayerSettings.XP_BASE_NECESSARIA;

    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Cooldown weaponCooldown = new Cooldown();
    private final Cooldown stunCooldown = new Cooldown();

    private Runnable onDeathCallback;

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    private final Projectile projectile = new Projectile(ProjectileType.PLAYER_BULLET);

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


    }

    // LEVEL SYSTEM GETTERS
    public IntegerProperty levelProperty() { return level; }
    public int getLevel() { return level.get(); }

    public DoubleProperty xpProperty() { return xp; }
    public double getXp() { return xp.get(); }
    public double getXpNeeded() { return xpNeeded; }

    public boolean isScattoDisponibile() { return dashCooldown.isReady(); }
    public boolean isInScatto() { return dashDuration.isCoolingDown(); }
    public boolean isSparoDisponibile() { return weaponCooldown.isReady(); }

    public void startCooldownFuoco() {
        weaponCooldown.start(PlayerSettings.COOLDOWN_FUOCO_SEC);
    }

    public void setCooldownFuocoSec(double new_value){
        PlayerSettings.COOLDOWN_FUOCO_SEC = new_value;
    }

    /** Managed safe retrieval of current cooldown fraction for visual interface bars */
    public double getDashMeter() {
        return dashCooldown.getProgress();
    }

    @Override
    public double getTerminalVelocity() {
        return PlayerSettings.VELOCITA_MAX * (isInScatto() ? 10 : 1);
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
        if (onDeathCallback != null) onDeathCallback.run();
    }

    /**
     * Aggiunge XP al giocatore e gestisce l'eventuale Level Up a catena.
     */
    public void addXp(double amount) {
        if (amount <= 0) return;

        this.xp.set(this.xp.get() + amount);

        if (this.xpNeeded <= 0) {
            this.xpNeeded = 100.0;
        }

        // Loop nel caso in cui l'XP ricevuta sia talmente tanta da fare più di un livello
        while (this.xp.get() >= xpNeeded) {
            levelUp();
        }
    }

    private void levelUp() {
        this.xp.set(this.xp.get() - xpNeeded);
        this.level.set(this.level.get() + 1);
        this.xpNeeded = this.xpNeeded * 1.2;

        AudioManager.getInstance().playSFX("levelup");

        // Aumento di statistiche + cura totale
        setMaxLife(getMaxLife() + 100);
        setLife(getMaxLife());

        System.out.println("[LEVEL UP] Nuovo Livello: " + getLevel() + " | Prossimo livello a: " + this.xpNeeded + " XP");
    }

    public void applyStun(double duration) {
        stunCooldown.start(duration);
    }

    public boolean notStunned() {
        return stunCooldown.isReady();
    }
}