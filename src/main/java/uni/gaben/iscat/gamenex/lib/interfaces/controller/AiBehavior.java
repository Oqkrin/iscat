package uni.gaben.iscat.gamenex.lib.interfaces.controller;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseModel;

/**
 * Interfaccia funzionale per una singola unità di comportamento dell'intelligenza artificiale.
 * Ogni implementazione rappresenta una logica atomica (es. "Insegui", "Spara", "Fuggi").
 */
@FunctionalInterface
public interface AiBehavior {
    /**
     * Esegue la logica del comportamento.
     * @param npc L'entità che deve compiere l'azione.
     * @param universe Il modello del mondo per recuperare informazioni sul target e sull'ambiente.
     * @param dt Il tempo trascorso dall'ultimo frame (Delta Time).
     */
    void execute(AbstractEntityModel npc, UniverseModel universe, double dt);
}
