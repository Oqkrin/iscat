package uni.gaben.iscat.universe.effects;

import org.dyn4j.geometry.Vector2;

/**
 * Modello dati e configurazione per l'effetto di spinta dei motori (Thrust).
 * <p>
 * Questa classe incapsula i parametri geometrici e cinematici necessari agli emettitori
 * di particelle per generare la scia dei propulsori delle navicelle (Jet Thrust/Engine Trail).
 * </p>
 * <p>
 * Memorizza costanti di densità e variazione per il burst particellare, calcolando in tempo reale
 * l'intensità della spinta basandosi sul rapporto tra la velocità corrente e la velocità massima.
 * </p>
 */
public class Thrust {

    // === Costanti di configurazione per gli effetti particellari del motore ===
    /** Quantità minima garantita di particelle da generare per fotogramma. */
    public static final int THRUST_MIN_PARTICLES = 12;
    /** Coefficiente moltiplicativo aggiuntivo per il calcolo del numero di particelle in massima spinta. */
    public static final int THRUST_EXTRA_PARTICLES = 48;
    /** Fattore di dispersione (spread) orizzontale orizzontale sull'asse X locale. */
    public static final double THRUST_SPREAD_X_FACTOR = 1.0;
    /** Dimensione minima in pixel mondo per ogni singola unità particellare della scia. */
    public static final double THRUST_MIN_PARTICLE_SIZE = 1.0;
    /** Delta massimo di variazione casuale da sommare alla dimensione minima della particella. */
    public static final double THRUST_PARTICLE_SIZE_VARIATION = 7.0;

    /** Intensità corrente della spinta, normalizzata all'interno del range {@code [0.0, 1.0]}. */
    private double intensity;
    /** Vettore di deriva/scostamento laterale (drift) calcolato nello spazio locale dell'entità. */
    private final Vector2 localDrift;
    /** Larghezza complessiva della navicella espressa in pixel. */
    private double shipWidth;
    /** Altezza complessiva della navicella espressa in pixel. */
    private double shipHeight;
    /** Flag di controllo per determinare se l'emettitore è attivo e deve produrre particelle. */
    private boolean active = true;

    /**
     * Costruisce un nuovo oggetto Thrust inizializzando il vettore di deriva locale.
     */
    public Thrust() {
        this.localDrift = new Vector2();
    }

    /**
     * Aggiorna lo stato cinematico e le dimensioni di riferimento del sistema di spinta.
     *
     * @param intensity  Il nuovo livello di intensità del motore (viene forzato tra 0.0 e 1.0).
     * @param localDrift Il vettore {@link Vector2} indicante lo scostamento laterale corrente.
     * @param shipWidth  La larghezza aggiornata della navicella in pixel.
     * @param shipHeight L'altezza aggiornata della navicella in pixel.
     */
    public void update(double intensity, Vector2 localDrift, double shipWidth, double shipHeight) {
        this.intensity = Math.clamp(intensity, 0.0, 1.0);
        this.localDrift.set(localDrift);
        this.shipWidth = shipWidth;
        this.shipHeight = shipHeight;
    }

    /** @return L'intensità di spinta corrente (valore normalizzato tra 0.0 e 1.0). */
    public double getIntensity() { return intensity; }

    /** @return Il vettore di deriva laterale espresso nello spazio di coordinate locali. */
    public Vector2 getLocalDrift() { return localDrift; }

    /** @return La larghezza attuale della navicella associata in pixel. */
    public double getShipWidth() { return shipWidth; }

    /** @return L'altezza attuale della navicella associata in pixel. */
    public double getShipHeight() { return shipHeight; }

    /** @return {@code true} se l'effetto propulsore è abilitato, {@code false} se spento. */
    public boolean isActive() { return active; }

    /** @param active Imposta lo stato di attivazione dell'emettitore di spinta. */
    public void setActive(boolean active) { this.active = active; }
}