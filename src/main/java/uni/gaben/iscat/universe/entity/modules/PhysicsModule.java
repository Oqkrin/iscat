package uni.gaben.iscat.universe.entity.modules;

import org.dyn4j.collision.AbstractCollisionBody;
import org.dyn4j.collision.CategoryFilter;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.*;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entity.ShapeType;
import uni.gaben.iscat.universe.PolygonFactory;
import uni.gaben.iscat.universe.entity.Data.PhysicsData;
import uni.gaben.iscat.universe.entity.Data.IdentityData;

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

        // Determine collision radius:
        //   1. Explicit radius in physics data (metres)  → use directly
        //   2. Sprite frame dimensions (pixels)          → convert
        //   3. Hard fallback                             → 0.25 m
        double collisionSize;
        if (data.radius() > 0) {
            collisionSize = UU.pxToM(data.radius()); // stored in px, convert to m
        } else if (entity.getRecord().sprite() != null) {
            double w     = entity.getRecord().sprite().frameW();
            double scale = entity.getRecord().sprite().scale();
            collisionSize = UU.pxToM(w * scale * 0.9);
        } else {
            collisionSize = 0.25; // sensible fallback (metres)
        }

        if (data.shapeType() == ShapeType.CIRCLE) {
            fixture = entity.addFixture(Geometry.createCircle(collisionSize / 2.0));
        } else if (data.shapeType() == ShapeType.SQUARE) {
            fixture = entity.addFixture(Geometry.createSquare(collisionSize));
        } else if (data.shapeType() == ShapeType.POLYGON) {
            fixture = entity.addFixture(new Polygon(PolygonFactory.getVerticesByRadius(collisionSize)));
        }

        if (entity.getRecord().identity() != null) {
            switch (entity.getRecord().identity().type()) {
                case PROJECTILE:
                    if (entity.getRecord().identity().ownerId() != null && entity.getRecord().identity().ownerId().contains("player")) {
                        fixture.setFilter(UniverseCollisionLayers.PROJECTILE_FILTER);
                    } else {
                        fixture.setFilter(UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER);
                    }
                    entity.setBullet(true);
                    break;
                case PLAYER:
                    fixture.setFilter(UniverseCollisionLayers.PLAYER_FILTER);
                    break;
                case ENEMY:
                    if (entity.getRecord().identity().entityKey().contains("worm_body") || entity.getRecord().identity().entityKey().contains("worm_tail")) {
                        fixture.setFilter(UniverseCollisionLayers.WORM_BODY_FILTER);
                    } else {
                        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
                    }
                    break;
                case ENVIRONMENT:
                    fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);
                    break;
                default:
                    fixture.setFilter(new CategoryFilter(data.collisionFilter(), ~0L));
                    break;
            }
        } else {
            fixture.setFilter(new CategoryFilter(data.collisionFilter(), ~0L));
        }

        fixture.setDensity(data.density());
        fixture.setSensor(data.isSensor());

        // Set mass; zero-mass bodies become FIXED_LINEAR_VELOCITY (e.g. static blackholes)
        entity.setMass(data.mass() > 0 ? MassType.NORMAL : MassType.FIXED_LINEAR_VELOCITY);
        entity.setLinearDamping(data.linearDamping());
    }

    public BodyFixture getFixture() {
        return fixture;
    }

    /**
     * Re-applies the collision filter for a bullet when the owning entity (shooter)
     * is known. Called by Shooter after populating the bullet's ownerId at fire time.
     */
    public void applyOwnerFilter(IdentityData identity) {
        if (fixture == null) return;
        switch (identity.type()) {
            case PROJECTILE:
                if (identity.ownerId() != null && identity.ownerId().contains("player")) {
                    fixture.setFilter(UniverseCollisionLayers.PROJECTILE_FILTER);
                } else {
                    fixture.setFilter(UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER);
                }
                break;
            default:
                // No change for other types
                break;
        }
    }

    // ---- Geometry helpers (based on fixtures) ----
    public double getWidthMeters() {
        if (entity.getFixtureCount() == 0) return 0.0;
        AABB aabb = entity.createAABB(new Transform());
        return aabb.getWidth();
    }

    public double getHeightMeters() {
        if (entity.getFixtureCount() == 0) return 0.0;
        AABB aabb = entity.createAABB(new Transform());
        return aabb.getHeight();
    }

    public double getWidthPx() { return UU.mToPx(getWidthMeters()); }

    public double getHeightPx() { return UU.mToPx(getHeightMeters()); }
}
