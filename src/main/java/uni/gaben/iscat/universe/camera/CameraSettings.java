package uni.gaben.iscat.universe.camera;

import uni.gaben.iscat.universe.UniverseVelocitySettings;

/**
 * Configurazione centrale dei parametri fisici e del comportamento della telecamera (Camera).
 * <p>
 * Tutti i valori sono statici e finali per garantire un comportamento consistente dell'inerzia
 * e del feedback visivo all'interno dell'applicazione. È possibile calibrare {@link #SPRING_STIFFNESS}
 * e {@link #SPRING_MASS} per modificare la reattività (smoothness/snappiness) con cui la visuale
 * insegue il target di riferimento.
 * </p>
 */
public final class CameraSettings {

    /**
     * Costruttore privato atto a prevenire l'istanza diretta della classe (Utility Class).
     */
    private CameraSettings() {
        // Private constructor to prevent instantiation (utility class)
    }

    /**
     * Costante di rigidità elastica (k) della molla per l'asse orizzontale X.
     * <p>
     * Valori più elevati rendono la risposta della telecamera più immediata rispetto agli spostamenti del target.
     * Il valore è derivato matematicamente dalla velocità massima del giocatore per preservare un rapporto visivo
     * naturale tra l'azione cinematica e il ritardo (lag) della telecamera.
     * </p>
     */
    public static final double SPRING_STIFFNESS = UniverseVelocitySettings.PLAYER_MAX_VELOCITY / 2;

    /**
     * Massa fittizia (m) applicata ai sistemi oscillatori armonici delle molle X e Y.
     * <p>
     * Combinata con un coefficiente di smorzamento pari a 1.0 (smorzamento critico), permette
     * alla telecamera di stabilizzarsi sul target nel minor tempo possibile eliminando qualsiasi effetto di sovrallungamento (overshoot).
     * </p>
     */
    public static final double SPRING_MASS = 0.9;

    /**
     * Moltiplicatore di rigidità applicato selettivamente alla molla dell'asse verticale Y.
     * <p>
     * Il tracciamento verticale viene reso più rigido rispetto a quello orizzontale per mitigare
     * i sobbalzi generati da repentini cambi di quota (es. salti o cadute) e mantenere l'entità
     * focalizzata al centro dello schermo in modo più aggressivo.
     * </p>
     */
    public static final double Y_STIFFNESS_MULTIPLIER = 3.0;

    /** Coefficiente di modifica massimo per l'allontanamento (Zoom Out) dinamico basato sulla velocità dell'entità. */
    public static final double MAX_ZOOM_OUT_MODIFIER = 0.75;

    /** Velocità di interpolazione lineare (Smoothing Speed) applicata alla transizione cinematica dello zoom. */
    public static final double ZOOM_SMOOTHING_SPEED  = 2;

    /** Limite minimo di sicurezza consentito per lo zoom manuale impostato dall'utente. */
    public static final double MIN_MANUAL_ZOOM = .1;

    /** Limite massimo di sicurezza consentito per lo zoom manuale impostato dall'utente. */
    public static final double MAX_MANUAL_ZOOM = 3.0;
}