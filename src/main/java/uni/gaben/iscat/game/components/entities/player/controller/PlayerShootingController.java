package uni.gaben.iscat.game.components.entities.player.controller;

import uni.gaben.iscat.game.controller.InputHandler;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;

/**
 * Gestisce lo sparo: legge l'input e delega al PlayerModel.
 */
public class PlayerShootingController {

    public void process(InputHandler input, PlayerModel p) {
        if (input.shooting) {
            p.richiestaFuoco();
        }
        p.elaboraFuoco();
    }
}
