package uni.gaben.iscat.gamenex.lib.interfaces.controller;

import uni.gaben.iscat.gamenex.universe.UniverseModel;

/**
 * Interfaccia per i sistemi di controllo dell'intelligenza artificiale.
 * Qualsiasi oggetto che implementi questa interfaccia può gestire la logica
 * decisionale di un'entità nel tempo.
 */
public interface AiController {
    /**
     * Aggiorna lo stato decisionale dell'entità.
     * @param universeModel Il mondo di riferimento per percepire target o ostacoli.
     * @param dt Il tempo trascorso (Delta Time).
     */
    void aiUpdate(UniverseModel universeModel, double dt);
}
