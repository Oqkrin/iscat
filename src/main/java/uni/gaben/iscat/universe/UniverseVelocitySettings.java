package uni.gaben.iscat.universe;

/**
 * Registro centralizzato e sorgente di verità per tutte le velocità cinematiche del gioco.
 * <p>
 * Tutte le costanti sono espresse in <b>metri al secondo</b> ($m/s$) nel sistema di coordinate del mondo fisico,
 * a meno che il suffisso {@code PX} non indichi esplicitamente un'unità basata sui pixel.
 * </p>
 */
public final class UniverseVelocitySettings {

    private UniverseVelocitySettings() {}

    // Proiettili (Projectiles)
    /** Velocità lineare del proiettile standard generato dal giocatore ($11.0\ m/s$). */
    public static final double PLAYER_PROJECTILE_VELOCITY = 11.0;

    /** Velocità dei proiettili nemici (sincronizzata a quella del giocatore via hardware, $11.0\ m/s$). */
    public static final double ENEMY_PROJECTILE_VELOCITY = PLAYER_PROJECTILE_VELOCITY;

    // Giocatore (Player)
    /** Velocità massima raggiungibile dalla navicella sotto spinta dei propulsori ($8.8\ m/s$). */
    public static final double PLAYER_MAX_VELOCITY    = ENEMY_PROJECTILE_VELOCITY * 0.8;

    /** Magnitudo dell'impulso istantaneo applicato durante la manovra di dash ($11.0\ m/s$). */
    public static final double PLAYER_DASH_IMPULSE    = ENEMY_PROJECTILE_VELOCITY;

    /**
     * Calcola la velocità terminale di un asteroide in base al suo diametro in pixel.
     * <p>
     * Applica una penalità di velocità lineare inversamente proporzionale alla massa visiva:
     * $$v_{term} = \max\left(2.0, 14.0 - \frac{\text{sizePx}}{8.0}\right)$$
     * </p>
     *
     * @param sizePx Diametro del corpo celeste espresso in pixel.
     * @return La velocità terminale massima consentita in metri al secondo ($m/s$).
     */
    public static double asteroidTerminalVelocity(double sizePx) {
        return Math.max(2.0, 14.0 - (sizePx / 8.0));
    }
}