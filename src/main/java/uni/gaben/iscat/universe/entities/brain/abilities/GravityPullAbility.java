package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import java.util.Collections;

/**
 * Abilità speciale per la simulazione di un campo di attrazione gravitazionale attivo (Gravity Pull Ability).
 * <p>
 * Agisce come un'abilità passiva o continua a esecuzione persistente. A ogni tick del ciclo logico,
 * itera sulle entità all'interno del raggio d'azione (Target) e applica un vettore di forza centripeta
 * basato sull'equazione di Newton della gravitazione universale:
 * </p>
 * $$F = G \frac{M \cdot m}{r^2}$$
 */
public class GravityPullAbility extends Ability {

    private final Target inRange;

    /**
     * Inizializza l'abilità gravitazionale definendo il fornitore di entità da attrarre.
     */
    public GravityPullAbility(Target target) {
        super("GravityPull", AbilityCategory.SPECIAL, Collections.emptySet());
        this.inRange = target;
    }

    /**
     * @return {@code true} in quanto l'abilità non ha prerequisiti e può rimanere sempre attiva come effetto di campo.
     */
    @Override
    public boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
        return true;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {}

    /**
     * Mantiene l'abilità indefinitamente attiva all'interno della coda delle azioni finché il mondo esiste.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return world != null;
    }

    /**
     * Routine di aggiornamento continuo che calcola e applica le forze gravitazionali.
     * Isola le masse dei corpi coinvolti e la loro distanza euclidea al quadrato per ricavare l'intensità del vettore.
     */
    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {
        Vector2 myPos = brain.getEntity().getTransform().getTranslation();
        double myMass = brain.getEntity().getMass().getMass();

        // Calcolo e Applicazione della Forza Gravitazionale Newtoniana
        for (AbstractPhysicalEntityModel item : inRange.getEntities(world)) {
            if (item == brain.getEntity() || item == null) continue;

            Vector2 otherPos = item.getTransform().getTranslation();
            Vector2 direction = myPos.copy().subtract(otherPos);
            double distanceSq = direction.getMagnitudeSquared();

            // Safe check per prevenire la divisione per zero in caso di sovrapposizione perfetta dei centri di massa
            if (distanceSq < 0.01) continue;

            double otherMass = item.getMass().getMass();

            // Calcolo dell'intensità della forza tramite la costante gravitazionale globale del motore (G)
            double forceMagnitude = (UU.UniversalGravitationalConstant.get() * myMass * otherMass) / distanceSq;

            direction.normalize();

            // Applicazione della forza accumulata sul corpo rigido dell'entità target
            item.applyForce(direction.multiply(forceMagnitude));
        }
    }
}