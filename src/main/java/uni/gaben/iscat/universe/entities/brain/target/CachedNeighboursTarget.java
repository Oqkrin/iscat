package uni.gaben.iscat.universe.entities.brain.target;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

import java.util.List;

public class CachedNeighboursTarget implements Target {
    private final Body self;
    private final double range;
    private final UniverseDetectFilter filter; // changed from generic DetectFilter
    private List<AbstractPhysicalEntityModel> cachedEntities;
    private double lastQueryTime = -1.0;

    public CachedNeighboursTarget(Body self, double range, UniverseDetectFilter filter) {
        this.self = self;
        this.range = range;
        this.filter = filter;
    }

    @Override
    public List<AbstractPhysicalEntityModel> getEntities(UniverseModel universe) {
        double now = universe.getPhysicsLifetime();
        if (cachedEntities != null && lastQueryTime == now) {
            return cachedEntities;
        }

        filter.setUniverse(universe);

        AABB aabb = self.createAABB();
        AABB detectionBox = new AABB(
                aabb.getMinX() - range,
                aabb.getMinY() - range,
                aabb.getMaxX() + range,
                aabb.getMaxY() + range
        );

        cachedEntities = universe.detect(detectionBox, filter).stream()
                .map(r -> r.getBody())
                .filter(body -> body instanceof AbstractPhysicalEntityModel)
                .map(body -> (AbstractPhysicalEntityModel) body)
                .filter(e -> e != self)
                .toList();
        lastQueryTime = now;
        return cachedEntities;
    }
}