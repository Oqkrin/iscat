package uni.gaben.iscat.universe.lib.implementations.behaviors;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;

public class ObstacleAvoidanceBehavior implements AiBehavior {

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {

    }
}