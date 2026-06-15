package uni.gaben.iscat.universe.entity.hardcoded.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;

import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityModel;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.interfaces.*;
import uni.gaben.iscat.universe.rendering.RenderingSettings;
import uni.gaben.iscat.universe.Thrust;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionScoreTracker;

public class PlayerModel extends AbstractLivingEntityModel implements HasSprite, HasThrust, HasDash,Stunnable, Progression {

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

    private final EntityRecord data;
    private final Vector2 dashDir = UU.vector2zero();

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    public PlayerModel(double x, double y, EntityRecord data) {
        super(x, y, data);
        this.data = data;

        // Usiamo i dati dinamici dal JSON al posto di PlayerSettings
        double radiusInMeters = UU.pxToM(data.frameW() / 2.5); // o usa un valore specifico
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusInMeters*data.scale()));
        fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);

        setMass(MassType.NORMAL);

        setLinearDamping(data.linearDamping());

        thrust = new Thrust();
    }

    @Override
    public void update(double dt) {
        dashCooldown.update(dt);
        dashDuration.update(dt);
        weaponCooldown.update(dt);
        updateThrust();
        updateStateTime(dt);

        if (data.player() != null) {
            setLinearDamping(isDashing() ? 0.1 : data.linearDamping());
        }
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

    @Override
    public void dashTowards(double angle) {
        if (data.player() == null) return;

        dashDir.set(Math.cos(angle), Math.sin(angle));
        if (getLinearVelocity().dot(dashDir) < 0) {
            setLinearVelocity(0, 0);
        }

        applyImpulse(dashDir.setMagnitude(data.player().dashImpulse() * getMass().getMass()));
        dashDuration.start(data.player().dashDurationSec());
        dashCooldown.start(data.player().dashCooldownSec());
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

    @Override
    public boolean canDash() { return dashCooldown.isReady(); }

    @Override
    public boolean isDashing() { return dashDuration.isCoolingDown(); }
    public boolean isSparoDisponibile() { return weaponCooldown.isReady(); }

    public void startCooldownFuoco() {
        weaponCooldown.start(PlayerSettings.COOLDOWN_FUOCO_SEC);
    }

    public void setCooldownFuocoSec(double cooldown) {
        PlayerSettings.COOLDOWN_FUOCO_SEC = cooldown;
    }

    @Override
    public double getTerminalVelocity() {
        return PlayerSettings.VELOCITA_MAX * (isDashing() ? 10 : 1);
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

    @Override
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

    @Override
    public Thrust thrust() {
        return thrust;
    }

    @Override
    public String getSpritePath() {
        return data.spritePath();
    }

    @Override
    public int getSpriteFrameWidth() {
        return data.frameW();
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public int getSpriteFrameHeight() {
        return data.frameH();
    }

    @Override
    public double getVisualScale() {
        return data.scale();
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 180;
    }
}