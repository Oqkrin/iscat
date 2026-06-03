package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.targets.CachedNeighboursTarget;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
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

    static Target ofPlayer() {
        return world -> {
            PlayerModel p = world.getPlayer();
            return (p != null && !p.shouldRemove()) ? Collections.singletonList(p) : Collections.emptyList();
        };
    }

    static Target ofEntities(Function<UniverseModel, List<AbstractEntityModel>> query) {
        return query::apply;
    }



    /**
     * Returns a Target that queries neighbours using a dynamic AABB (re‑computed every call).
     * To avoid repeated queries, use neighboursCached() instead.
     */
    static Target neighbours(Body body, double range, DetectFilter<Body, BodyFixture> filter) {
        return universe -> {
            AABB aabb = body.createAABB();
            AABB detectionBox = new AABB(
                    aabb.getMinX() - range,
                    aabb.getMinY() - range,
                    aabb.getMaxX() + range,
                    aabb.getMaxY() + range
            );
            return universe.detect(detectionBox, filter).stream()
                    .map(r -> (AbstractEntityModel) r.getBody())
                    .collect(Collectors.toList());
        };
    }

    static Target neighboursCached(Body self, double range,
                                   boolean ignoreSensors, boolean ignoreDisabled,
                                   Filter categoryMaskFilter,
                                   UniverseFilter universeFilter,
                                   Predicate<Body> bodyPredicate) {
        UniverseDetectFilter filter = new UniverseDetectFilter(
                ignoreSensors, ignoreDisabled, categoryMaskFilter, universeFilter, bodyPredicate);
        return new CachedNeighboursTarget(self, range, filter);
    }

    // Even simpler: only body predicate, no universe filter
    static Target neighboursCached(Body self, double range, Predicate<Body> bodyPredicate) {
        return neighboursCached(self, range, true, true, null, null, bodyPredicate);
    }

    // Inside Target.java

    /**
     * Returns a new Target that filters the entities of this target.
     * The underlying list is still cached; filtering is applied on each call.
     */
    default Target filtered(Predicate<AbstractEntityModel> predicate) {
        return world -> getEntities(world).stream().filter(predicate).toList();
    }

}