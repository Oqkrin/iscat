package uni.gaben.iscat.utils.sprite;

public class AnimationController {
    private double internalTime = 0;
    private double speedMultiplier = 1.0;
    private int currentState = 0;

    /**
     * Aggiorna il timer interno.
     * @param deltaTime Tempo trascorso dall'ultimo frame (in secondi, es. 0.016 per 60FPS)
     */
    public void update(double deltaTime) {
        internalTime += deltaTime * speedMultiplier;
    }

    /**
     * Restituisce l'indice del frame orizzontale.
     * @param framesPerRow Quanti frame ci sono nella riga della sheet
     * @param frameDuration Durata base di un singolo frame (es. 0.1 per 10 FPS)
     */
    public int getCurrentFrameIdx(int framesPerRow, double frameDuration) {
        if (framesPerRow <= 0) return 0;
        return (int) (internalTime / frameDuration) % framesPerRow;
    }

    /**
     * Cambia lo stato (riga). Se lo stato è diverso dal precedente,
     * resetta il tempo per far partire l'animazione dall'inizio.
     */
    public void setState(int newState) {
        if (this.currentState != newState) {
            this.currentState = newState;
            this.internalTime = 0; // Reset per fluidità
        }
    }

    public int getCurrentState() { return currentState; }
    public void setSpeed(double speed) { this.speedMultiplier = speed; }
    public void reset() { this.internalTime = 0; }
}