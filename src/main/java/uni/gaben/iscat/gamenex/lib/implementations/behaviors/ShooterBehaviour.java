package uni.gaben.iscat.gamenex.lib.implementations.behaviors;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

/**
 * Gestisce la logica di attacco a distanza.
 * @param <T> L'entità che spara (deve implementare Shooter).
 * @param <P> Il tipo di proiettile generato.
 */
public class ShooterBehaviour<T extends HasProjectile<P>, P extends AbstractProjectileModel> implements AiBehavior {

    private double priorityValue = 80.0;
    private double combatRange = 8.0;

    public ShooterBehaviour() {}

    public ShooterBehaviour(double priorityValue, double combatRange) {
        this.priorityValue = priorityValue;
        this.combatRange = combatRange;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = npc.getTransform().getTranslation().distance(player.getTransform().getTranslation());
        // Spara solo se il giocatore è entro il raggio di combattimento
        return dist <= combatRange ? priorityValue : 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {

    }
}