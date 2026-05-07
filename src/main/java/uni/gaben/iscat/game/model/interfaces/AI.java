package uni.gaben.iscat.game.model.interfaces;

import uni.gaben.iscat.game.model.GameModel;

/**
 * Interfaccia per entità con comportamento AI.
 * L'AI viene aggiornata dal GameModel prima della fisica.
 */
public interface AI {
    
    /**
     * Aggiorna la logica AI dell'entità.
     * Chiamato ogni tick prima dell'aggiornamento fisico.
     * 
     * @param model riferimento al GameModel per accedere a player, nemici, ecc.
     * @param dt delta time
     */
    void updateAI(GameModel model, double dt);
    
    /**
     * Resetta lo stato dell'AI (es. dopo morte, stun, teleport).
     */
    default void resetAI() {}
}
