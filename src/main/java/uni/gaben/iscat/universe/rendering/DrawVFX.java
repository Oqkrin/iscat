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
 * Classe di sbarramento e smistamento (Facade Pattern) responsabile della centralizzazione
 * dei rendering grafici degli effetti visivi (VFX).
 * <p>
 * {@code DrawVFX} risiede all'interno del pacchetto {@code uni.gaben.iscat.universe.rendering} al solo scopo
 * di fare da ponte per l'incapsulamento: potendo accedere legittimamente ai record con visibilità di pacchetto
 * definiti in {@link OptimizedLayeredRenderer} (come {@code ProjectileBatch} e {@code HpBarBatch}), estrae da essi
 * i parametri primitivi disaccoppiati e li instrada in modo sicuro verso i motori grafici specializzati nel sotto-pacchetto {@code .vfx}.
 * </p>
 */
public final class DrawVFX {

    /**
     * Costruttore privato per impedire l'istanziamento di questa classe di utility.
     */
    private DrawVFX() {}

    /**
     * Delega il disegno di un'onda d'urto standard (shockwave) al modulo grafico dedicato.
     *
     * @param gc        il {@link GraphicsContext} del canvas su cui disegnare
     * @param cx        la coordinata X del centro dell'onda (screen space)
     * @param cy        la coordinata Y del centro dell'onda (screen space)
     * @param shockwave l'oggetto modello contenente lo stato dell'onda d'urto
     */
    public static void drawShockwaveRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        ShockwaveVFX.drawShockwaveRaw(gc, cx, cy, shockwave);
    }

    /**
     * Delega il disegno degli effetti visivi avanzati di un buco nero (orizzonte degli eventi e onde di distorsione)
     * al modulo grafico dedicato.
     *
     * @param gc        il {@link GraphicsContext} del canvas su cui disegnare
     * @param cx        la coordinata X del centro della singolarità (screen space)
     * @param cy        la coordinata Y del centro della singolarità (screen space)
     * @param shockwave l'oggetto modello da cui estrapolare i dati dell'orizzonte degli eventi
     */
    public static void drawBlackHoleRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        ShockwaveVFX.drawBlackHoleRaw(gc, cx, cy, shockwave);
    }

    /**
     * Delega la generazione del sistema particellare di spinta del motore della nave spaziale
     * al modulo grafico dedicato.
     *
     * @param gc     il {@link GraphicsContext} del canvas su cui disegnare
     * @param cx     la coordinata X del punto di ancoraggio del propulsore (screen space)
     * @param cy     la coordinata Y del punto di ancoraggio del propulsore (screen space)
     * @param angle  l'angolo di rotazione attuale della nave (in gradi)
     * @param thrust il modello contenente lo stato dinamico e i dati fisici di spinta
     */
    public static void drawThrustRaw(GraphicsContext gc, double cx, double cy, double angle, Thrust thrust) {
        ThrustVFX.drawThrustRaw(gc, cx, cy, angle, thrust);
    }

    /**
     * Delega il disegno dell'interfaccia fluttuante dei testi dei danni o delle cure (endurance)
     * al modulo grafico dell'interfaccia.
     *
     * @param enduranceIndicator l'oggetto contenente il valore numerico, la posizione e l'opacità del testo fluttuante
     * @param gc                 il {@link GraphicsContext} del canvas su cui disegnare
     */
    public static void drawEnduranceIndicator(EnduranceIndicator enduranceIndicator, GraphicsContext gc) {
        InterfaceVFX.drawEnduranceIndicator(enduranceIndicator, gc);
    }

    /**
     * Estrae i dati geometrici e cromatici da un pacchetto batch protetto di proiettili e li
     * inoltra al rendering nel modulo esterno.
     * <p>
     * Questo metodo ha visibilità {@code package-private} per intercettare in modo sicuro le strutture dati locali di
     * {@link OptimizedLayeredRenderer} senza esporle all'esterno del pacchetto di appartenenza.
     * </p>
     *
     * @param gc il {@link GraphicsContext} del canvas su cui disegnare
     * @param p  il record di batch contenente le informazioni condensate del proiettile e della sua scia (trail)
     */
    static void drawProjectile(GraphicsContext gc, OptimizedLayeredRenderer.ProjectileBatch p) {
        ProjectileVFX.drawProjectileRaw(
                gc, p.cx(), p.cy(), p.w(), p.h(), p.color(),
                p.trailX1(), p.trailY1(), p.trailX2(), p.trailY2(), p.trailWidth()
        );
    }

    /**
     * Estrae le informazioni di riempimento e posizionamento da un pacchetto batch protetto di una barra della salute (HP)
     * e le inoltra al disegno diretto nel modulo dell'interfaccia.
     * <p>
     * Questo metodo ha visibilità {@code package-private} per intercettare in modo sicuro le strutture dati locali di
     * {@link OptimizedLayeredRenderer} senza esporle all'esterno del pacchetto di appartenenza.
     * </p>
     *
     * @param gc il {@link GraphicsContext} del canvas su cui disegnare
     * @param h  il record di batch contenente le dimensioni e la percentuale di riempimento della barra della vita
     */
    static void drawHpBar(GraphicsContext gc, OptimizedLayeredRenderer.HpBarBatch h) {
        InterfaceVFX.drawHpBarRaw(gc, h.x(), h.y(), h.w(), h.h(), h.percent());
    }
}