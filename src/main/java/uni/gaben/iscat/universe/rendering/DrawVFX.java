package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.universe.rendering.vfx.InterfaceVFX;
import uni.gaben.iscat.universe.rendering.vfx.ProjectileVFX;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveVFX;
import uni.gaben.iscat.universe.rendering.vfx.ThrustVFX;

/**
 * Facade centralizzato per lo smistamento dei rendering grafici degli effetti visivi (VFX).
 * <p>
 * Funge da ponte di incapsulamento: intercetta i record con visibilità di pacchetto di
 * {@link OptimizedLayeredRenderer} e ne instrada i dati atomici verso i motori grafici del sotto-pacchetto {@code .vfx}.
 * </p>
 */
public final class DrawVFX {

    private DrawVFX() {}

    /** Disegna un'onda d'urto standard (shockwave) tramite il modulo dedicato. */
    public static void drawShockwaveRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        ShockwaveVFX.drawShockwaveRaw(gc, cx, cy, shockwave);
    }

    /** Genera l'orizzonte degli eventi e gli effetti di distorsione visiva di un buco nero. */
    public static void drawBlackHoleRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        ShockwaveVFX.drawBlackHoleRaw(gc, cx, cy, shockwave);
    }

    /** Sviluppa il sistema particellare di spinta (Thrust) della scia dei motori. */
    public static void drawThrustRaw(GraphicsContext gc, double cx, double cy, double angle, Thrust thrust) {
        ThrustVFX.drawThrustRaw(gc, cx, cy, angle, thrust);
    }

    /** Renderizza i testi fluttuanti ed indicatori legati alle variazioni di endurance (danni/cure). */
    public static void drawEnduranceIndicator(EnduranceIndicator enduranceIndicator, GraphicsContext gc) {
        InterfaceVFX.drawEnduranceIndicator(enduranceIndicator, gc);
    }

    /**
     * Estrae i dati geometrici e cromatici da un batch locale di proiettili per il rendering diretto.
     */
    static void drawProjectile(GraphicsContext gc, OptimizedLayeredRenderer.ProjectileBatch p) {
        ProjectileVFX.drawProjectileRaw(
                gc, p.cx(), p.cy(), p.w(), p.h(), p.color(),
                p.trailX1(), p.trailY1(), p.trailX2(), p.trailY2(), p.trailWidth()
        );
    }

    /**
     * Estrae le geometrie e le percentuali di riempimento da un batch locale per il disegno delle barre HP.
     */
    static void drawHpBar(GraphicsContext gc, OptimizedLayeredRenderer.HpBarBatch h) {
        InterfaceVFX.drawHpBarRaw(gc, h.x(), h.y(), h.w(), h.h(), h.percent());
    }

    /**
     * Configura ed inoltra i parametri per il disegno delle barre di cooldown temporale (Time Gauge).
     */
    static void drawTimeGaugeBar(GraphicsContext gc, OptimizedLayeredRenderer.TimeGaugeBarBatch t) {
        InterfaceVFX.drawTimeGaugeBarRaw(gc, t.x(), t.y(), t.w(), t.h(), t.percent());
    }
}