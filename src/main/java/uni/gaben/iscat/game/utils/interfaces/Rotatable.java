package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità con un angolo di direzione (facing).
 *
 * Separato da {@link Alive} perché non tutte le entità viventi hanno una direzione
 * (es. una torretta fissa, un ostacolo).
 * Separato da {@link Physical} perché non tutte le entità fisiche ruotano
 * (es. un proiettile punta sempre nella direzione della velocità).
 */
public interface Rotatable {

    /** Angolo di direzione in gradi (0° = est, 90° = sud). */
    double getDirectionAngle();

    void setDirectionAngle(double angle);

    /**
     * Aggiorna la direzione in modo smooth verso il target.
     * @param dx         differenza x verso il target
     * @param dy         differenza y verso il target
     * @param smoothing  fattore di interpolazione (0-1). Più basso = rotazione più lenta.
     */
    default void updateDirectionSmooth(double dx, double dy, double smoothing) {
        double targetAngle = Math.toDegrees(Math.atan2(dy, dx));
        double angleDiff   = targetAngle - getDirectionAngle();
        while (angleDiff >  180) angleDiff -= 360;
        while (angleDiff < -180) angleDiff += 360;
        setDirectionAngle(getDirectionAngle() + angleDiff * smoothing);
    }
}
