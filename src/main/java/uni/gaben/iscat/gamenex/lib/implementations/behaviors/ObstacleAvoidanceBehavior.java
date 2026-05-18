package uni.gaben.iscat.gamenex.lib.implementations.behaviors;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;

public class ObstacleAvoidanceBehavior implements AiBehavior {

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {

    }
}