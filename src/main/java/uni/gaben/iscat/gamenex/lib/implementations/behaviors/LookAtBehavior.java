package uni.gaben.iscat.gamenex.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.utils.Spring;

/**
 * Comportamento che ruota l'entità per guardare verso un bersaglio (solitamente il giocatore).
 * Utilizza un sistema a molla (Spring) per garantire rotazioni fluide e naturali.
 */
public class LookAtBehavior implements AiBehavior {
    private final Spring rotationSpring;
    private double currentAngle = 0;
    private final double accuracy; // 1.0 = perfetto, valori più bassi = ritardo o jitter

    /**
     * Crea un nuovo comportamento di rotazione.
     * @param stiffness Rigidità della molla (maggiore = rotazione più veloce).
     * @param damping Smorzamento della molla (evita oscillazioni eccessive).
     * @param accuracy Precisione del puntamento (0.0 - 1.0).
     */
    public LookAtBehavior(double stiffness, double damping, double accuracy) {
        this.rotationSpring = Spring.critico(0, stiffness, damping);
        this.accuracy = accuracy;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        Vector2 target = universe.getPlayer().getTransform().getTranslation();
        Vector2 myPos = npc.getTransform().getTranslation();
        
        double dx = target.x - myPos.x;
        double dy = target.y - myPos.y;
        double targetAngle = Math.atan2(dy, dx);

        // Fattore di precisione: aggiunge un leggero ritardo o errore se accuracy < 1.0
        if (accuracy < 1.0) {
            // Simulazione semplice di errore: il bersaglio è leggermente sfalsato
            targetAngle += (Math.random() - 0.5) * (1.0 - accuracy);
        }

        // Calcolo della differenza angolare minima (gestione overflow 360°)
        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;

        rotationSpring.setTarget(currentAngle + diff);
        rotationSpring.update(dt);

        currentAngle = rotationSpring.getPosition();
        npc.getTransform().setRotation(currentAngle);
    }
}
