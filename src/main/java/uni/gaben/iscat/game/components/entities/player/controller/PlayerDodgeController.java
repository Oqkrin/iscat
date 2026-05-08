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

    private boolean scattoAppenaEseguito = false;

    /**
     * Processa lo scatto per questo tick.
     * @return true se lo scatto è appena stato eseguito (per l'impulso stelle)
     */
    public boolean process(InputHandler input, PlayerModel p) {
        boolean eraDisponibile = p.isScattoDisponibile();
        if (input.consumeDodge()) p.richiestaScatto();
        p.elaboraScatto();
        scattoAppenaEseguito = eraDisponibile && !p.isScattoDisponibile();
        return scattoAppenaEseguito;
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
