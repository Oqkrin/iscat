package uni.gaben.iscat.universe.entities.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Updatable;

/**
 * Modificatore di traiettoria ondulatoria (sinusoidale) per proiettili.
 * Ottimizzato per azzerare le allocazioni e minimizzare l'overhead vettoriale nel game loop.
 */
public class TrajectoryModifier implements Updatable {

    private static final double TWO_PI = 2.0 * Math.PI;

    private final Vector2 direction;
    private final Vector2 perpendicular;
    private double amplitude;
    private double frequency;
    private double phaseOffset;
    private double elapsedTime;

    // Vettori di scratch pre-allocati per i calcoli inline
    private final Vector2 lateralScratch = UU.vector2zero();
    private final Vector2 trajectoryOut   = UU.vector2zero();

    public TrajectoryModifier(Vector2 direction, Vector2 perpendicular, double amplitude, double frequency) {
        this(direction, perpendicular, amplitude, frequency, 0.0);
    }

    public TrajectoryModifier(Vector2 direction, Vector2 perpendicular, double amplitude, double frequency, double phaseOffset) {
        this.direction = direction.getNormalized();
        this.perpendicular = perpendicular.getNormalized();
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phaseOffset = phaseOffset;
        this.elapsedTime = 0.0;
    }

    @Override
    public void update(double dt) {
        elapsedTime += dt;
    }

    /**
     * Calcola il vettore di velocità laterale corrente.
     * Ottimizzato con manipolazione diretta delle primitive del vettore.
     */
    public Vector2 getLateralVelocity() {
        double angle = TWO_PI * frequency * elapsedTime + phaseOffset;
        double lateralSpeed = amplitude * Math.sin(angle);

        // Evita il chaining dei metodi operando direttamente sulle coordinate x e y
        lateralScratch.x = perpendicular.x * lateralSpeed;
        lateralScratch.y = perpendicular.y * lateralSpeed;

        return lateralScratch;
    }

    /**
     * Combina la velocità frontale con la componente laterale ondulatoria.
     * Ottimizzato per calcolare la traiettoria finale in un unico passaggio atomico.
     */
    public Vector2 getTrajectory(double forwardSpeed) {
        double angle = TWO_PI * frequency * elapsedTime + phaseOffset;
        double lateralSpeed = amplitude * Math.sin(angle);

        // Calcolo inline combinato per saltare l'overhead dei metodi interni di dyn4j
        trajectoryOut.x = (direction.x * forwardSpeed) + (perpendicular.x * lateralSpeed);
        trajectoryOut.y = (direction.y * forwardSpeed) + (perpendicular.y * lateralSpeed);

        return trajectoryOut;
    }

    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }

    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }

    public Vector2 getDirection() { return direction; }

    /**
     * Resetta il tempo interno per consentire il riutilizzo sicuro del modificatore.
     */
    public void resetTime() {
        elapsedTime = 0.0;
    }
}