package uni.gaben.iscat.universe.entity.brain;

import uni.gaben.iscat.universe.UniverseModel;

@FunctionalInterface
public interface IEntityController {
    void update(UniverseModel world, double dt);
}
