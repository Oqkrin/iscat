package uni.gaben.iscat.universe.entities.brain.rotation;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.utils.Cooldown;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interfaccia funzionale per la definizione e il calcolo degli obiettivi di orientamento angolare (Rotation Goal).
 * <p>
 * Fornisce un'architettura strategica tramite metodi factory statici per determinare la rotazione ideale
 * di un'entità fisica a seconda del suo stato comportamentale (inseguimento, rotazione uniforme, rotazione a scatti, fermo).
 * </p>
 */
@FunctionalInterface
public interface RotationGoal {

    /**
     * Calcola l'angolo assoluto desiderato per il frame corrente.
     *
     * @param self  L'entità fisica che deve applicare la rotazione.
     * @param world Il modello dell'universo per tracciare le coordinate spaziali.
     * @param dt    Il delta time del frame corrente.
     * @return L'angolo bersaglio espresso in radianti, oppure {@link Double#NaN} se l'entità non deve applicare alcuna forza rotazionale.
     */
    double compute(AbstractPhysicalEntityModel self, UniverseModel world, double dt);

    /**
     * @return Un obiettivo di rotazione costante e statico ancorato all'angolo zero nominale.
     */
    static RotationGoal still()  { return (apem, universe, dt) -> 0.0; }

    /**
     * Orienta dinamicamente l'entità verso il suo vettore di movimento.
     * Se l'entità è ferma (velocità quadratica inferiore alla tolleranza), sospende la rotazione.
     * * @return La direzione angolare del vettore velocità lineare, o {@link Double#NaN}.
     */
    static RotationGoal movement() {
        return (self, world, dt) -> {
            Vector2 vel = self.getLinearVelocity();
            return vel.getMagnitudeSquared() > 0.01 ? vel.getDirection() : Double.NaN;
        };
    }

    /**
     * Traccia cinematicamente la posizione di un bersaglio mobile orientando costantemente la prua dell'entità verso di esso.
     * Gestisce i target nulli o distrutti in modalità Fault-Tolerant evitando crash applicativi.
     *
     * @return La direzione angolare risultante dalla sottrazione vettoriale $(TargetPos - SelfPos)$.
     */
    static RotationGoal target(Target target) {
        AtomicReference<Vector2> pos = new AtomicReference<>(UU.vector2zero());
        return (self, world, dt) -> {
            Vector2 currentTargetPos = target.getPosition(world);

            if (currentTargetPos == null) {
                return Double.NaN;
            }

            pos.set(currentTargetPos);
            return pos.get().subtract(self.getTransform().getTranslation()).getDirection();
        };
    }

    /**
     * @return Un obiettivo passivo che non richiede alcuna correzione angolare.
     */
    static RotationGoal idle() {
        return (self, world, dt) -> Double.NaN;
    }

    /**
     * Applica una rotazione uniforme, circolare e infinita su se stessi a velocità costante.
     *
     * @param spinSpeedRadiansPerTick Velocità angolare di rotazione espressa in radianti applicata ad ogni secondo.
     */
    static RotationGoal continuesSpin(double spinSpeedRadiansPerTick) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            @Override
            public double compute(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                }
                currentAngle += spinSpeedRadiansPerTick * dt;
                return currentAngle;
            }
        };
    }

    /**
     * Genera una rotazione quantizzata suddivisa in passi angolari simmetrici intervallati da pause temporizzate (effetto radar).
     * Gestisce inversioni di marcia ed elimina l'accumulo di errori di precisione in virgola mobile (drift) tramite hard clamp sul target.
     *
     * @param spinSteps       Numero totale di frazioni discrete in cui suddividere l'angolo giro ($2\pi$).
     * @param stepPauseSec    Tempo di sosta in secondi alla fine di ogni singolo step angolare.
     * @param speedRadPerSec  Velocità angolare di transizione cinetica tra uno step e il successivo.
     */
    static RotationGoal intervalSpin(int spinSteps, double stepPauseSec, double speedRadPerSec) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            private double targetAngle = Double.NaN;
            private final Cooldown pauseTimer = new Cooldown(stepPauseSec);
            private final double stepDelta = Math.TAU / spinSteps;
            private boolean isPaused = true;

            @Override
            public double compute(AbstractPhysicalEntityModel self, UniverseModel world, double dt) {
                // Inizializzazione lazy al primo frame di simulazione valido
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                    targetAngle = currentAngle;
                    pauseTimer.start();
                    isPaused = true;
                }

                if (isPaused) {
                    // STATO DI PAUSA
                    pauseTimer.update(dt);
                    if (pauseTimer.isReady()) {
                        isPaused = false;
                        double direction = Math.signum(speedRadPerSec) >= 0 ? 1.0 : -1.0;
                        targetAngle = currentAngle + (stepDelta * direction);
                    }
                } else {
                    // STATO DI ROTAZIONE CINETICA
                    double direction = Math.signum(speedRadPerSec) >= 0 ? 1.0 : -1.0;
                    currentAngle += speedRadPerSec * dt;

                    // Rilevamento dell'over-shooting dello step angolare target
                    boolean overshot = (direction > 0 && currentAngle >= targetAngle) ||
                            (direction < 0 && currentAngle <= targetAngle);

                    if (overshot) {
                        currentAngle = targetAngle; // Taglio netto dei decimali spuri (Anti-Drift Fix)
                        pauseTimer.start();
                        isPaused = true;
                    }
                }

                self.getTransform().setRotation(currentAngle);
                return currentAngle;
            }
        };
    }

    /**
     * Vincola in modo assoluto l'orientamento ad un angolo immutabile predefinito.
     * Disattiva le forze inerziali e azzera le accelerazioni angolari indotte da collisioni esterne sul corpo rigido.
     *
     * @param angleRadians Orientamento spaziale assoluto in radianti.
     */
    static RotationGoal fixedAngle(double angleRadians) {
        return (self, world, dt) -> {
            self.getTransform().setRotation(angleRadians);
            self.setAngularVelocity(0);
            return angleRadians;
        };
    }
}