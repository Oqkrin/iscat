package uni.gaben.iscat.universe.brain;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@FunctionalInterface
public interface Target {

    /** Returns all entities associated with this target (empty if targeting a point in space). */
    List<AbstractEntityModel> getEntities(UniverseModel world);

    /** Returns the positions of all relevant entities or points. */
    default List<Vector2> getPositions(UniverseModel world) {
        return getEntities(world).stream()
                .filter(e -> !e.shouldRemove())
                .map(e -> e.getTransform().getTranslation())
                .collect(Collectors.toList());
    }

    /** Returns a single representative position (the exact point, or the center of a group). */
    default Vector2 getPosition(UniverseModel world) {
        List<Vector2> positions = getPositions(world);
        if (positions.isEmpty()) return null;
        if (positions.size() == 1) return positions.get(0);

        // Average center for groups (Flocking Cohesion)
        Vector2 center = new Vector2();
        for (Vector2 p : positions) center.add(p);
        return center.multiply(1.0 / positions.size());
    }

    // ── Factories ────────────────────────────────────────────────────────

    static Target ofPoint(Vector2 point) {
        return new Target() {
            @Override
            public List<AbstractEntityModel> getEntities(UniverseModel world) { return Collections.emptyList(); }
            @Override
            public List<Vector2> getPositions(UniverseModel world) { return Collections.singletonList(point.copy()); }
        };
    }

    static Target ofDynamicPoint(Function<UniverseModel, Vector2> query) {
        return new Target() {
            @Override
            public List<AbstractEntityModel> getEntities(UniverseModel world) { return Collections.emptyList(); }
            @Override
            public List<Vector2> getPositions(UniverseModel world) {
                Vector2 p = query.apply(world);
                return p == null ? Collections.emptyList() : Collections.singletonList(p);
            }
        };
    }

    static Target ofEntity(AbstractEntityModel entity) {
        return world -> entity.shouldRemove() ? Collections.emptyList() : Collections.singletonList(entity);
    }

    /** A common helper for enemy AI. */
    static Target ofPlayer() {
        return world -> {
            PlayerModel p = world.getPlayer();
            return (p != null && !p.shouldRemove()) ? Collections.singletonList(p) : Collections.emptyList();
        };
    }

    /** The ultimate dynamic query (perfect for Flocking Boids). */
    static Target ofEntities(Function<UniverseModel, List<AbstractEntityModel>> query) {
        return query::apply;
    }



    static Target neighbours(Body body, double range, DetectFilter<Body, BodyFixture> filter) {

        AABB aabb = body.createAABB();

        AABB detectionRange = new AABB(
                aabb.getMinX() - range,
                aabb.getMinY() - range,
                aabb.getMaxX() + range,
                aabb.getMaxY() + range
        );


        return universe -> universe.detect(detectionRange, filter).stream().map((detectResult -> (AbstractEntityModel) detectResult.getBody())).toList();
    }

}