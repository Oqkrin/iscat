package uni.gaben.iscat.universe.enemies.generic;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * A single Model class that replaces IscatMobModel, IscatCoreModel, etc.
 * All physics values come from a GenericEntitySettings instance loaded
 * from the Entita DB table — no hardcoded numbers anywhere.
 * Shape is determined by settings.shapeType:
 *   CIRCLE → createCircle(radius)  — used by mob-style enemies
 *   SQUARE → createSquare(side)    — used by core-style enemies
 * RAM collision damage is driven by settings.customParam1 (heavy slam)
 * and settings.customParam2 (light contact). If both are 0.0 the
 * collision callback is simply not registered, so WANDER_SHOOT enemies
 * are unaffected.
 */
public class GenericEntityModel extends LivingEntityModel {

    private final GenericEntitySettings settings;

    public GenericEntityModel(double x, double y, GenericEntitySettings settings) {
        super(x, y, settings.initLife, settings.initLife);
        this.settings = settings;

        setXpReward(settings.xpReward);

        // ── Fixture ──────────────────────────────────────────────────────────
        BodyFixture fixture = switch (settings.shapeType) {
            case CIRCLE -> addFixture(
                    Geometry.createCircle(
                            UU.pxToM(settings.dimSprite * settings.scale / 2.0 * 0.9)));
            case SQUARE -> addFixture(
                    Geometry.createSquare(
                            UU.pxToM(settings.dimSprite * settings.scale * 0.9)));
        };

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(settings.dampingLineare);

        // ── RAM collision damage (only wired if the enemy actually uses it) ──
        if (settings.customParam1 > 0.0 || settings.customParam2 > 0.0) {
            final double heavyDamage = settings.customParam1;
            final double lightDamage = settings.customParam2;

            setOnCollision(other -> {
                if (other instanceof PlayerModel player) {
                    double speed = this.getLinearVelocity().getMagnitude();
                    if (speed > settings.maxVelocity * 1.5) {
                        player.deltaToLife(-heavyDamage);
                    } else {
                        player.deltaToLife(-lightDamage);
                    }
                }
            });
        }
    }

    public void update(double dt) {
        updateStateTime(dt);
    }

    @Override
    public double getTerminalVelocity() {
        return settings.maxVelocity;
    }

    public GenericEntitySettings getSettings() {
        return settings;
    }
}