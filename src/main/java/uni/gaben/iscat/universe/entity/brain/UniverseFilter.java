package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.collision.Filter;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * Extends dyn4j's Filter to also allow filtering based on the whole game universe.
 */
public interface UniverseFilter extends Filter {

    /**
     * Returns true if the entity/fixture should be considered given the current universe state.
     */
    boolean isAllowed(UniverseModel universe);

    @Override
    default boolean isAllowed(Filter filter) {
        // Two UniverseFilters are considered compatible
        return filter instanceof UniverseFilter;
    }
}
