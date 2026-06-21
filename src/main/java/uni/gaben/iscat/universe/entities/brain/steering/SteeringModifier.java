package uni.gaben.iscat.universe.entities.brain.steering;

import javafx.beans.property.DoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import java.util.List;

/**
 * Interfaccia funzionale per l'applicazione di modificatori e forze cumulative di sterzata (Steering Modifiers).
 * <p>
 * Implementa le estensioni algoritmiche per i comportamenti di gruppo (Flocking/Boids) e l'evitamento degli ostacoli.
 * A differenza degli obiettivi principali di guida, i modificatori lavorano direttamente alterando un vettore
 * di output accumulato ({@code outForce}) scalato tramite proprietà di peso dinamiche ({@link DoubleProperty}).
 * </p>
 */
@FunctionalInterface
public interface SteeringModifier {

    /**
     * Calcola la forza di sterzata specifica e la accumula nel vettore di output fornito.
     *
     * @param self     L'entità fisica che subisce il calcolo della forza.
     * @param world    Il modello globale dell'universo di gioco.
     * @param maxForce La forza massima applicabile per questo modificatore.
     * @param dt       Il delta time del frame corrente.
     * @param outForce Il vettore di output in cui accumulare la forza risultante (passato per riferimento).
     */
    void computeSteer(AbstractPhysicalEntityModel self, UniverseModel world, double maxForce, double dt, Vector2 outForce);

    /**
     * Forza di Separazione (Separation). Previene il sovraffollamento tra entità vicine generando una forza
     * repulsiva inversamente proporzionale alla distanza euclidea con i vicini entro un certo raggio.
     *
     * @param neighborhood     Il fornitore del set di entità vicine (vicinato).
     * @param separationRadius Il raggio massimo di attivazione della repulsione.
     * @param weight           La proprietà dinamica che definisce il peso di questa forza nel blend complessivo.
     */
    static SteeringModifier separation(Target neighborhood, double separationRadius, DoubleProperty weight) {
        Vector2 toNeighbor = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();

            for (AbstractPhysicalEntityModel neighbor : neighbors) {
                if (neighbor == self || neighbor.shouldRemove()) continue;

                toNeighbor.set(selfPos).subtract(neighbor.getTransform().getTranslation());
                double distSq = toNeighbor.getMagnitudeSquared();

                // Verifica se il vicino si trova nella bolla di prossimità metrica
                if (distSq > 0.0001 && distSq < (separationRadius * separationRadius)) {
                    double dist = Math.sqrt(distSq);
                    double strength = 1.0 - (dist / separationRadius); // Più vicino = repulsione più forte

                    toNeighbor.normalize();
                    toNeighbor.multiply(maxForce * strength);
                    outForce.add(toNeighbor);
                }
            }

            if (!outForce.isZero()) {
                outForce.normalize();
                outForce.multiply(maxForce * weight.get());
            }
        };
    }

    /**
     * Forza di Allineamento (Alignment). Sincronizza l'orientamento e il vettore di movimento dell'entità
     * calcolando la media delle velocità lineari di tutti i componenti del vicinato.
     *
     * @param neighborhood Il fornitore del vicinato.
     * @param weight       La proprietà dinamica che definisce il peso di questa forza.
     */
    static SteeringModifier alignment(Target neighborhood, DoubleProperty weight) {
        Vector2 avgVelocity = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            avgVelocity.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            int count = 0;
            for (AbstractPhysicalEntityModel neighbor : neighbors) {
                if (neighbor == self || neighbor.shouldRemove()) continue;

                avgVelocity.add(neighbor.getLinearVelocity());
                count++;
            }

            if (count > 0) {
                avgVelocity.divide(count); // Velocità media desiderata dello stormo
                if (!avgVelocity.isZero()) {
                    avgVelocity.normalize();
                    avgVelocity.multiply(maxForce);
                }
                outForce.set(avgVelocity).subtract(self.getLinearVelocity());
                if (!outForce.isZero()) {
                    outForce.normalize();
                    outForce.multiply(maxForce * weight.get());
                }
            }
        };
    }

    /**
     * Forza di Coesione (Cohesion). Spinge l'entità a convergere verso il centro di massa geometrico
     * (baricentro delle posizioni) formato dai membri del proprio vicinato, mantenendo unito lo stormo.
     *
     * @param neighborhood Il fornitore del vicinato.
     * @param weight       La proprietà dinamica che definisce il peso di questa forza.
     */
    static SteeringModifier cohesion(Target neighborhood, DoubleProperty weight) {
        Vector2 centerOfMass = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            centerOfMass.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (AbstractPhysicalEntityModel neighbor : neighbors) {
                if (neighbor == self || neighbor.shouldRemove()) continue;

                centerOfMass.add(neighbor.getTransform().getTranslation());
                count++;
            }

            if (count > 0) {
                centerOfMass.divide(count); // Coordinate del centro di massa dello stormo
                Vector2 desired = centerOfMass.subtract(selfPos);
                if (!desired.isZero()) {
                    desired.normalize();
                    desired.multiply(maxForce);

                    outForce.set(desired).subtract(self.getLinearVelocity());
                    if (!outForce.isZero()) {
                        outForce.normalize();
                        outForce.multiply(maxForce * weight.get());
                    }
                }
            }
        };
    }

    /**
     * Evitamento Predittivo delle Collisioni (Collision Avoidance). Proietta la traiettoria geometrica
     * delle minacce (es. proiettili o ostacoli mobili) per rilevare l'impatto più imminente. Se intercettato,
     * genera una forza di schivata laterale (ortogonale a 90° rispetto al vettore minaccia) scalata su un coefficiente di urgenza.
     *
     * @param threats           Il fornitore di entità catalogate come minacce potenziali.
     * @param maxPredictionTime Finestra temporale massima di proiezione del vettore futuro.
     * @param avoidRadius       Il raggio della bolla di tolleranza fisica dell'ostacolo.
     * @param weight            Il peso della forza di sterzata applicata.
     */
    static SteeringModifier collisionAvoidance(Target threats, double maxPredictionTime, double avoidRadius, DoubleProperty weight) {
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> entities = threats.getEntities(world);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            AbstractPhysicalEntityModel mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            // --- Analisi Predittiva del Tempo di Impatto Minimo (CPA) ---
            for (AbstractPhysicalEntityModel threat : entities) {
                if (threat == self || threat.shouldRemove()) continue;

                dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
                dv.set(threat.getLinearVelocity()).subtract(selfVel);

                double dvSq = dv.getMagnitudeSquared();
                if (dvSq < 0.0001) continue;

                // Calcolo analitico del tempo di intersezione futuro: t = - (dp • dv) / ||dv||²
                double t = -dp.dot(dv) / dvSq;

                if (t > 0 && t < maxPredictionTime) {
                    double cx = dp.x + (dv.x * t);
                    double cy = dp.y + (dv.y * t);
                    // Rilevamento della violazione della sezione d'urto radiale
                    if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                        if (t < shortestTime) {
                            shortestTime = t;
                            mostImminent = threat;
                        }
                    }
                }
            }

            // --- Calcolo del Vettore di Schivata Laterale Ortogonale ---
            if (mostImminent != null) {
                Vector2 threatVel = mostImminent.getLinearVelocity();

                if (!threatVel.isZero()) {
                    Vector2 bulletDir = threatVel.copy();
                    bulletDir.normalize();

                    // Matrice di rotazione piana di 90°: (-y, x) per isolare la retta perpendicolare
                    Vector2 lateralEvasion = new Vector2(-bulletDir.y, bulletDir.x);

                    // Selezione del semipiano (+ o -) ottimale basata sul prodotto scalare (dot)
                    Vector2 toSelf = selfPos.copy().subtract(mostImminent.getTransform().getTranslation());
                    if (lateralEvasion.dot(toSelf) < 0) {
                        lateralEvasion.multiply(-1); // Inverte il verso se punta verso la minaccia
                    }

                    outForce.set(lateralEvasion);
                } else {
                    // Fallback geometrico in caso di minaccia statica/ancorata (Ostacolo Fisso)
                    outForce.set(selfPos).subtract(mostImminent.getTransform().getTranslation());
                    if (!outForce.isZero()) outForce.normalize();
                }

                // Calcolo del fattore di urgenza (proporzionale alla vicinanza temporale del potenziale impatto)
                double urgency = 1.0 - (shortestTime / maxPredictionTime);
                urgency = Math.clamp(urgency, 0.1, 1.0);

                if (!outForce.isZero()) {
                    outForce.multiply(maxForce * weight.get() * urgency);
                }
            }
        };
    }
}