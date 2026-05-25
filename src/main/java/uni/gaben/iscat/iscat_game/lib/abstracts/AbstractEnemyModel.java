package uni.gaben.iscat.iscat_game.lib.abstracts;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;

/**
 * Base model for all standard enemies.
 * Automatically applies physics constraints (mass, damping, hitboxes, collision layers)
 * based on the provided BaseEntitySettings, drastically reducing boilerplate code.
 */
public abstract class AbstractEnemyModel extends LivingEntityModel {
    protected final BaseEntitySettings settings;

    public AbstractEnemyModel(double x, double y, BaseEntitySettings settings) {
        super(x, y, settings.initLife, settings.initLife);
        this.settings = settings;
        setXpReward(settings.xpReward);
        
        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(settings.dimSprite * settings.scale / 2.0 * 0.9)));
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(settings.dampingLineare);
    }

    public BaseEntitySettings getSettings() {
        return settings;
    }

    @Override
    public double getTerminalVelocity() {
        return settings.maxVelocity;
    }
}
