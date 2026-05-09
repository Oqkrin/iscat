package uni.gaben.iscat.game.controller;

import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.PlayerSettings;
import uni.gaben.iscat.game.utils.settings.VisualSettings;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Gestisce una camera 2D che segue sempre il giocatore con easing fluido.
 *
 * La camera insegue il centro del giocatore usando {@link Interpolator.Preset#SMOOTHER}.
 * Il rendering applica gc.translate(-cameraX, -cameraY) — nessuna posizione fisica
 * viene modificata, solo la vista.
 *
 * Le stelle seguono la velocità del giocatore tramite SpaceModel.update(),
 * non il movimento della camera.
 */
public class GameScrollController {

    /** Offset corrente della camera (coordinate mondo). */
    private double cameraX = 0;
    private double cameraY = 0;

    /**
     * Processa la camera per questo tick.
     * La camera insegue il centro del giocatore con easing SMOOTHER.
     *
     * @param p il giocatore
     * @param w larghezza canvas
     * @param h altezza canvas
     */
    public void process(PlayerModel p, double w, double h) {
        if (w <= 0 || h <= 0) return;

        // Target: centro del giocatore
        double targetX = p.getX() + PlayerSettings.DIMENSIONE_SPRITE / 2.0 - w / 2.0;
        double targetY = p.getY() + PlayerSettings.DIMENSIONE_SPRITE / 2.0 - h / 2.0;

        // Lerp con easing SMOOTHER verso il target
        cameraX = Interpolator.Preset.SMOOTHER.apply(cameraX, targetX, VisualSettings.LERP_CAMERA);
        cameraY = Interpolator.Preset.SMOOTHER.apply(cameraY, targetY, VisualSettings.LERP_CAMERA);
    }

    /** Offset X della camera da applicare al rendering (gc.translate). */
    public double getCameraX() { return cameraX; }

    /** Offset Y della camera da applicare al rendering (gc.translate). */
    public double getCameraY() { return cameraY; }

    /**
     * Inizializza la camera centrata sul giocatore senza easing.
     * Chiamare una volta dopo lo spawn del giocatore.
     */
    public void snapToPlayer(PlayerModel p, double w, double h) {
        if (w <= 0 || h <= 0) return;
        cameraX = p.getX() + PlayerSettings.DIMENSIONE_SPRITE / 2.0 - w / 2.0;
        cameraY = p.getY() + PlayerSettings.DIMENSIONE_SPRITE / 2.0 - h / 2.0;
    }
}
