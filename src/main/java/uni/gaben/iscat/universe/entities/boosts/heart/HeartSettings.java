package uni.gaben.iscat.universe.entities.boosts.heart;

/**
 * Costanti di configurazione globali per l'entità Cuore (Heart Balance Settings).
 * <p>
 * Raggruppa i parametri strutturali relativi al dimensionamento dello Sprite Sheet,
 * alla scala di rendering e alla geometria di collisione del consumabile.
 * </p>
 */
public class HeartSettings {

    /** Dimensione base del lato (larghezza e altezza) dello sprite espressa in pixel ($32\text{ px}$). */
    public static final int DIM_SPRITE = 32;

    /** Fattore di scala geometrico applicato in fase di rendering bidimensionale. */
    public static final double SCALE = 1.0;

    /** * Raggio della fixture circolare di collisione fisica espresso in pixel.
     * Calcolato come il $90\%$ del raggio teorico dello sprite per rendere il raggio di raccolta (pickup) più preciso.
     */
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;

    /** Percorso della risorsa (URI/Path) all'interno del modulo degli asset per il caricamento della texture. */
    public static final String sprite = "/uni/gaben/iscat/sprites/boosts/heart.png";
}