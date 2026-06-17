package uni.gaben.iscat.universe.entities.brain.abilities;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.brain.Brain;

import java.util.Set;

public abstract class Ability {
    protected final String name;
    protected final AbilityCategory category;
    protected final Set<AbilityCategory> blockedCategories;

    protected Ability(String name, AbilityCategory category, Set<AbilityCategory> blockedCategories) {
        this.name = name;
        this.category = category;
        this.blockedCategories = blockedCategories;
    }

    public AbilityCategory getCategory() { return category; }
    public Set<AbilityCategory> getBlockedCategories() { return blockedCategories; }

    public abstract boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt);
    public abstract void onActivate(Brain<?> brain, UniverseModel world);
    /** @return true if still running, false when finished */
    public abstract boolean update(Brain<?> brain, UniverseModel world, double dt);

}