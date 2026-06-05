package uni.gaben.iscat.universe.entity.brain.targets;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.UniverseDetectFilter;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.List;
import java.util.stream.Collectors;

public class CachedNeighboursTarget implements Target {
    private final Body self;
    private final double range;
    private final UniverseDetectFilter filter; // changed from generic DetectFilter
    private List<AbstractEntityModel> cachedEntities;
    private double lastQueryTime = -1.0;

    public CachedNeighboursTarget(Body self, double range, UniverseDetectFilter filter) {
        this.self = self;
        this.range = range;
        this.filter = filter;
    }

    @Override
    public List<AbstractEntityModel> getEntities(UniverseModel universe) {
        double now = universe.getLifetime(); // or getTick()
        if (cachedEntities != null && lastQueryTime == now) {
            return cachedEntities;
        }

        // Inject the current universe into the filter
        filter.setUniverse(universe);

        AABB aabb = self.createAABB();
        AABB detectionBox = new AABB(
                aabb.getMinX() - range,
                aabb.getMinY() - range,
                aabb.getMaxX() + range,
                aabb.getMaxY() + range
        );

        cachedEntities = universe.detect(detectionBox, filter).stream()
                .map(r -> (AbstractEntityModel) r.getBody())
                .filter(e -> e != self)
                .collect(Collectors.toList());
        lastQueryTime = now;
        return cachedEntities;
    }
}