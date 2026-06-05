package uni.gaben.iscat.universe.entity.brain.actions;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import java.util.Set;

public abstract class Action {
    protected final String name;
    protected final ActionCategory category;
    /** Which categories this action blocks while it's running (besides its own). */
    protected final Set<ActionCategory> blockedCategories;

    public Action(String name, ActionCategory category, Set<ActionCategory> blockedCategories) {
        this.name = name;
        this.category = category;
        this.blockedCategories = blockedCategories;
    }

    public ActionCategory getCategory() { return category; }
    public Set<ActionCategory> getBlockedCategories() { return blockedCategories; }

    public abstract boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt);
    public abstract void onActivate(Brain<?> brain, UniverseModel world);
    /** @return true if still running, false when finished */
    public abstract boolean update(Brain<?> brain, UniverseModel world, double dt);
}