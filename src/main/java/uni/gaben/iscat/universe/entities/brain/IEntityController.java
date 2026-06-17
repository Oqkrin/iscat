package uni.gaben.iscat.universe.entities.brain;

import uni.gaben.iscat.universe.UniverseModel;

@FunctionalInterface
public interface IEntityController {
    void update(UniverseModel world, double dt);
}