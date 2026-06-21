package uni.gaben.iscat.universe.entities.hardcoded.blackhole;

import org.dyn4j.world.DetectFilter;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.brain.abilities.GravityPullAbility;

/**
 * Controller logico e comportamentale del Buco Nero (Black Hole Brain Architecture).
 * <p>
 * Estende la classe {@link Brain} per orchestrare l'azione periodica di attrazione gravitazionale
 * sull'ambiente circostante. Implementa un sistema di ascolto reattivo (Listener) per scalare
 * dinamicamente l'area di influenza della gravità proporzionalmente alla crescita del raggio dell'entità.
 * </p>
 */
public class BlackHoleBrain extends Brain<BlackHoleModel> {

    /**
     * Costruisce il cervello del buco nero, registra l'abilità di attrazione gravitazionale iniziale
     * e aggancia un listener per ricalcolarla in caso di espansione della massa.
     *
     * @param entity Il modello fisico {@link BlackHoleModel} associato a questo controller.
     */
    public BlackHoleBrain(BlackHoleModel entity) {
        super(entity);

        // Configurazione dell'azione gravitazionale iniziale basata sul raggio di partenza (moltiplicato per un fattore x10)
        addAction("gravity",
                new GravityPullAbility(Target.neighbours(entity, entity.getRadius().m().get() * 10, new DetectFilter<>(true, true, null)))
        );

        // Listener Reattivo: ad ogni variazione del raggio, l'azione di gravità precedente viene sostituita
        // istantaneamente per estendere o ridurre l'area di cattura (Aura Query Range) dei corpi rigidi vicini.
        entity.getRadius().m().addListener(
                (_, _, newRadius) -> replaceAction("gravity",
                        new GravityPullAbility(Target.neighbours(entity, (double) newRadius * 10, new DetectFilter<>(true, true, null)))
                ));
    }
}