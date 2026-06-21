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
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.*;
import uni.gaben.iscat.universe.rendering.RenderingSettings;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.utils.audio.AudioManager;
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

    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final DoubleProperty xp = new SimpleDoubleProperty(0);
    private double xpNeeded;

    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Cooldown weaponCooldown = new Cooldown();
    private final Cooldown stunCooldown = new Cooldown();
    private final Cooldown meleeCooldown = new Cooldown();
    private final Cooldown quickDashCooldown = new Cooldown();
    private final Cooldown postDashProtection = new Cooldown();

    private double timeGauge = 0.0;
    private double maxTimeGauge = 100.0;
    private boolean wasDashing = false;

    private final Thrust thrust;
    private Runnable onDeathCallback;

    private double meleeDamage;
    private double meleeCooldownSec;
    private double currentWeaponCooldown;
    private double dannoProiettile;

    private final EntityRecord data;
    private final Vector2 dashDir = UU.vector2zero();
    private final Vector2 quickDashDir = UU.vector2zero();

    private double idleAudioTimer = 8.0 + Math.random() * 10.0;

    /**
     * Inizializza il giocatore impostando le statistiche di base e la fixture fisica circolare.
     */
    public PlayerModel(double x, double y, EntityRecord data) {
        super(x, y, data);
        this.data = data;

        this.xpNeeded = data.player().baseXPNeeded();
        this.meleeDamage = data.player().meleeDamage();
        this.meleeCooldownSec = data.player().meleeCooldownSec();
        this.currentWeaponCooldown = data.player().baseCooldownSec();
        this.dannoProiettile = data.player().dannoProiettile();

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

    /**
     * Esegue uno scatto rapido e immediato nella direzione dell'angolo specificato.
     */
    public void quickDash(double angle) {
        if (!quickDashCooldown.isReady() || isStunned() || data.player() == null) {
            return;
        }

        double impulse = data.player().dashImpulse() * 3;
        quickDashDir.set(Math.cos(angle), Math.sin(angle));

        if (getLinearVelocity().dot(quickDashDir) < 0) {
            setLinearVelocity(0, 0);
        }

        applyImpulse(quickDashDir.setMagnitude(impulse * getMass().getMass()));
        quickDashCooldown.start(0.2);
    }

    /**
     * Esegue il dash standard attivando l'invulnerabilità e i relativi cooldown.
     */
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
     * Aggiorna l'intensità e l'effetto visivo del motore di spinta (Thrust).
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

    /**
     * Aggiorna lo stato temporale, i cooldown dei sistemi, l'audio e le logiche di protezione post-dash.
     */
    @Override
    public void update(double dt) {
        dashCooldown.update(dt);
        dashDuration.update(dt);
        stunCooldown.update(dt);
        weaponCooldown.update(dt);
        quickDashCooldown.update(dt);
        meleeCooldown.update(dt);

        updateThrust();
        updateStateTime(dt);

        if (!isStunned() && !isDashing()) {
            idleAudioTimer -= dt;
            if (idleAudioTimer <= 0) {
                uni.gaben.iscat.utils.EntityAudioManager.playEventAudio(this, "idle");
                idleAudioTimer = 8.0 + Math.random() * 14.0;
            }
        }

        boolean currentlyDashing = isDashing();
        if (data.player() != null) {
            setLinearDamping(currentlyDashing ? 0.0 : data.linearDamping());
        }

        if (wasDashing && !currentlyDashing) {
            postDashProtection.start(2.0);
        }
        wasDashing = currentlyDashing;
        postDashProtection.update(dt);
    }

    /**
     * Avanza il livello del giocatore, incrementa i punti vita massimi e resetta la vita attuale.
     */
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

    /**
     * Aggiunge punti esperienza al giocatore e gestisce i passaggi di livello multipli.
     */
    @Override
    public void incrementExperience(double amount) {
        if (amount <= 0) return;

        setExperience(this.xp.get() + amount);

        while (this.xp.get() >= xpNeeded) {
            levelUp();
        }
    }

    /**
     * Gestisce il danno corpo a corpo e applica un forte contraccolpo (knockback) se il giocatore sta dashando.
     */
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

    /**
     * Attiva il cooldown dell'arma principale dopo uno sparo e resetta l'audio di idle.
     */
    public void startCooldownFuoco() {
        weaponCooldown.start(currentWeaponCooldown);
        this.idleAudioTimer = 8.0 + Math.random() * 14.0;
    }

    /**
     * Imposta il tempo di ricarica in secondi dell'arma principale.
     */
    public void setCooldownFuocoSec(double cooldown) {
        this.currentWeaponCooldown = cooldown;
    }

    /**
     * Verifica se il giocatore è pronto a infliggere danni corpo a corpo.
     */
    public boolean canDealMeleeDamage() {
        return meleeDamage > 0 && meleeCooldown.isReady();
    }

    /**
     * Calcola il danno corpo a corpo scalato in base al livello attuale.
     */
    public double getMeleeDamage() {
        return meleeDamage * getLevel();
    }

    /**
     * Avvia il timer di ricarica per l'attacco corpo a corpo.
     */
    public void startMeleeCooldown() {
        if (meleeDamage > 0) {
            meleeCooldown.start(meleeCooldownSec);
        }
    }

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    /**
     * Callback eseguito alla morte del giocatore per notificare i sistemi esterni.
     */
    @Override
    public void onDeath() {
        if (onDeathCallback != null) {
            onDeathCallback.run();
        }
    }

    /**
     * Modifica l'endurance ed azzera la barra dell'indicatore temporale se si subisce danno fuori dalla protezione.
     */
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

    /**
     * Assorbe il proiettile nemico convertendo il danno in energia per l'indicatore solo se eseguito durante un dash.
     */
    public boolean absorbProjectile(double damage) {
        if (isDashing()) {
            addTimeGauge(damage * 10.0);
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

    /**
     * Calcola il danno dei proiettili del giocatore scalato in base al livello attuale.
     */
    public double getProjectileDamage() {
        if (getLevel() > 1)
            return dannoProiettile + ((getLevel() - 1) * 25.0);
        return dannoProiettile;
    }

    public IntegerProperty levelProperty() {
        return level;
    }

    public DoubleProperty xpProperty() {
        return xp;
    }

    @Override
    public int getLevel() {
        return level.get();
    }

    @Override
    public void setExperience(double experience) {
        xp.set(experience);
    }

    @Override
    public boolean canDash() {
        return dashCooldown.isReady();
    }

    @Override
    public boolean isDashing() {
        return dashDuration.isCoolingDown();
    }

    /**
     * Verifica se l'arma principale è pronta per fare fuoco.
     */
    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    @Override
    public boolean isStunned() {
        return stunCooldown.isCoolingDown();
    }

    /**
     * Rende il giocatore immune alle alterazioni di stato/salute durante il dash.
     */
    @Override
    public boolean isInalterable() {
        return isDashing();
    }

    /**
     * Calcola il limite di velocità massima incrementandolo drasticamente se l'entità è in fase di dash.
     */
    @Override
    public double getTerminalVelocity() {
        return data.maxVelocity() * (isDashing() ? 10 : 1);
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
    public int getSpriteFrameHeight() {
        return data.frameH();
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 180;
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK * 6;
    }

    /**
     * Applica uno stordimento al giocatore bloccando le azioni per la durata in secondi specificata.
     */
    @Override
    public void stun(double ms) {
        stunCooldown.start(ms);
    }
}