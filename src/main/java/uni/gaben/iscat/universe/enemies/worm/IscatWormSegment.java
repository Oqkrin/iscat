package uni.gaben.iscat.universe.enemies.worm;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

/**
 * Segmento del verme (Head, Body, Tail).
 * Ottimizzato nella gestione della memoria e corretto nelle transizioni di stato fisiche.
 */
public class IscatWormSegment extends LivingEntityModel implements HasProjectile, Updatable {

    public enum Type { HEAD, BODY, TAIL }

    private Type type;
    private boolean consumed = false;
    private IscatWormSegment previousSegment;

    private final Cooldown attackCooldown = new Cooldown();

    private final Projectile bulletTemplate;
    private final Cooldown tailFireCooldown = new Cooldown();
    private int projectileTickCount = 0;

    public IscatWormSegment(Type type, double x, double y) {
        super(x, y, getHp(type), getHp(type));
        this.type = type;
        setXpReward(IscatWormSettings.XP_REWARD);

        this.bulletTemplate = (type == Type.TAIL) ? new Projectile(ProjectileType.ENEMY_BULLET) : null;

        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(getRadius(type))));

        if (type == Type.HEAD) {
            fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        } else {
            fixture.setFilter(UniverseCollisionLayers.WORM_BODY_FILTER);
        }

        setMass(MassType.NORMAL);
        setLinearDamping(getDamping(type));
        setAngularDamping(0.0); // Curve fulminee

        this.setOnCollision(other -> {
            if (other instanceof uni.gaben.iscat.universe.player.PlayerModel player) {
                if (this.type == Type.HEAD) {
                    if (this.canAttack()) {
                        Vector2 vel = this.getLinearVelocity();
                        if (vel.getMagnitude() > IscatWormSettings.HEAD_MAX_SPEED * IscatWormSettings.PLUNGE_THRESHOLD_MULT) {
                            player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER * IscatWormSettings.PLUNGE_DAMAGE_MULT);
                        } else {
                            player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER);
                        }
                        this.startAttackCooldown();
                    }
                } else {
                    player.deltaToLife(-IscatWormSettings.BODY_CONTACT_DAMAGE);
                }
            }
        });
    }

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

    @Override
    public void update(double dt) {
        updateStateTime(dt);
        updateCooldowns(dt);
    }

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
        setLinearDamping(getDamping(Type.HEAD));
    }

    public void updateCooldowns(double dt) {
        attackCooldown.update(dt);
        if (type == Type.TAIL) {
            tailFireCooldown.update(dt);
        }
    }

    public boolean canAttack()                   { return !attackCooldown.isCoolingDown(); }
    public void startAttackCooldown()            { attackCooldown.start(IscatWormSettings.HEAD_ATTACK_COOLDOWN); }

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