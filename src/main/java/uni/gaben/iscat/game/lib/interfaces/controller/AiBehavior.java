package uni.gaben.iscat.game.lib.interfaces.controller;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.universe.UniverseModel;

/**
 * Interfaccia per una singola unità di comportamento dell'intelligenza artificiale.
 * Ogni implementazione rappresenta una logica atomica (es. "Insegui", "Spara", "Fuggi")
 * e definisce dinamicamente la propria priorità per consentire il cambio di stato.
 */
public interface AiBehavior {
    
    /**
     * Calcola l'urgenza di questo comportamento.
     * @param npc L'entità.
     * @param universe Il mondo.
     * @return Un valore > 0 se eseguibile, maggiore è il valore più alta è la priorità.
     */
    double getPriority(AbstractEntityModel npc, UniverseModel universe);

    /**
     * Esegue la logica del comportamento.
     * @param npc L'entità che deve compiere l'azione.
     * @param universe Il modello del mondo.
     * @param dt Il tempo trascorso (Delta Time).
     */
    void execute(AbstractEntityModel npc, UniverseModel universe, double dt);

    default void tick(AbstractEntityModel enemy, UniverseModel universe, double dt) {}
}
