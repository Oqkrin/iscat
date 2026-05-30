package uni.gaben.iscat.universe.lib.interfaces.controller;

import uni.gaben.iscat.universe.UniverseModel;

@FunctionalInterface
public interface IEntityController {
    void update(UniverseModel world, double dt);

}