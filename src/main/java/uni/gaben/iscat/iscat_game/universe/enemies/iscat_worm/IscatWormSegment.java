package uni.gaben.iscat.iscat_game.universe.enemies.iscat_worm;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

/**
 * Segmento del verme (Head, Body, Tail).
 * Implementa HasProjectile per supportare la fase di fuoco della Coda.
 */
public class IscatWormSegment extends LivingEntityModel implements HasProjectile {

    public enum Type { HEAD, BODY, TAIL }

    private Type type;
    private boolean consumed = false;
    private IscatWormSegment previousSegment;

    private final Cooldown attackCooldown = new Cooldown();

    // ── CAMPI AGGIUNTI PER INTERFACCIA HASPROJECTILE ──────────────────────────
    private final Projectile bulletTemplate;
    private final Cooldown tailFireCooldown = new Cooldown();
    private int projectileTickCount = 0;

    public IscatWormSegment(Type type, double x, double y) {
        super(x, y, getHp(type), getHp(type));
        this.type = type;
        setXpReward(IscatWormSettings.XP_REWARD);

        // Inizializzazione nativa del proiettile di gioco
        this.bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(getRadius(type))));

        // --- GESTIONE COLLISIONI STRUTTURALI ---
        if (type == Type.HEAD) {
            fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        } else {
            fixture.setFilter(UniverseCollisionLayers.WORM_BODY_FILTER);
        }

        setMass(MassType.NORMAL);
        setLinearDamping(getDamping(type));
        setAngularDamping(0.0); // Azzera l'attrito rotazionale per curve fulminee

        this.setOnCollision(other -> {
            if (other instanceof uni.gaben.iscat.iscat_game.universe.player.PlayerModel player) {
                if (this.type == Type.HEAD) {
                    if (this.canAttack()) {
                        Vector2 vel = this.getLinearVelocity();
                        if (vel.getMagnitude() > IscatWormSettings.HEAD_MAX_SPEED * 1.5) {
                            player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER * 1.5); // Plunge attack
                        } else {
                            player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER);
                        }
                        this.startAttackCooldown();
                    }
                } else {
                    player.deltaToLife(-2.0); // Minor contact damage from body/tail
                }
            }
        });
    }

    // ── IMPLEMENTAZIONE RIGIDA DI HASPROJECTILE ───────────────────────────────

    @Override
    public AbstractProjectileModel getProjectile() {
        return this.type == Type.TAIL ? this.bulletTemplate : null;
    }

    @Override
    public boolean hasAmmo() {
        return this.type == Type.TAIL && !consumed;
    }

    @Override
    public Cooldown projectileCooldown() {
        return this.tailFireCooldown;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return this.projectileTickCount;
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        this.projectileTickCount = tickCount;
    }

    // ── LOGICA DI GESTIONE INTERNA ────────────────────────────────────────────

    @Override
    public void onDeath() {
        super.onDeath();
        consume();
    }

    public void promoteToHead() {
        this.type = Type.HEAD;
        if (!getFixtures().isEmpty()) {
            getFixtures().get(0).setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        }
    }

    public void updateCooldowns(double dt) {
        attackCooldown.update(dt);
        if (type == Type.TAIL) {
            tailFireCooldown.update(dt);
        }
    }

    public boolean canAttack()                   { return !attackCooldown.isCoolingDown(); }
    public void startAttackCooldown()            { attackCooldown.start(IscatWormSettings.HEAD_ATTACK_COOLDOWN); }

    // ── GETTERS / SETTERS ─────────────────────────────────────────────────────
    public Type              getType()           { return type; }
    public boolean           isConsumed()        { return consumed; }
    public void              consume()           { this.consumed = true; setShouldRemove(true); }
    public IscatWormSegment  getPreviousSegment(){ return previousSegment; }
    public void              setPreviousSegment(IscatWormSegment p) { this.previousSegment = p; }

    public Vector2 getPosition()              { return getTransform().getTranslation(); }
    public void    setRotation(double angle)  { getTransform().setRotation(angle); }
    public double  getRotation()              { return getTransform().getRotationAngle(); }

    private static int    getHp(Type t)     { return switch(t){ case HEAD->IscatWormSettings.HEAD_HP; case BODY->IscatWormSettings.BODY_HP; case TAIL->IscatWormSettings.TAIL_HP; }; }
    private static double getRadius(Type t) { return switch(t){ case HEAD->IscatWormSettings.HEAD_SCALE; case BODY->IscatWormSettings.BODY_SCALE; case TAIL->IscatWormSettings.TAIL_SCALE; } * IscatWormSettings.DIM_SPRITE / 2 * 0.85; }
    private static double getDamping(Type t){ return switch(t){ case HEAD->2.8; case BODY->4.2; case TAIL->5.5; }; }
}