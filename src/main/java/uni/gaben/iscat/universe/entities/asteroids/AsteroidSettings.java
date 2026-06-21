package uni.gaben.iscat.universe.entities.asteroids;

/**
 * Costanti di configurazione globali per le entità Asteroide (Asteroid Balance Settings).
 * <p>
 * Raggruppa tutti i parametri numerici utilizzati per la generazione procedurale della forma,
 * il bilanciamento dei punti ferita (durabilità) e le soglie di frammentazione cinematica.
 * </p>
 */
public final class AsteroidSettings {

    /** Dimensione massima del diametro dell'asteroide espressa in pixel ($64\text{ px}$). */
    public static final int MAXPXSIZE = 64;

    /** Numero minimo di vertici per la generazione del poligono di base. */
    public static final int MIN_VERTICES = 5;

    /** Variazione casuale massima da sommare al numero minimo di vertici. */
    public static final int VERTICE_VARIATION = 5;

    /** Fattore di scala minimo applicabile al raggio di ogni singolo vertice per creare irregolarità. */
    public static final double RADIUS_VARIATION_MIN = 0.7;

    /** Intervallo (range) di variazione casuale applicabile al raggio del vertice. */
    public static final double RADIUS_VARIATION_RANGE = 0.3;

    /** Durabilità di base dell'asteroide, moltiplicata per la densità/massa del corpo per calcolarne gli HP totali. */
    public static final double BASE_DURABILITY = 20.0;

    /** Fattore di scala che determina il danno netto inflitto dai proiettili all'impatto. */
    public static final double PROJECTILE_DAMAGE_FACTOR = 10.0;

    /** * Soglia limite di dimensione (in pixel). Al di sotto di questo valore l'asteroide si polverizza
     * completamente all'azzeramento degli HP invece di innescare la mitosi in sotto-frammenti.
     */
    public static final double MIN_SPLIT_SIZE = 32.0;

    /**
     * Costruttore privato per prevenire l'istanza di una classe di sole costanti statiche.
     */
    private AsteroidSettings() {}
}