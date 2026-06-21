package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Gestore grafico per il rendering ad alte prestazioni dei proiettili (Projectile VFX).
 * Disegna la testa del proiettile e la relativa scia di movimento interpolata (trail) tramite primitive grafiche dirette.
 */
public final class ProjectileVFX {

    private ProjectileVFX() {}

    /**
     * Disegna un proiettile renderizzando una scia di cerchi sfumati in dimensione/opacità e un corpo centrale.
     * Le coordinate fornite devono essere già convertite in pixel rispetto alla telecamera (Screen Space).
     *
     * @param gc         Il contesto grafico (GraphicsContext) del canvas di destinazione.
     * @param cx         La coordinata X del centro del proiettile.
     * @param cy         La coordinata Y del centro del proiettile.
     * @param w          La larghezza (diametro orizzontale) della testa del proiettile.
     * @param h          L'altezza (diametro verticale) della testa del proiettile.
     * @param color      Il colore nativo associato al proiettile e alla sua scia.
     * @param trailX1    La coordinata X del punto di origine iniziale della scia (coda estesa).
     * @param trailY1    La coordinata Y del punto di origine iniziale della scia (coda estesa).
     * @param trailX2    La coordinata X di arrivo della scia (ancorata vicino al centro del proiettile).
     * @param trailY2    La coordinata Y di arrivo della scia (ancorata vicino al centro del proiettile).
     * @param trailWidth Lo spessore nominale della linea della scia.
     */
    public static void drawProjectileRaw(GraphicsContext gc, double cx, double cy, double w, double h, Color color,
                                         double trailX1, double trailY1, double trailX2, double trailY2, double trailWidth) {

        gc.setFill(color);

        // Rendering dei nodi di scia (3 ovali con dissolvenza dimensionale e di opacità)
        int numTrails = 3;
        for (int i = 1; i <= numTrails; i++) {
            double progress = (double) i / numTrails;
            double tx = trailX1 + (trailX2 - trailX1) * progress;
            double ty = trailY1 + (trailY2 - trailY1) * progress;

            double tSize = w * (1.0 - progress * 0.5);
            double alpha = 0.6 * (1.0 - progress);

            gc.setGlobalAlpha(alpha);
            gc.fillOval(tx - tSize / 2, ty - tSize / 2, tSize, tSize);
        }

        // Rendering della testa principale del proiettile
        gc.setGlobalAlpha(1.0);
        gc.fillOval(cx - w / 2, cy - h / 2, w, h);
    }
}