package uni.gaben.iscat.utils;

import org.dyn4j.geometry.Vector2;

/**
 * Motore di utilità per l'interpolazione e il tweening di valori scalari e vettoriali.
 * <p>
 * Questa classe fornisce un set completo di funzioni di easing (equazioni di Robert Penner),
 * polinomi di interpolazione (Hermite e Perlin) e simulazioni fisiche di base (rimbalzo ed elasticità).
 * </p>
 * <p>
 * <b>Principi di Funzionamento:</b>
 * Tutte le funzioni si basano su un parametro di progresso {@code t}, che rappresenta il tempo normalizzato.
 * </p>
 * <ul>
 *     <li>Se <b>t = 0.0</b>: L'animazione è all'inizio (restituisce il valore 'a').</li>
 *     <li>Se <b>t = 1.0</b>: L'animazione è completata (restituisce il valore 'b').</li>
 *     <li>Se <b>0.0 < t < 1.0</b>: Viene calcolato un valore intermedio basato sulla curva specifica.</li>
 * </ul>
 * <p>
 * La classe è <b>stateless</b> e <b>thread-safe</b>, ideale per essere utilizzata all'interno
 * di game loop ad alte prestazioni.
 * </p>
 *
 * @author Gemini 3 Flash
 */
public final class Interpolator {

    // --- Costanti Matematiche per Easing ---

    /**
     * Costante di Penner per l'overshoot (Back).
     * Il valore 1.70158 è calibrato per produrre un superamento del bersaglio pari a circa il 10%
     * dell'ampiezza totale prima del rientro.
     */
    private static final double BACK_S = 1.70158;

    /** Ampiezza standard per l'oscillazione elastica. 1.0 rappresenta l'unità di misura piena. */
    private static final double ELASTIC_AMPLITUDE = 1.0;

    /** Periodo standard dell'oscillazione elastica. Definisce la durata di un singolo ciclo d'onda. */
    private static final double ELASTIC_PERIOD    = 0.3;

    /** Esponente di decadimento energetico per l'oscillazione elastica. Basato sulla funzione 2^(-10t). */
    private static final double ELASTIC_DECAY     = -10.0;

    /** Divisore base per il calcolo delle frequenze di rimbalzo (Bounce). Derivato dalla frazione 11/4. */
    private static final double B_D1 = 11.0 / 4.0;

    /** Fattore di scala dell'accelerazione per il Bounce. Equivale al quadrato di B_D1 (7.5625). */
    private static final double B_N1 = B_D1 * B_D1;

    /** Soglia temporale per il primo impatto del rimbalzo (1.5 / 2.75). */
    private static final double B_P1 = (3.0 / 2.0)  / B_D1;

    /** Soglia temporale per il secondo impatto del rimbalzo (2.25 / 2.75). */
    private static final double B_P2 = (9.0 / 4.0)  / B_D1;

    /** Soglia temporale per il terzo impatto del rimbalzo (2.625 / 2.75). */
    private static final double B_P3 = (21.0 / 8.0) / B_D1;

    /** Offset di ampiezza per il primo rimbalzo (3/4). */
    private static final double B_L1 = 3.0 / 4.0;

    /** Offset di ampiezza per il secondo rimbalzo (15/16). */
    private static final double B_L2 = 15.0 / 16.0;

    /** Offset di ampiezza per il terzo rimbalzo (63/64). */
    private static final double B_L3 = 63.0 / 64.0;

    /**
     * Costruttore privato. Questa classe non deve essere istanziata.
     */
    private Interpolator() {}

    /**
     * Preset di easing pronti all'uso tramite Strategy Pattern.
     * <p>
     * Ogni preset implementa una specifica curva di movimento che può essere passata
     * come parametro a sistemi di animazione o UI.
     * </p>
     */
    public enum Preset {
        /** Interpolazione lineare: movimento a velocità costante, senza accelerazione. */
        LINEAR      { @Override public double apply(double a, double b, double t) { return lerp(a, b, t); } },
        /** Ease-In: parte lentamente e accelera seguendo una curva quadratica. */
        EASE_IN     { @Override public double apply(double a, double b, double t) { return easeIn(a, b, t); } },
        /** Ease-Out: parte velocemente e decelera verso la fine (frenata). */
        EASE_OUT    { @Override public double apply(double a, double b, double t) { return easeOut(a, b, t); } },
        /** Ease-In-InOut: combinazione di accelerazione iniziale e decelerazione finale. */
        EASE_IN_OUT { @Override public double apply(double a, double b, double t) { return smoothStep(a, b, t); } },
        /** Alias di EASE_IN_OUT basato sul polinomio di Hermite. */
        SMOOTH_STEP { @Override public double apply(double a, double b, double t) { return smoothStep(a, b, t); } },
        /** SmootherStep: versione di Perlin con derivate prime e seconde nulle agli estremi. */
        SMOOTHER    { @Override public double apply(double a, double b, double t) { return smootherStep(a, b, t); } },
        /** Bounce: effetto rimbalzo fisico (tipo pallina che cade) verso la fine. */
        BOUNCE_OUT  { @Override public double apply(double a, double b, double t) { return bounceOut(a, b, t); } },
        /** Elastic: effetto molla con oscillazione che supera il target prima di stabilizzarsi. */
        ELASTIC_OUT { @Override public double apply(double a, double b, double t) { return elasticOut(a, b, t); } },
        /** Back: l'oggetto supera leggermente il bersaglio e poi torna indietro (overshoot). */
        BACK_OUT    { @Override public double apply(double a, double b, double t) { return backOut(a, b, t); } };

        /**
         * Applica il preset a due valori scalari.
         *
         * @param a Il valore di partenza.
         * @param b Il valore di arrivo.
         * @param t Il progresso normalizzato da 0.0 a 1.0.
         * @return Il valore interpolato.
         */
        public abstract double apply(double a, double b, double t);

        /**
         * Applica il preset a due vettori Vector2.
         *
         * @param a Il vettore di partenza.
         * @param b Il vettore di arrivo.
         * @param t Il progresso normalizzato da 0.0 a 1.0.
         * @return Un nuovo vettore Vector2 con i componenti x e y interpolati.
         */
        public Vector2 apply(Vector2 a, Vector2 b, double t) {
            return new Vector2(apply(a.x, b.x, t), apply(a.y, b.y, t));
        }
    }

    // ------------------------------------------------------------------ base

    /**
     * Calcola l'interpolazione lineare (Lerp) tra due valori.
     * <p>Formula: f(t) = a + t * (b - a)</p>
     *
     * @param a Valore iniziale.
     * @param b Valore finale.
     * @param t Tempo normalizzato [0, 1].
     * @return Il valore calcolato linearmente.
     */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Calcola l'interpolazione lineare tra due vettori (Lerp Vettoriale).
     *
     * @param a Vettore iniziale.
     * @param b Vettore finale.
     * @param t Tempo normalizzato [0, 1].
     * @return Un nuovo Vector2 interpolato.
     */
    public static Vector2 lerp(Vector2 a, Vector2 b, double t) {
        return new Vector2(lerp(a.x, b.x, t), lerp(a.y, b.y, t));
    }

    /**
     * Esegue una rimappatura proporzionale di un valore da un intervallo ad un altro.
     * <p>Esempio: Trasformare una velocità da 0-100 a un'opacità da 0.0-1.0.</p>
     *
     * @param value Il valore da processare.
     * @param inMin Minimo dell'intervallo di input.
     * @param inMax Massimo dell'intervallo di input.
     * @param outMin Minimo dell'intervallo di output.
     * @param outMax Massimo dell'intervallo di output.
     * @return Il valore mappato proporzionalmente nel nuovo intervallo.
     */
    public static double remap(double value, double inMin, double inMax, double outMin, double outMax) {
        return lerp(outMin, outMax, (value - inMin) / (inMax - inMin));
    }

    // ------------------------------------------------------------------ ease in/out

    /**
     * Calcola un'accelerazione quadratica (Ease-In).
     * <p>Utile per oggetti che partono da fermi aumentando gradualmente la velocità.</p>
     * <p>Formula: t * t</p>
     *
     * @return Valore scalato quadraticamente.
     */
    public static double easeIn(double a, double b, double t) {
        t = unitClamp(t);
        return lerp(a, b, t * t);
    }

    /**
     * Calcola una decelerazione quadratica (Ease-Out).
     * <p>Utile per una frenata dolce verso il bersaglio.</p>
     * <p>Formula: 1 - (1 - t)^2</p>
     *
     * @return Valore scalato per decelerazione.
     */
    public static double easeOut(double a, double b, double t) {
        t = 1.0 - unitClamp(t);
        return lerp(a, b, 1.0 - t * t);
    }

    /**
     * Calcola il SmoothStep basato sul polinomio di Hermite (3t² - 2t³).
     * <p>Fornisce un'accelerazione iniziale e una decelerazione finale simmetriche.</p>
     *
     * @return Valore interpolato con smoothing ai bordi.
     */
    public static double smoothStep(double a, double b, double t) {
        t = unitClamp(t);
        return lerp(a, b, t * t * (3.0 - 2.0 * t));
    }

    /**
     * Versione migliorata del SmoothStep proposta da Ken Perlin.
     * <p>Formula: 6t⁵ - 15t⁴ + 10t³</p>
     * <p>Rispetto allo SmoothStep classico, ha derivate seconde nulle,
     * rendendo il movimento privo di "scatti" (jerk) percettibili.</p>
     *
     * @return Valore estremamente fluido.
     */
    public static double smootherStep(double a, double b, double t) {
        t = unitClamp(t);
        return lerp(a, b, t * t * t * (t * (t * 6.0 - 15.0) + 10.0));
    }

    // ------------------------------------------------------------------ bounce

    /**
     * Applica un effetto di rimbalzo fisico (Bounce).
     * <p>Simula una caduta con perdita di energia cinetica ad ogni impatto.</p>
     * <p>La funzione è implementata come una quadratica a tratti (4 segmenti).</p>
     *
     * @return Valore rimbalzante.
     */
    public static double bounceOut(double a, double b, double t) {
        t = unitClamp(t);
        double f;
        if (t < 1.0 / B_D1) {
            f = B_N1 * t * t;
        } else if (t < 2.0 / B_D1) {
            f = B_N1 * (t -= B_P1) * t + B_L1;
        } else if (t < 2.5 / B_D1) {
            f = B_N1 * (t -= B_P2) * t + B_L2;
        } else {
            f = B_N1 * (t -= B_P3) * t + B_L3;
        }
        return lerp(a, b, f);
    }

    // ------------------------------------------------------------------ elastic

    /**
     * Simula il comportamento di un oscillatore armonico smorzato (Molla).
     * <p>L'oggetto oscilla oltre il bersaglio e si stabilizza gradualmente.</p>
     *
     * @param a Valore iniziale.
     * @param b Valore finale.
     * @param t Tempo normalizzato.
     * @param amplitude L'ampiezza massima dell'oscillazione.
     * @param period Il tempo impiegato per un ciclo completo.
     * @return Valore elastico interpolato.
     */
    public static double elasticOut(double a, double b, double t, double amplitude, double period) {
        t = unitClamp(t);
        if (t == 0 || t == 1.0) return lerp(a, b, t);

        // Fase basata sull'ampiezza (asin(1/amplitude))
        double s = period / (2.0 * Math.PI) * Math.asin(1.0 / amplitude);
        // Esponenziale decrescente (2^-10t) * Sinusoide
        double f = amplitude * Math.pow(2.0, ELASTIC_DECAY * t)
                * Math.sin((t - s) * (2.0 * Math.PI) / period) + 1.0;

        return lerp(a, b, f);
    }

    /**
     * Versione semplificata di ElasticOut con parametri predefiniti
     * (Ampiezza: 1.0, Periodo: 0.3).
     */
    public static double elasticOut(double a, double b, double t) {
        return elasticOut(a, b, t, ELASTIC_AMPLITUDE, ELASTIC_PERIOD);
    }

    // ------------------------------------------------------------------ back

    /**
     * Esegue un effetto "Back", in cui l'animazione supera leggermente il bersaglio
     * prima di stabilizzarsi (Overshoot).
     *
     * @param a Valore iniziale.
     * @param b Valore finale.
     * @param t Tempo normalizzato.
     * @param overshoot L'intensità del superamento.
     * @return Valore con overshoot calcolato.
     */
    public static double backOut(double a, double b, double t, double overshoot) {
        t = unitClamp(t) - 1.0;
        return lerp(a, b, t * t * ((overshoot + 1.0) * t + overshoot) + 1.0);
    }

    /**
     * Versione semplificata di BackOut con overshoot predefinito del 10% (1.70158).
     */
    public static double backOut(double a, double b, double t) {
        return backOut(a, b, t, BACK_S);
    }

    // ------------------------------------------------------------------ utility

    /**
     * Vincola il parametro t nell'intervallo chiuso [0.0, 1.0].
     * <p>Previene comportamenti imprevisti o errori matematici (NaN) nelle funzioni
     * trigonometriche ed esponenziali se il tempo sforasse i limiti.</p>
     *
     * @param t Valore da vincolare.
     * @return Il valore vincolato tra 0 e 1.
     */
    private static double unitClamp(double t) { return Math.clamp(t, 0.0, 1.0); }
}