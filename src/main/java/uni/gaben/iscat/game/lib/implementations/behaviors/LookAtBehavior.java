package uni.gaben.iscat.game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.utils.Spring;

/**
 * Comportamento che ruota l'entità verso un bersaglio (di default il giocatore).
 * Usa un sistema a molla ({@link Spring}) per rotazioni fluide e naturali.
 *
 * <h2>Imprecisione simulata</h2>
 * <p>Quando {@code accuracy < 1.0}, un offset angolare deriva lentamente verso nuovi
 * valori casuali a intervalli regolari, producendo un <em>wobble</em> organico.
 * Rispetto a {@code Math.random()} chiamato ogni frame (che genera flickering ad alta
 * frequenza), questo approccio aggiorna il target di jitter ogni
 * {@value #JITTER_INTERVAL_S} secondi e interpola fluidamente tra di essi.</p>
 *
 * <pre>
 *   jitterTarget  ← random ogni JITTER_INTERVAL_S secondi
 *   jitterOffset  ← lerp(jitterOffset, jitterTarget, dt × JITTER_SMOOTHING)
 *   targetAngle   += jitterOffset
 * </pre>
 */
public class LookAtBehavior implements AiBehavior {

    /** Secondi tra un aggiornamento casuale del target di jitter e il successivo. */
    private static final double JITTER_INTERVAL_S = 0.4;
    /** Velocità di interpolazione verso il nuovo target di jitter (1/s). */
    private static final double JITTER_SMOOTHING  = 5.0;
    /** Ampiezza massima del jitter a {@code accuracy = 0}: ±15% di π rad. */
    private static final double JITTER_MAX_RAD    = Math.PI * 0.15;

    private final Spring rotationSpring;
    private final double accuracy;

    private double currentAngle   = 0.0;

    // Stato del jitter: aggiornato a intervalli, interpolato ogni frame
    private double jitterOffset   = 0.0;
    private double jitterTarget   = 0.0;
    private double jitterTimer    = 0.0;

    /**
     * Crea un comportamento di rotazione verso il target.
     *
     * @param stiffness Rigidità della molla (maggiore = rotazione più veloce e reattiva).
     * @param damping   Smorzamento della molla (evita oscillazioni; critico ≈ 2√stiffness).
     * @param accuracy  Precisione del puntamento: {@code 1.0} = perfetto,
     *                  valori più bassi aggiungono un wobble organico crescente.
     */
    public LookAtBehavior(double stiffness, double damping, double accuracy) {
        this.rotationSpring = Spring.critico(0, stiffness, damping);
        this.accuracy       = Math.max(0.0, Math.min(1.0, accuracy));
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        Vector2 target = universe.getPlayer().getTransform().getTranslation();
        Vector2 myPos  = npc.getTransform().getTranslation();

        double dx = target.x - myPos.x;
        double dy = target.y - myPos.y;
        double targetAngle = Math.atan2(dy, dx);

        // Imprecisione: deriva lenta verso un offset casuale invece di rumore per-frame
        if (accuracy < 1.0) {
            targetAngle += computeJitterOffset(dt);
        }

        // Differenza angolare minima (gestione wraparound ±π)
        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += 2.0 * Math.PI;
        while (diff >  Math.PI) diff -= 2.0 * Math.PI;

        rotationSpring.setTarget(currentAngle + diff);
        rotationSpring.update(dt);

        currentAngle = rotationSpring.getPosition();
        npc.getTransform().setRotation(currentAngle);
    }

    // ========================================================================
    // HELPER PRIVATO
    // ========================================================================

    /**
     * Aggiorna e restituisce l'offset di jitter per questo frame.
     *
     * <p>Il target di jitter viene scelto casualmente ogni {@value #JITTER_INTERVAL_S}s;
     * l'offset corrente segue con un lerp esponenziale, producendo una deriva morbida
     * anziché salti istantanei.</p>
     *
     * @param dt Delta time in secondi.
     * @return L'offset angolare corrente in radianti.
     */
    private double computeJitterOffset(double dt) {
        jitterTimer -= dt;
        if (jitterTimer <= 0.0) {
            // Nuovo target casuale scalato per accuracy: più accuracy è bassa, più ampio è il jitter
            double ampiezza = (1.0 - accuracy) * JITTER_MAX_RAD;
            jitterTarget = (Math.random() - 0.5) * 2.0 * ampiezza;
            jitterTimer  = JITTER_INTERVAL_S;
        }
        // Lerp esponenziale: jitterOffset si avvicina a jitterTarget fluidamente
        jitterOffset += (jitterTarget - jitterOffset) * dt * JITTER_SMOOTHING;
        return jitterOffset;
    }
}