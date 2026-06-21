package uni.gaben.iscat.utils.sprite;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Gestore dell'animazione di uno sprite sheet. Traccia il tempo trascorso
 * e mappa i millisecondi correnti all'indice del frame corretto per la riga di stato attiva.
 */
public class SpriteSheetsAnimator {

    private double internalTime = 0;
    private int currentState   = 0;

    /**
     * Matrice frastagliata (jagged) delle durate: {@code frameDurations[stato][frame]}.
     * Ogni array interno ha una lunghezza pari a {@code framesPerRow[stato]}.
     */
    private DoubleProperty[][] frameDurations;

    /**
     * Costruttore legacy per griglie uniformi (ogni riga ha lo stesso numero di frame).
     * Mantenuto per compatibilità con i vecchi componenti del framework.
     *
     * @param defaultFrameDuration Durata di default di ciascun frame (in secondi).
     * @param framesCount          Numero di frame presenti in ogni riga.
     * @param statesCount          Numero totale di righe (stati di animazione).
     */
    public SpriteSheetsAnimator(double defaultFrameDuration, int framesCount, int statesCount) {
        int[] uniform = new int[statesCount];
        for (int i = 0; i < statesCount; i++) {
            uniform[i] = framesCount;
        }
        buildMatrix(defaultFrameDuration, uniform);
    }

    // ── Inizializzazione Matrice ─────────────────────────────────────────────

    private void buildMatrix(double defaultDuration, int[] framesPerRow) {
        frameDurations = new DoubleProperty[framesPerRow.length][];
        for (int s = 0; s < framesPerRow.length; s++) {
            int count = Math.max(1, framesPerRow[s]);
            frameDurations[s] = new DoubleProperty[count];
            for (int f = 0; f < count; f++) {
                frameDurations[s][f] = new SimpleDoubleProperty(defaultDuration);
            }
        }
    }

    /**
     * Re-inizializza la matrice impostando una durata uniforme.
     * Trattenuto esclusivamente per retrocompatibilità.
     */
    public void constantDurationFiller(double defaultFrameDuration, int framesCount, int statesCount) {
        int[] uniform = new int[statesCount];
        for (int i = 0; i < statesCount; i++) {
            uniform[i] = framesCount;
        }
        buildMatrix(defaultFrameDuration, uniform);
    }

    // ── Riproduzione e Aggiornamento ─────────────────────────────────────────

    /**
     * Avanza il timer interno dell'animazione in base al tempo trascorso dall'ultimo frame.
     *
     * @param deltaTime Tempo trascorso in secondi (tipicamente ricavato dal Game Loop).
     */
    public void update(double deltaTime) {
        internalTime += deltaTime;
    }

    /**
     * Calcola e restituisce l'indice del frame corretto all'interno dello stato attivo,
     * gestendo il ciclo continuo (looping) sulla base delle durate specifiche dei singoli frame.
     */
    public int getCurrentFrame() {
        if (frameDurations == null || frameDurations.length == 0) {
            return 0;
        }

        DoubleProperty[] durations = frameDurations[currentState];
        int len = durations.length;

        double totalDuration = 0;
        for (DoubleProperty p : durations) {
            totalDuration += p.get();
        }
        if (totalDuration <= 0) {
            return 0;
        }

        double timeInCycle = internalTime % totalDuration;
        double accumulated = 0;

        for (int i = 0; i < len; i++) {
            accumulated += durations[i].get();
            if (timeInCycle < accumulated) {
                return i;
            }
        }
        return len - 1;
    }

    /**
     * Cambia lo stato di animazione corrente (la riga dello sprite sheet).
     * Se lo stato è differente da quello attuale, resetta il timer dell'animazione a 0.
     */
    public void setState(int newState) {
        if (frameDurations == null) {
            return;
        }
        if (newState >= 0 && newState < frameDurations.length && newState != currentState) {
            currentState = newState;
            reset();
        }
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int getCurrentState() { return currentState; }
    public double getTime()      { return internalTime; }

    public void setTime(double time) { this.internalTime = time; }
    public void reset()              { this.internalTime = 0; }

    // ── Logica Interna ───────────────────────────────────────────────────────

    private boolean inBounds(int state, int frame) {
        if (state < 0 || state >= frameDurations.length) {
            return false;
        }
        return frame >= 0 && frame < frameDurations[state].length;
    }
}