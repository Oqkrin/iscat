package uni.gaben.iscat.universe.lib.behaviurs;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

public interface AttackBehavior {
    double getPriority(AbstractEntityModel entity, UniverseModel world);
    void execute(AbstractEntityModel entity, UniverseModel world, double dt);
    default void tick(AbstractEntityModel entity, UniverseModel world, double dt) {}
}