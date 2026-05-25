package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;

public class IscatDasherController extends AiBehaviours<IscatDasherModel> implements AiController {


    /**
     * Crea un controller per un'entità specifica.
     *
     * @param aiEntity Il modello fisico dell'entità.
     */
    public IscatDasherController(IscatDasherModel aiEntity) {
        super(aiEntity);

        addBehavior(
                new ChaseBehavior(aiEntity.getBaseAccelerationPerTick(), aiEntity.getTerminalVelocity()));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {

    }
}
