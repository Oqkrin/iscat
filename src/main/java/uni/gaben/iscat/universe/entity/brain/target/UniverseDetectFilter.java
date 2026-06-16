package uni.gaben.iscat.universe.entity.brain.target;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.world.DetectFilter;
import org.dyn4j.collision.Filter;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.function.Predicate;

public class UniverseDetectFilter extends DetectFilter<Body, BodyFixture> {
    private UniverseModel universe;
    private final UniverseFilter universeFilter;
    private final Predicate<Body> bodyPredicate; // NEW: filter by body type / property

    public UniverseDetectFilter(boolean ignoreSensors, boolean ignoreDisabled,
                                Filter filter, UniverseFilter universeFilter,
                                Predicate<Body> bodyPredicate) {
        super(ignoreSensors, ignoreDisabled, filter);
        this.universeFilter = universeFilter;
        this.bodyPredicate = bodyPredicate;
    }

    // Convenience constructor without universeFilter
    public UniverseDetectFilter(boolean ignoreSensors, boolean ignoreDisabled,
                                Filter filter, Predicate<Body> bodyPredicate) {
        this(ignoreSensors, ignoreDisabled, filter, null, bodyPredicate);
    }

    public void setUniverse(UniverseModel universe) {
        this.universe = universe;
    }

    @Override
    public boolean isAllowed(Body body, BodyFixture fixture) {
        // 1. Standard dyn4j checks (sensors, disabled, category/mask)
        if (!super.isAllowed(body, fixture)) return false;

        // 2. Body predicate (type check, exclude self, etc.)
        if (bodyPredicate != null && !bodyPredicate.test(body)) return false;

        // 3. Universe‑level filter (if any)
        if (universeFilter != null && universe != null) {
            return universeFilter.isAllowed(universe);
        }
        return true;
    }
}