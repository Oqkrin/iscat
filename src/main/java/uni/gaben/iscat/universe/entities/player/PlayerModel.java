package uni.gaben.iscat.universe.entities.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.*;
import uni.gaben.iscat.universe.rendering.RenderingSettings;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionScoreTracker;

/**
 * Modello del giocatore che gestisce movimento, progressione, dash e combattimento.
 * Implementa diverse interfacce per abilitare comportamenti modulari.
 */
public class PlayerModel extends AbstractLivingEntityModel
        implements HasSprite, HasThrust, HasDash, Stunnable, Progression {

    // ==================== PROPRIETÀ DI PROGRESSIONE ====================
    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final DoubleProperty xp = new SimpleDoubleProperty(0);
    private double xpNeeded;

    // ==================== COOLDOWN SYSTEMS ====================
    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Cooldown weaponCooldown = new Cooldown();
    private final Cooldown stunCooldown = new Cooldown();
    private final Cooldown meleeCooldown = new Cooldown();
    private final Cooldown quickDashCooldown = new Cooldown();
    private final Cooldown postDashProtection = new Cooldown();

    // ==================== TIME GAUGE ====================
    private double timeGauge = 0.0;
    private double maxTimeGauge = 100.0;
    private boolean wasDashing = false;

    // ==================== THRUST E CALLBACK ====================
    private final Thrust thrust;
    private Runnable onDeathCallback;

    // ==================== STATISTICHE DI COMBATTIMENTO ====================
    private double meleeDamage;
    private double meleeCooldownSec;
    private double currentWeaponCooldown;
    private double dannoProiettile;

    // ==================== DATI E VETTORI DI MOVIMENTO ====================
    private final EntityRecord data;
    private final Vector2 dashDir = UU.vector2zero();
    private final Vector2 quickDashDir = UU.vector2zero();

    // === IDLE ====
    private double idleAudioTimer = 8.0 + Math.random() * 10.0;

    // ==================== COSTRUTTORE ====================
    public PlayerModel(double x, double y, EntityRecord data) {
        super(x, y, data);
        this.data = data;

        // Inizializzazione dai dati del record
        this.xpNeeded = data.player().baseXPNeeded();
        this.meleeDamage = data.player().meleeDamage();
        this.meleeCooldownSec = data.player().meleeCooldownSec();
        this.currentWeaponCooldown = data.player().baseCooldownSec();
        this.dannoProiettile = data.player().dannoProiettile();

        // Configurazione fisica del corpo
        double radiusInMeters = UU.pxToM(data.frameW() / 2.5);
        BodyFixture fixture = addFixture(
                Geometry.createCircle(radiusInMeters * data.scale())
        );
        fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);

        setMass(MassType.NORMAL);
        setLinearDamping(data.linearDamping());
        this.thrust = new Thrust();

        addOnCollision("melee", other -> {
            if (other instanceof AbstractLivingEntityModel enemy && !(other instanceof AbstractPhysicalProjectileModel)) {
                handleMeleeCollision(enemy);
            }
        });

    }

    // ==================== METODI DI MOVIMENTO ====================

    /**
     * Esegue un dash rapido nella direzione specificata.
     * @param angle Angolo in radianti della direzione del dash
     */
    public void quickDash(double angle) {
        if (!quickDashCooldown.isReady() || isStunned() || data.player() == null) {
            return;
        }

        double impulse = data.player().dashImpulse() * 3;
        quickDashDir.set(Math.cos(angle), Math.sin(angle));

        // Annulla la velocità se si sta muovendo in direzione opposta
        if (getLinearVelocity().dot(quickDashDir) < 0) {
            setLinearVelocity(0, 0);
        }

        applyImpulse(quickDashDir.setMagnitude(impulse * getMass().getMass()));
        quickDashCooldown.start(0.2);
    }

    @Override
    public void dashTowards(double angle) {
        if (data.player() == null) {
            return;
        }

        dashDir.set(Math.cos(angle), Math.sin(angle));

        if (getLinearVelocity().dot(dashDir) < 0) {
            setLinearVelocity(0, 0);
        }

        applyImpulse(dashDir.setMagnitude(
                data.player().dashImpulse() * getMass().getMass()
        ));

        dashDuration.start(data.player().dashDurationSec());
        dashCooldown.start(data.player().dashCooldownSec());
    }

    /**
     * Aggiorna lo stato del thrust in base alla velocità attuale.
     */
    public void updateThrust() {
        Vector2 worldVel = getLinearVelocity();
        double speed = worldVel.getMagnitude();
        double maxSpeed = data.player().baseSpeed();
        double intensity = Math.min(speed / maxSpeed, 1.0);

        double normVx = worldVel.x / maxSpeed;
        double normVy = worldVel.y / maxSpeed;

        double rotRad = getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET;
        double cos = Math.cos(rotRad);
        double sin = Math.sin(rotRad);

        double localDriftX = -normVx * cos - normVy * sin;
        double localDriftY =  normVx * sin - normVy * cos;

        thrust.update(intensity,
                new Vector2(localDriftX, localDriftY),
                getWidthPx(),
                getHeightPx());
    }

    // ==================== METODI DI AGGIORNAMENTO ====================

    @Override
    public void update(double dt) {
        // Aggiorna tutti i cooldown
        dashCooldown.update(dt);
        dashDuration.update(dt);
        stunCooldown.update(dt);
        weaponCooldown.update(dt);
        quickDashCooldown.update(dt);
        meleeCooldown.update(dt);

        updateThrust();
        updateStateTime(dt);

        // LOGICA AUDIO IDLE
        if (!isStunned() && !isDashing()) {
            idleAudioTimer -= dt;
            if (idleAudioTimer <= 0) {
                uni.gaben.iscat.utils.EntityAudioManager.playEventAudio(this, "idle");
                // Imposta un intervallo casuale per il prossimo suono (es. tra 8 e 22 secondi)
                idleAudioTimer = 8.0 + Math.random() * 14.0;
            }
        }

        // Gestisce lo smorzamento durante il dash
        boolean currentlyDashing = isDashing();
        if (data.player() != null) {
            setLinearDamping(currentlyDashing ? 0.0 : data.linearDamping());
        }

        // Post-dash protection logic
        if (wasDashing && !currentlyDashing) {
            postDashProtection.start(2.0); // 2.0 seconds protection
        }
        wasDashing = currentlyDashing;
        postDashProtection.update(dt);
    }

    // ==================== METODI DI PROGRESSIONE ====================

    @Override
    public void levelUp() {
        this.xp.set(this.xp.get() - xpNeeded);
        this.level.set(this.level.get() + 1);
        this.xpNeeded = this.xpNeeded * 1.2;

        AudioManager.getInstance().playSFX("levelup");
        setMaxEndurance(getMaxEndurance() + 100);
        setEndurance(getMaxEndurance());
        applyImpulse(new Vector2(0, 0));
    }

    @Override
    public void incrementExperience(double amount) {
        if (amount <= 0) return;

        setExperience(this.xp.get() + amount);

        while (this.xp.get() >= xpNeeded) {
            levelUp();
        }
    }

    public void handleMeleeCollision(AbstractLivingEntityModel enemy) {
        if (!canDealMeleeDamage() || enemy == null) return;

        double damage = getMeleeDamage();

        if (isDashing()) {
            Vector2 knockbackDir = enemy.getTransform().getTranslation()
                    .copy()
                    .subtract(this.getTransform().getTranslation());

            double distance = knockbackDir.getMagnitude();
            if (distance > 0.001) {
                knockbackDir.multiply(1.0 / distance);

                double knockbackMagnitude = 75.0 * enemy.getMass().getMass();
                Vector2 impulse = knockbackDir.product(knockbackMagnitude);

                enemy.setLinearVelocity(0, 0);

                enemy.applyImpulse(impulse);
            }
        }

        enemy.setMeleeAttacker(this);

        if (enemy.getEndurance() - damage <= 0) {
            enemy.setKilledByMeele(true);
            enemy.setKilledByProjectile(true);
        }

        enemy.alter(-damage);

        if (enemy.getEndurance() <= 0) {
            this.incrementExperience(enemy.getXpReward());
            SessionScoreTracker.getInstance().addKill();
        }

        startMeleeCooldown();
    }


    public void startCooldownFuoco() {
        weaponCooldown.start(currentWeaponCooldown);
        // Resettiamo il timer quando spari
        this.idleAudioTimer = 8.0 + Math.random() * 14.0;
    }

    public void setCooldownFuocoSec(double cooldown) {
        this.currentWeaponCooldown = cooldown;
    }

    public boolean canDealMeleeDamage() {
        return meleeDamage > 0 && meleeCooldown.isReady();
    }

    public double getMeleeDamage() {
        return meleeDamage * getLevel();
    }

    public void startMeleeCooldown() {
        if (meleeDamage > 0) {
            meleeCooldown.start(meleeCooldownSec);
        }
    }

    // ==================== GETTER E SETTER ====================

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    @Override
    public void onDeath() {
        if (onDeathCallback != null) {
            onDeathCallback.run();
        }
    }

    @Override
    public void alter(double delta) {
        super.alter(delta);
        if (delta < 0) {
            SessionScoreTracker.getInstance()
                    .addDamageReceived((int) Math.abs(delta));
            if (!postDashProtection.isCoolingDown()) {
                resetTimeGauge();
            }
        }
    }

    public boolean absorbProjectile(double damage) {
        if (isDashing()) {
            addTimeGauge(damage * 10.0); // Scale damage to gauge
            return true;
        }
        return false;
    }

    public void addTimeGauge(double amount) {
        timeGauge = Math.min(timeGauge + amount, maxTimeGauge);
    }

    public void decreaseTimeGauge(double amount) {
        timeGauge = Math.max(timeGauge - amount, 0.0);
    }

    public void resetTimeGauge() {
        timeGauge = 0.0;
    }

    public double getTimeGauge() {
        return timeGauge;
    }

    public double getMaxTimeGauge() {
        return maxTimeGauge;
    }

    @Override
    public void setLevel(int level) {
        for (int i = getLevel(); i <= level; i++) {
            levelUp();
        }
    }

    public double getProjectileDamage() {
        if (getLevel() > 1)
            return dannoProiettile + ((getLevel() - 1) * 25.0);
        return dannoProiettile;
    }

    // ==================== PROPERTIES JAVAFX ====================

    public IntegerProperty levelProperty() {
        return level;
    }

    public DoubleProperty xpProperty() {
        return xp;
    }

    // ==================== GETTER DI STATO ====================

    @Override
    public int getLevel() {
        return level.get();
    }

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
    public boolean canDash() {
        return dashCooldown.isReady();
    }

    @Override
    public boolean isDashing() {
        return dashDuration.isCoolingDown();
    }

    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    @Override
    public boolean isStunned() {
        return stunCooldown.isCoolingDown();
    }

    @Override
    public boolean isInalterable() {
        return isDashing();
    }

    @Override
    public double getTerminalVelocity() {
        return data.maxVelocity() * (isDashing() ? 10 : 1);
    }

    // ==================== GETTER VISUALI ====================

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

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return UU.UNIVERSE_TICK * 6;
    }

    @Override
    public void stun(double ms) {
        stunCooldown.start(ms);
    }
}