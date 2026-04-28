package uni.gaben.iscat.utils;

public class RapportoAureo {
    private RapportoAureo() {
        /* Classe utils da non essere istanziata */
    }
    /** Rapporto Aureo Φ */
    public static final float PHI = 1.618f;
    /** Rapporto Aureo Inverso 1/Φ == Φ - 1 */
    public static final float IPHI = 0.618f;
    /**
     ** Scala un valore in base al rapporto aureo. Da utilizzare per ottenere layout armoniosi
     ** @param valore Il valore base da proporzionare.
     * @param livello Il livello di potenza di PHI (positivo per ingrandire, negativo per rimpicciolire).
     * @return Il valore scalato.
     */
    public static float scalaAurea(float valore, int livello) {
        return (float) (valore * Math.pow(PHI, livello));
    }
    /**
     * @see #scalaAurea(float, int)
     ** @return sezione aurea minore (livello -1) */
    public static float phiMinore(float value) {
        return value * IPHI;
    }
    /**
     * @see #scalaAurea(float, int)
     ** @return sezione aurea inferiore (livello 1) */
    public static float phiMaggiore(float value) {
        return value * PHI;
    }
    /**
     ** Scala un valore in base al rapporto aureo. Da utilizzare per ottenere layout armoniosi
     ** @param valore Il valore base da proporzionare.
     * @param livello Il livello di potenza di PHI (positivo per ingrandire, negativo per rimpicciolire).
     * @return Il valore scalato.
     */
    public static double scalaAurea(double valore, int livello) {
        return valore * Math.pow(PHI, livello);
    }
    /**
     * @see #scalaAurea(double, int)
     ** @return sezione aurea inferiore (livello -1) */
    public static double phiMinore(double value) {
        return value * IPHI;
    }
    /** @return sezione aurea maggiore (livello 1) */
    public static double phiMaggiore(double value) {
        return value * PHI;
    }
}
