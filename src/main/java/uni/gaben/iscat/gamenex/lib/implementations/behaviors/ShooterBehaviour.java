package uni.gaben.iscat.gamenex.lib.implementations.behaviors;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Shooter;
import uni.gaben.iscat.gamenex.universe.UniverseModel;

/**
 * Gestisce la logica di attacco a distanza.
 * @param <T> L'entità che spara (deve implementare Shooter).
 * @param <P> Il tipo di proiettile generato.
 */
public class ShooterBehaviour<T extends Shooter<P>, P extends AbstractProjectileModel> implements AiBehavior {

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {

    }
}