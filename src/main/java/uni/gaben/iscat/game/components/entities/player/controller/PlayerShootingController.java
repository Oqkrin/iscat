package uni.gaben.iscat.game.components.entities.player.controller;

import uni.gaben.iscat.game.controller.InputHandler;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.utils.physics.Vec2;

import java.util.function.BiConsumer;

/**
 * Gestisce lo sparo: legge l'input, verifica il cooldown,
 * ottiene i dati di spawn dal modello e notifica il controller di gioco.
 */
public class PlayerShootingController {

    /** Called when a shot fires: receives spawn position and bullet velocity. */
    private BiConsumer<Vec2, Vec2> onSparo;
    /** Called when a shot fires: plays the shot sound. */
    private Runnable onSparoSound;

    public void setOnSparo(BiConsumer<Vec2, Vec2> callback)  { this.onSparo = callback; }
    public void setOnSparoSound(Runnable callback)           { this.onSparoSound = callback; }

    public void process(InputHandler input, PlayerModel p) {
        if (input.shooting && p.isSparoDisponibile()) {
            Vec2[] spawnData = p.getSpawnData(p.getDirectionAngle());
            p.startCooldownFuoco();
            if (onSparo      != null) onSparo.accept(spawnData[0], spawnData[1]);
            if (onSparoSound != null) onSparoSound.run();
        }
    }
}
