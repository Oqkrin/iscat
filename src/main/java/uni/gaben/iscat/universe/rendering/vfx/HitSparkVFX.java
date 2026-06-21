package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.rendering.OptimizedLayeredRenderer;

/**
 * Gestore grafico degli effetti di impatto e scintille (Hit Spark VFX).
 * Estrae i vettori delle particelle e le onde d'urto locali, inserendoli nel sistema di batching.
 */
public final class HitSparkVFX {

    private HitSparkVFX() {}

    /**
     * Scompone il modello dell'impatto e ne accoda le componenti particellari e geometriche nel renderer stratificato.
     *
     * @param model  Il modello contenente lo stato dinamico dell'effetto d'impatto.
     * @param layers Il renderer ottimizzato a cui delegare il disegno differito.
     */
    public static void renderHitSpark(HitSpark model, OptimizedLayeredRenderer layers) {
        if (model == null || model.isExpired()) return;

        // --- Rendering del Sistema Particellare ---
        for (HitSpark.SparkParticle p : model.getParticles()) {
            if (p.isDead()) continue;
            double r = p.getRadius();
            Color color = p.getColor().deriveColor(1, 1, 1, p.getAlpha());
            layers.addFilledOval(p.getX() - r, p.getY() - r, r * 2, r * 2, color, 1.0);
        }

        // --- Rendering dell'Onda d'Urto Perimetrale ---
        HitSpark.ShockwaveCircle sw = model.getShockwave();
        if (!sw.isDead() && sw.getAlpha() > 0.01) {
            double radius = sw.getRadius();
            Color color = Color.WHITE.deriveColor(1, 1, 1, sw.getAlpha());
            layers.addStrokedOval(sw.getCenterX() - radius, sw.getCenterY() - radius, radius * 2, radius * 2, color, sw.getLineWidth());
        }
    }
}