package uni.gaben.iscat.universe.entity.hardcoded.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityRecordBuilder;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;
import uni.gaben.iscat.universe.entity.interfaces.HasThrust;
import uni.gaben.iscat.universe.entity.interfaces.Progression;
import uni.gaben.iscat.universe.entity.interfaces.Stunnable;
import uni.gaben.iscat.universe.rendering.RenderingSettings;
import uni.gaben.iscat.universe.Thrust;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionScoreTracker;

public class PlayerModel extends AbstractLivingEntityModel implements HasSprite, HasThrust, Stunnable, Progression {

    // LEVEL SYSTEM VARIABLES
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final DoubleProperty xp = new SimpleDoubleProperty(0);
    private double xpNeeded = PlayerSettings.XP_BASE_NECESSARIA;

    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Cooldown weaponCooldown = new Cooldown();
    private final Cooldown stunCooldown = new Cooldown();
    private final Thrust thrust;
    private Runnable onDeathCallback;

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    public PlayerModel(double x, double y) {
        super(x, y, new EntityRecordBuilder().build());

        double radiusInMeters = UU.pxToM(PlayerSettings.RAGGIO_COLLISIONE);
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusInMeters));
        fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(PlayerSettings.LINEAR_DAMPING);

        thrust = new Thrust();
    }

    public void update(double dt) {
        dashCooldown.update(dt);
        dashDuration.update(dt);
        weaponCooldown.update(dt);
        updateThrust();
        updateStateTime(dt);
        setLinearDamping(isInScatto() ? PlayerSettings.LINEAR_DAMPING_SCATTO : PlayerSettings.LINEAR_DAMPING);
    }

    public void updateThrust() {
        Vector2 worldVel = getLinearVelocity();
        double speed = worldVel.getMagnitude();
        double intensity = Math.min(speed / PlayerSettings.VELOCITA_MAX, 1.0);

        double normVx = worldVel.x / PlayerSettings.VELOCITA_MAX;
        double normVy = worldVel.y / PlayerSettings.VELOCITA_MAX;

        double rotRad = getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET;
        double cos = Math.cos(rotRad);
        double sin = Math.sin(rotRad);

        double localDriftX = -normVx * cos - normVy * sin;
        double localDriftY =  normVx * sin - normVy * cos;

        thrust.update(intensity, new Vector2(localDriftX, localDriftY),
                getWidthPx(), getHeightPx());
    }

    public void executeScatto(double angle) {
        Vector2 dashDir = new Vector2(Math.cos(angle), Math.sin(angle));
        if (getLinearVelocity().dot(dashDir) < 0) {
            setLinearVelocity(new Vector2(0, 0));
        }
        applyImpulse(dashDir.multiply(PlayerSettings.IMPULSO_SCATTO * getMass().getMass()));
        dashDuration.start(PlayerSettings.DURATA_SCATTO_SEC);
        dashCooldown.start(PlayerSettings.COOLDOWN_SCATTO_SEC);
    }

    // LEVEL SYSTEM GETTERS
    public IntegerProperty levelProperty() { return level; }
    @Override
    public int getLevel() { return level.get(); }

    public DoubleProperty xpProperty() { return xp; }

    @Override
    public double getExperience() {
        return xp.get();
    }

    @Override
    public void setExperience(double experience) {
        xp.set(experience);
    }

    @Override
    public double getNeededXpFor(int level) {
        return xpNeeded;
    }
    public boolean isScattoDisponibile() { return dashCooldown.isReady(); }
    public boolean isInScatto() { return dashDuration.isCoolingDown(); }
    public boolean isSparoDisponibile() { return weaponCooldown.isReady(); }

    public void startCooldownFuoco() {
        weaponCooldown.start(PlayerSettings.COOLDOWN_FUOCO_SEC);
    }

    public void setCooldownFuocoSec(double new_value) {
        PlayerSettings.COOLDOWN_FUOCO_SEC = new_value;
    }

    public double getDashMeter() {
        return dashCooldown.getProgress();
    }

    @Override
    public double getTerminalVelocity() {
        return PlayerSettings.VELOCITA_MAX * (isInScatto() ? 10 : 1);
    }

    @Override
    public void onDeath() {
        if (onDeathCallback != null) onDeathCallback.run();
    }

    @Override
    public void incrementExperience(double amount) {
        if (amount <= 0) return;
        setExperience(this.xp.get() + amount);
        if (this.xpNeeded <= 0) this.xpNeeded = 100.0;
        while (this.xp.get() >= xpNeeded) {
            levelUp();
        }
    }

     public void levelUp() {
        this.xp.set(this.xp.get() - xpNeeded);
        this.level.set(this.level.get() + 1);
        this.xpNeeded = this.xpNeeded * 1.2;

        AudioManager.getInstance().playSFX("levelup");
        setMaxEndurance(getMaxEndurance() + 100);
        setEndurance(getMaxEndurance());
        System.out.println("[LEVEL UP] New Level: " + getLevel() + " | Next: " + this.xpNeeded + " XP");
        applyImpulse(new Vector2(0, 0));
    }

    @Override
    public void setLevel(int level) {
        for (int i = getLevel(); i<=level; i++) {
            levelUp();
        }
    }

    @Override
    public void stun(double duration) {
        stunCooldown.start(duration);
    }

    @Override
    public boolean isStunned() {
        return stunCooldown.isCoolingDown();
    }

    @Override
    public void alter(double delta) {
        super.alter(delta);
        if (delta < 0) {
            SessionScoreTracker.getInstance().addDamageReceived((int) Math.abs(delta));
        }
    }

    // ---- HasSprite implementation ----
    @Override
    public String getSpritePath() {
        return PlayerSettings.getPlayerSkin();
    }

    @Override
    public int getSpriteFrameWidth() {
        return (int) PlayerSettings.DIMENSIONE_DA_DISEGNARE;
    }

    @Override
    public int getSpriteFrameHeight() {
        return (int) PlayerSettings.DIMENSIONE_DA_DISEGNARE;
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return getFrameDuration();
    }

    @Override
    public double getVisualScale() {
        return PlayerSettings.MASSA;
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 180;
    }

    @Override
    public Thrust thrust() {
        return thrust;
    }
}