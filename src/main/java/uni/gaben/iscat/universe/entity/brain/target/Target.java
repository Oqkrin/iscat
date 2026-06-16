package uni.gaben.iscat.universe.entity.brain.target;

import org.dyn4j.collision.Filter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.world.result.DetectResult;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

@FunctionalInterface
public interface Target {

    List<AbstractEntityModel> getEntities(UniverseModel universe);

    default List<Vector2> getPositions(UniverseModel universe) {
        List<AbstractEntityModel> entities = getEntities(universe);
        List<Vector2> positions = new ArrayList<>(entities.size());
        for (int i = 0; i < entities.size(); i++) {
            AbstractEntityModel e = entities.get(i);
            if (!e.shouldRemove()) {
                positions.add(e.getTransform().getTranslation());
            }
        }
        return positions;
    }

    default Vector2 getPosition(UniverseModel universe) {
        List<AbstractEntityModel> entities = getEntities(universe);
        if (entities.isEmpty()) return null;

        double sumX = 0.0;
        double sumY = 0.0;
        int count = 0;

        for (int i = 0; i < entities.size(); i++) {
            AbstractEntityModel e = entities.get(i);
            if (e != null && !e.shouldRemove()) {
                Vector2 pos = e.getTransform().getTranslation();
                sumX += pos.x;
                sumY += pos.y;
                count++;
            }
        }

        if (count == 0) return null;
        return new Vector2(sumX / count, sumY / count);
    }

    // ── Factories ────────────────────────────────────────────────────────

    static Target ofPoint(Vector2 point) {
        final Vector2 copiedPoint = point.copy();
        final List<Vector2> cachedList = Collections.singletonList(copiedPoint);
        return new Target() {
            @Override
            public List<AbstractEntityModel> getEntities(UniverseModel universe) { return Collections.emptyList(); }
            @Override
            public List<Vector2> getPositions(UniverseModel universe) { return cachedList; }
            @Override
            public Vector2 getPosition(UniverseModel universe) { return copiedPoint; }
        };
    }

    static Target ofDynamicPoint(Function<UniverseModel, Vector2> query) {
        return new Target() {
            @Override
            public List<AbstractEntityModel> getEntities(UniverseModel universe) { return Collections.emptyList(); }
            @Override
            public List<Vector2> getPositions(UniverseModel universe) {
                Vector2 p = query.apply(universe);
                return p == null ? Collections.emptyList() : Collections.singletonList(p);
            }
            @Override
            public Vector2 getPosition(UniverseModel universe) { return query.apply(universe); }
        };
    }

    static Target ofEntity(AbstractEntityModel entity) {
        return world -> entity.shouldRemove() ? Collections.emptyList() : Collections.singletonList(entity);
    }

    static Target ofPlayer() {
        return universe -> {
            PlayerModel p = universe.getPlayer();
            return (p != null && !p.shouldRemove()) ? Collections.singletonList(p) : Collections.emptyList();
        };
    }

    static Target ofEntities(Function<UniverseModel, List<AbstractEntityModel>> query) {
        return query::apply;
    }

    static Target neighbours(Body body, double range, DetectFilter<Body, BodyFixture> filter) {
        return universe -> {
            AABB aabb = body.createAABB();
            AABB detectionBox = new AABB(
                    aabb.getMinX() - range, aabb.getMinY() - range,
                    aabb.getMaxX() + range, aabb.getMaxY() + range
            );
            List<DetectResult<Body, BodyFixture>> detected = universe.detect(detectionBox, filter);
            List<AbstractEntityModel> result = new ArrayList<>(detected.size());
            for (int i = 0; i < detected.size(); i++) {
                result.add((AbstractEntityModel) detected.get(i).getBody());
            }
            return result;
        };
    }

    static Target neighboursCached(Body self, double range, boolean ignoreSensors, boolean ignoreDisabled,
                                   Filter categoryMaskFilter, UniverseFilter universeFilter, Predicate<Body> bodyPredicate) {
        UniverseDetectFilter filter = new UniverseDetectFilter(ignoreSensors, ignoreDisabled, categoryMaskFilter, universeFilter, bodyPredicate);
        return new CachedNeighboursTarget(self, range, filter);
    }

    static Target neighboursCached(Body self, double range, Predicate<Body> bodyPredicate) {
        return neighboursCached(self, range, true, true, null, null, bodyPredicate);
    }

    default Target filtered(Predicate<AbstractEntityModel> predicate) {
        return world -> {
            List<AbstractEntityModel> all = getEntities(world);
            List<AbstractEntityModel> filtered = new ArrayList<>(all.size());
            for (int i = 0; i < all.size(); i++) {
                AbstractEntityModel e = all.get(i);
                if (predicate.test(e)) filtered.add(e);
            }
            return filtered;
        };
    }

    /**
     * High-performance intercept path calculation. Bypasses allocations by writing to the `out` vector.
     */
    default Vector2 predictedPosition(UniverseModel universe, Vector2 targeterPos, double velocity, Vector2 out) {
        Vector2 targetPos = getPosition(universe);
        if (targetPos == null) return out.set(0, 0);

        double lookAheadTime = Predictor.calculateDirectTime(targeterPos, targetPos, velocity);
        return Predictor.extrapolate(this, universe, lookAheadTime, out);
    }
}