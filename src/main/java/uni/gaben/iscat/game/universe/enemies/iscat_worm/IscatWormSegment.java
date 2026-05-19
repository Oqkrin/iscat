package uni.gaben.iscat.game.universe.enemies.iscat_worm;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.utils.Cooldown;

/**
 * Segmento del verme (Head, Body, Tail)
 */
public class IscatWormSegment extends LivingEntityModel {

    public enum Type { HEAD, BODY, TAIL }

    private Type type; // non final: permette promozione a HEAD
    private boolean consumed = false;
    private IscatWormSegment previousSegment;
    private final Cooldown attackCooldown = new Cooldown();

    public IscatWormSegment(Type type, double x, double y) {
        super(x, y, getHp(type), getHp(type));
        this.type = type;

        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(getRadius(type))));
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);

        setLinearDamping(getDamping(type));
        // FIX: Rimuove l'attrito angolare per permettere virate istantanee e fulminee
        setAngularDamping(0.0);
    }

    @Override
    public void onDeath() {
        super.onDeath();
        consume();
    }

    // ── Promozione ────────────────────────────────────────────────────────────
    public void promoteToHead() { this.type = Type.HEAD; }

    // ── Cooldown (aggiornato dal controller ogni tick) ────────────────────────
    public void updateCooldowns(double dt)       { attackCooldown.update(dt); }
    public boolean canAttack()                   { return !attackCooldown.isCoolingDown(); }
    public void startAttackCooldown()            { attackCooldown.start(IscatWormSettings.HEAD_ATTACK_COOLDOWN); }

    // ── Getters / setters ─────────────────────────────────────────────────────
    public Type              getType()           { return type; }
    public boolean           isConsumed()        { return consumed; }
    public void              consume()           { this.consumed = true; setShouldRemove(true); }
    public IscatWormSegment  getPreviousSegment(){ return previousSegment; }
    public void              setPreviousSegment(IscatWormSegment p) { this.previousSegment = p; }

    // Metodi fisici esposti (niente @Override: non c'è interfaccia)
    public Vector2 getPosition()              { return getTransform().getTranslation(); }
    public void    setRotation(double angle)  { getTransform().setRotation(angle); }
    public double  getRotation()              { return getTransform().getRotationAngle(); }

    private static int    getHp(Type t)     { return switch(t){ case HEAD->IscatWormSettings.HEAD_HP; case BODY->IscatWormSettings.BODY_HP; case TAIL->IscatWormSettings.TAIL_HP; }; }
    private static double getRadius(Type t) { return switch(t){ case HEAD->IscatWormSettings.HEAD_SCALE; case BODY->IscatWormSettings.BODY_SCALE; case TAIL->IscatWormSettings.TAIL_SCALE; } * IscatWormSettings.DIM_SPRITE / 2 * 0.85; }
    private static double getDamping(Type t){ return switch(t){ case HEAD->2.8; case BODY->4.2; case TAIL->5.5; }; }
}