package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Utility class responsabile del rendering dei proiettili e delle relative scie di movimento (trail)
 * all'interno del mondo di gioco.
 * <p>
 * Questa classe non è istanziabile e fornisce esclusivamente metodi statici basati su parametri primitivi.
 * Questo approccio evita il disallineamento dei pacchetti rispetto ai record di rendering protetti
 * e massimizza la velocità di esecuzione eliminando l'allocazione di oggetti extra nel loop grafico.
 * </p>
 */
public final class ProjectileVFX {

    /**
     * Costruttore privato per prevenire l'istanziamento della classe utility.
     */
    private ProjectileVFX() {}

    /**
     * Disegna un singolo proiettile composto da una scia lineare semi-trasparente (trail)
     * e da un corpo sferico principale (testa del proiettile).
     * <p>
     * Le coordinate geometriche fornite devono essere già proiettate in pixel rispetto alla visuale
     * corrente della telecamera (screen space).
     * </p>
     *
     * @param gc         il {@link GraphicsContext} del canvas su cui effettuare il disegno
     * @param cx         la coordinata X del centro del proiettile in pixel
     * @param cy         la coordinata Y del centro del proiettile in pixel
     * @param w          la larghezza (diametro orizzontale) del proiettile in pixel
     * @param h          l'altezza (diametro verticale) del proiettile in pixel
     * @param color      il {@link Color} del proiettile e della sua scia
     * @param trailX1    la coordinata X iniziale della scia di movimento
     * @param trailY1    la coordinata Y iniziale della scia di movimento
     * @param trailX2    la coordinata X finale della scia di movimento (generalmente vicina al centro del proiettile)
     * @param trailY2    la coordinata Y finale della scia di movimento (generalmente vicina al centro del proiettile)
     * @param trailWidth lo spessore della linea della scia in pixel
     */
    public static void drawProjectileRaw(GraphicsContext gc, double cx, double cy, double w, double h, Color color,
                                         double trailX1, double trailY1, double trailX2, double trailY2, double trailWidth) {
        
        gc.setFill(color);
        
        // Draw 3 trailing circles
        int numTrails = 3;
        for (int i = 1; i <= numTrails; i++) {
            double progress = (double) i / numTrails;
            double tx = trailX1 + (trailX2 - trailX1) * progress;
            double ty = trailY1 + (trailY2 - trailY1) * progress;
            
            double tSize = w * (1.0 - progress * 0.5); // Shrink trail slightly
            double alpha = 0.6 * (1.0 - progress);
            
            gc.setGlobalAlpha(alpha);
            gc.fillOval(tx - tSize / 2, ty - tSize / 2, tSize, tSize);
        }

        // Draw main head with full alpha and a subtle glow effect
        gc.setGlobalAlpha(1.0);
        gc.fillOval(cx - w / 2, cy - h / 2, w, h);
    }
}