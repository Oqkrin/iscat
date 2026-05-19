package uni.gaben.iscat.game.lib.implementations.behaviors;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.universe.UniverseModel;

public class ObstacleAvoidanceBehavior implements AiBehavior {

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {

    }
}