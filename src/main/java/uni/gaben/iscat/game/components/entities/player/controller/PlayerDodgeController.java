package uni.gaben.iscat.game.components.entities.player.controller;

import uni.gaben.iscat.game.controller.InputHandler;
import uni.gaben.iscat.game.utils.settings.GameSettings;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.PlayerSettings;
import uni.gaben.iscat.game.components.space.SpaceModel;

/**
 * Gestisce lo scatto (dodge): consuma l'input, esegue il dash,
 * e propaga l'impulso visivo alle stelle.
 */
public class PlayerDodgeController {

    private Runnable onScatto;

    /** Registra il callback audio/effetti da eseguire quando lo scatto viene eseguito. */
    public void setOnScatto(Runnable callback) { this.onScatto = callback; }

    /**
     * Processa lo scatto per questo tick.
     * @return true se lo scatto è appena stato eseguito (per l'impulso stelle)
     */
    public boolean process(InputHandler input, PlayerModel p) {
        if (input.consumeDodge() && p.isScattoDisponibile()) {
            p.executeScatto(p.getDirectionAngle());
            if (onScatto != null) onScatto.run();
            return true;
        }
        return false;
    }

    /** Applica l'impulso visivo alle stelle dopo uno scatto. */
    public void applyStarImpulse(PlayerModel p, SpaceModel space) {
        double rad = Math.toRadians(p.getDirectionAngle());
        space.applyImpulse(
                Math.cos(rad) * PlayerSettings.IMPULSO_SCATTO * GameSettings.FATTORE_IMPULSO_STELLE,
                Math.sin(rad) * PlayerSettings.IMPULSO_SCATTO * GameSettings.FATTORE_IMPULSO_STELLE
        );
    }
}
