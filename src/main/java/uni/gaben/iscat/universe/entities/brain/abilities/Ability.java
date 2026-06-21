package uni.gaben.iscat.universe.entities.brain.abilities;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.brain.Brain;
import java.util.Set;

/**
 * Classe astratta per la definizione delle abilità e delle azioni delle IA (Ability Pattern).
 * Fornisce l'infrastruttura per gestire le mutue esclusioni tra azioni tramite un sistema a categorie bloccanti
 * e definisce il ciclo vitale a fasi (verifica, attivazione, avanzamento asincrono e aggiornamento continuo).
 */
public abstract class Ability {

    protected final String name;
    protected final AbilityCategory category;
    protected final Set<AbilityCategory> blockedCategories;

    /**
     * Costruttore base per strutturare i vincoli operativi e di mutua esclusione dell'abilità.
     *
     * @param name              Il nome identificativo dell'abilità.
     * @param category          La macro-categoria di appartenenza (es. ATTACK, MOVEMENT).
     * @param blockedCategories Il set di categorie di abilità che non possono essere eseguite in contemporanea a questa.
     */
    protected Ability(String name, AbilityCategory category, Set<AbilityCategory> blockedCategories) {
        this.name = name;
        this.category = category;
        this.blockedCategories = blockedCategories;
    }

    /** @return La categoria funzionale associata a questa abilità. */
    public AbilityCategory getCategory() { return category; }

    /** @return Il set di categorie che vengono inibite/bloccate durante l'attivazione di questa abilità. */
    public Set<AbilityCategory> getBlockedCategories() { return blockedCategories; }

    /**
     * Verifica la soddisfacibilità dei prerequisiti (es. cooldown, distanze, risorse) prima dell'attivazione.
     *
     * @param self  L'entità fisica proprietaria che tenta l'azione.
     * @param world Il modello globale del mondo di gioco.
     * @param dt    Il delta time del frame corrente.
     * @return {@code true} se l'abilità può essere avviata, altrimenti {@code false}.
     */
    public abstract boolean canActivate(AbstractPhysicalEntityModel self, UniverseModel world, double dt);

    /**
     * Logica di inizializzazione eseguita nell'istante esatto in cui l'abilità viene attivata e tolta dalla coda.
     */
    public abstract void onActivate(Brain<?> brain, UniverseModel world);

    /**
     * Aggiorna e fa avanzare lo stato dell'abilità nel tempo se questa ha un'esecuzione spalmata su più frame.
     * Utilizzato per gestire azioni asincrone e raffiche temporizzate.
     *
     * @return {@code true} se l'azione è ancora in esecuzione e richiede ulteriori aggiornamenti,
     * {@code false} se ha terminato il suo ciclo vitale e può essere rimossa.
     */
    public abstract boolean progressActivation(Brain<?> brain, UniverseModel world, double dt);

    /**
     * Routine di aggiornamento generica chiamata a ogni tick del ciclo logico principale, indipendentemente dallo stato di attivazione.
     */
    public abstract void update(Brain<?> brain, UniverseModel world, double dt);
}