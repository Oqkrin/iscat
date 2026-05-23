package uni.gaben.iscat.utils.sprite;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class SpriteSheetsAnimator {
    private double internalTime = 0;
    private double speedMultiplier = 1.0;
    private int currentState = 0;

    // Matrice di Properties: [Stato][Frame]
    private DoubleProperty[][] frameDurations;

    public SpriteSheetsAnimator(double defaultFrameDuration, int framesCount, int statesCount) {
        constantDurationFiller(defaultFrameDuration, framesCount, statesCount);
    }

    /**
     * Inizializza la matrice con durate costanti tramite DoubleProperty.
     */
    public void constantDurationFiller(double defaultFrameDuration, int framesCount, int statesCount) {
        frameDurations = new DoubleProperty[statesCount][framesCount];

        // Dobbiamo usare un doppio loop per creare istanze separate,
        // evitando il bug dei reference di Arrays.fill()
        for (int state = 0; state < statesCount; state++) {
            for (int frame = 0; frame < framesCount; frame++) {
                frameDurations[state][frame] = new SimpleDoubleProperty(defaultFrameDuration);
            }
        }
    }

    /**
     * Restituisce la Property di un frame per poterla bindare (es. a una statistica).
     */
    public DoubleProperty durationProperty(int state, int frame) {
        return frameDurations[state][frame];
    }

    /**
     * Imposta manualmente la durata senza binding.
     */
    public void setFrameDuration(int state, int frame, double duration) {
        if (frameDurations == null) return;
        frameDurations[state % frameDurations.length][frame % frameDurations[0].length].set(duration);
    }

    private long lastUpdateNanos = 0;

    public void update(double deltaTime) {
        long now = System.nanoTime();
        if (now - lastUpdateNanos > 5_000_000) { // Throttle a 5ms per evitare il bug del double-update nei singleton View
            internalTime += deltaTime * speedMultiplier;
            lastUpdateNanos = now;
        }
    }

    /**
     * Calcola l'indice del frame orizzontale in base ai tempi variabili.
     */
    public int getCurrentFrame() {
        if (frameDurations == null || frameDurations.length == 0) return 0;

        DoubleProperty[] currentDurations = frameDurations[currentState];

        // 1. Calcoliamo la durata totale dell'intera animazione (somma di tutti i frame)
        double totalStateDuration = 0;
        for (DoubleProperty prop : currentDurations) {
            totalStateDuration += prop.get();
        }

        if (totalStateDuration <= 0) return 0; // Prevenzione errori

        // 2. Troviamo a che punto siamo del ciclo (il resto del tempo diviso la durata totale)
        double timeInCycle = internalTime % totalStateDuration;

        // 3. Scorriamo i frame finché non "consumiamo" il timeInCycle
        double accumulatedTime = 0;
        for (int i = 0; i < currentDurations.length; i++) {
            accumulatedTime += currentDurations[i].get();
            if (timeInCycle < accumulatedTime) {
                return i; // Siamo dentro questo frame!
            }
        }

        return currentDurations.length - 1; // Fallback di sicurezza all'ultimo frame
    }

    public void setState(int newState) {
        if (this.currentState != newState && newState < frameDurations.length) {
            this.currentState = newState;
            reset();
        }
    }

    public int getCurrentState() { return currentState; }
    public void setSpeed(double speed) { this.speedMultiplier = speed; }
    public void reset() { this.internalTime = 0; }

    public boolean hasCompletedCycle() {
        if (frameDurations == null || frameDurations.length == 0) return true;

        DoubleProperty[] currentDurations = frameDurations[currentState];
        double totalStateDuration = 0;
        for (DoubleProperty prop : currentDurations) {
            totalStateDuration += prop.get();
        }

        return internalTime >= totalStateDuration;
    }
}