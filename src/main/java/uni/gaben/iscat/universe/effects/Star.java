package uni.gaben.iscat.universe.effects;

/**
 * Rappresentazione geometrica bidimensionale di una singola stella di sfondo (Star).
 * <p>
 * Questa classe funge da puro modello dati (Data Carrier) per memorizzare le coordinate
 * posizionali e la dimensione di un punto luminoso all'interno della simulazione spaziale.
 * Viene utilizzata principalmente dagli engine grafici per la gestione di sfondi stellati
 * dinamici o con effetto di parallasse.
 * </p>
 */
public class Star {

    /** Coordinata orizzontale X della stella nello spazio di riferimento. */
    private double x;

    /** Coordinata verticale Y della stella nello spazio di riferimento. */
    private double y;

    /** Dimensione o raggio geometrico della stella espresso in pixel. */
    private final double size;

    /**
     * Costruisce e inizializza un nuovo punto stella con coordinate e dimensioni specifiche.
     *
     * @param x    La coordinata d'origine orizzontale X.
     * @param y    La coordinata d'origine verticale Y.
     * @param size Il diametro o dimensione scalare della stella.
     */
    public Star(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    /** @return La coordinata X attuale della stella. */
    public double getX() { return x; }

    /** @return La coordinata Y attuale della stella. */
    public double getY() { return y; }

    /** @return La dimensione scalare del corpo della stella. */
    public double getSize() { return size; }

    /** @param x La nuova coordinata orizzontale X da assegnare alla stella. */
    public void setX(double x) { this.x = x; }

    /** @param y La nuova coordinata verticale Y da assegnare alla stella. */
    public void setY(double y) { this.y = y; }
}