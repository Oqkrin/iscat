package uni.gaben.iscat.game.player.controller;

import uni.gaben.iscat.game.input.InputHandler;
import uni.gaben.iscat.game.GameSettings;
import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.player.PlayerSettings;
import uni.gaben.iscat.game.space.SpaceModel;

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
