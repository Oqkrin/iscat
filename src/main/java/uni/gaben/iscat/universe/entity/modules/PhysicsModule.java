package uni.gaben.iscat.universe.entity.modules;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidShapeFactory;
import uni.gaben.iscat.universe.entity.record.PhysicsData;

public class PhysicsModule implements EntityModule {

    private GameEntity entity;
    private PhysicsData data;
    private BodyFixture fixture;

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().physics();

        createFixture();
    }

    public void createFixture() {
        if (fixture != null) {
            entity.removeFixture(fixture);
        }

        // The collision size might be based on sprite width/height.
        // Or provided dynamically. For now, assume based on sprite if not explicitly set.
        double w = entity.getRecord().sprite() != null ? entity.getRecord().sprite().frameW() : 32;
        double scale = entity.getRecord().sprite() != null ? entity.getRecord().sprite().scale() : 1.0;
        double collisionSize = UU.pxToM(w * scale * 0.9);

        if (data.shapeType() == EntityRecord.ShapeType.CIRCLE) {
            fixture = entity.addFixture(Geometry.createCircle(collisionSize / 2.0));
        } else if (data.shapeType() == EntityRecord.ShapeType.SQUARE) {
            fixture = entity.addFixture(Geometry.createSquare(collisionSize));
        } else if (data.shapeType() == EntityRecord.ShapeType.POLYGON) {
            fixture = entity.addFixture(new Polygon(AsteroidShapeFactory.getScaledShape(collisionSize)));
        }

        if (entity.getRecord().physics() != null && entity.getRecord().physics().isProjectile()) {
            if (entity.getRecord().identity().isEnemy()) {
                fixture.setFilter(UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER);
            } else {
                fixture.setFilter(UniverseCollisionLayers.PROJECTILE_FILTER);
            }
        } else if (entity.getRecord().identity() != null) {
            if (entity.getRecord().identity().entityKey().contains("player")) {
                fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);
            } else if (entity.getRecord().identity().isEnemy()) {
                if (entity.getRecord().identity().entityKey().contains("worm_body") || entity.getRecord().identity().entityKey().contains("worm_tail")) {
                    fixture.setFilter(UniverseCollisionLayers.WORM_BODY_FILTER);
                } else {
                    fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
                }
            } else {
                fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);
            }
        } else {
            fixture.setFilter(new org.dyn4j.collision.CategoryFilter(data.collisionFilter(), ~0L));
        }

        fixture.setDensity(data.density());
        fixture.setSensor(data.isSensor());

        // Update mass explicitly after changing density or shapes
        entity.setMass(data.mass() > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        entity.setLinearDamping(data.linearDamping());
    }

    public BodyFixture getFixture() {
        return fixture;
    }
}
