package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class BoundaryAvoidanceModifier implements MovementModifier {

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        double px = UU.mToPx(self.getTransform().getTranslationX());
        double py = UU.mToPx(self.getTransform().getTranslationY());
        double w = universe.getWidth();
        double h = universe.getHeight();

        // 1. Calcolo dinamico dello spazio di frenata
        double currentSpeedMeters = self.getLinearVelocity().getMagnitude();
        double mass = self.getMass().getMass();
        if (mass <= 0) mass = 1.0;

        double acceleration = maxForce / mass;

        double brakingDistanceMeters = (currentSpeedMeters * currentSpeedMeters) / (2 * acceleration);
        double dynamicMargin = UU.mToPx(brakingDistanceMeters) + 50.0;
        double margin = Math.max(100.0, dynamicMargin);

        // 2. Creiamo un vettore vuoto che conterrà SOLO la forza repulsiva del bordo
        Vector2 repulsionForce = new Vector2();

        // --- ASSE X ---
        if (px < margin) {
            double t = Math.max(0, Math.min(1, (margin - px) / margin));
            repulsionForce.x = maxForce * t; // Spinge verso destra
        } else if (px > w - margin) {
            double t = Math.max(0, Math.min(1, (px - (w - margin)) / margin));
            repulsionForce.x = -maxForce * t; // Spinge verso sinistra
        }

        // --- ASSE Y ---
        if (py < margin) {
            double t = Math.max(0, Math.min(1, (margin - py) / margin));
            repulsionForce.y = maxForce * t; // Spinge verso il basso
        } else if (py > h - margin) {
            double t = Math.max(0, Math.min(1, (py - (h - margin)) / margin));
            repulsionForce.y = -maxForce * t; // Spinge verso l'alto
        }

        // Se vogliamo assicurarci che i bordi abbiano la priorità assoluta su tutto il resto,
        // possiamo moltiplicare questa forza repulsiva per un "peso" maggiore.
        return repulsionForce.multiply(1.5);
    }
}