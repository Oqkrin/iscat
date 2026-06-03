package uni.gaben.iscat.universe.entity.enemies.worm;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entity.enemies.generic.GenericEntityModel;
import uni.gaben.iscat.universe.entity.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

public class IscatWormSegment extends GenericEntityModel {

    public enum Type { HEAD, BODY, TAIL }

    private Type type;
    private boolean consumed = false;
    private IscatWormSegment previousSegment;

    private final Cooldown attackCooldown = new Cooldown();

    public IscatWormSegment(Type type, double x, double y) {
        super(x, y, createSettings(type));
        this.type = type;
        setLife(getHp(type));
        setMaxLife(getHp(type));
        setXpReward(IscatWormSettings.XP_REWARD);

        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(getRadius(type))));

        if (type == Type.HEAD) {
            fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        } else {
            fixture.setFilter(UniverseCollisionLayers.WORM_BODY_FILTER);
        }

        setMass(MassType.FIXED_ANGULAR_VELOCITY);
        setLinearDamping(getDamping(type));
        setAngularDamping(0.0); // Curve fulminee
        // In IscatWormSegment constructor
        setOnCollision(other -> {
            if (other instanceof PlayerModel player) {
                if (type == Type.HEAD && canAttack()) {
                    double damage = IscatWormSettings.HEAD_ATTACK_POWER;
                    if (getLinearVelocity().getMagnitude() >
                            IscatWormSettings.HEAD_MAX_SPEED * IscatWormSettings.PLUNGE_THRESHOLD_MULT) {
                        damage *= IscatWormSettings.PLUNGE_DAMAGE_MULT;
                    }
                    player.deltaToLife(-damage);
                    startAttackCooldown();
                } else if (type != Type.HEAD) {
                    player.deltaToLife(-IscatWormSettings.BODY_CONTACT_DAMAGE);
                }
            }
        });
    }

    @Override
    public void update(double dt) {
        super.update(dt);
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

    private static GenericEntitySettings createSettings(Type type) {
        GenericEntitySettings s = new GenericEntitySettings();
        s.entityKey = "iscat_worm_" + type.name().toLowerCase();
        s.frameW = (int) IscatWormSettings.DIM_SPRITE;
        s.frameH = (int) IscatWormSettings.DIM_SPRITE;
        s.spritePath = switch (type) {
            case HEAD -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_head.png";
            case BODY -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_body_part.png";
            case TAIL -> "/uni/gaben/iscat/sprites/enemies/iscat_worm_tail.png";
        };
        s.scale = switch (type) {
            case HEAD -> IscatWormSettings.HEAD_SCALE;
            case BODY -> IscatWormSettings.BODY_SCALE;
            case TAIL -> IscatWormSettings.TAIL_SCALE;
        };
        s.linearDamping = getDamping(type);
        s.maxVelocity = IscatWormSettings.HEAD_MAX_SPEED; // Approximated
        s.maxForce = IscatWormSettings.HEAD_FORCE;
        return s;
    }
}