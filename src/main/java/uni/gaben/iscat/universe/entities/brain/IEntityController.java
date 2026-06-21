package uni.gaben.iscat.universe.entities.brain;

import uni.gaben.iscat.universe.UniverseModel;

/**
 * Interfaccia funzionale astratta per i moduli di controllo delle entità (Entity Controller).
 * <p>
 * Definisce il contratto standard per qualsiasi componente deputato all'aggiornamento logico,
 * comportamentale o decisionale di un'entità all'interno dell'universo di gioco ad ogni tick della simulazione.
 * </p>
 */
@FunctionalInterface
public interface IEntityController {

    /**
     * Aggiorna lo stato logico del controller basandosi sul tempo trascorso dall'ultimo frame.
     *
     * @param world Il modello globale dell'universo di gioco.
     * @param dt    Il delta time (tempo in secondi) trascorso dall'ultimo frame di simulazione.
     */
    void update(UniverseModel world, double dt);
}