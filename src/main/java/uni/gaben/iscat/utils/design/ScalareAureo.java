package uni.gaben.iscat.utils.design;

/** Costanti e scalatura aurea su {@code float} e {@code double}. */
public final class ScalareAureo {

    private ScalareAureo() {}

    public static final float  PHI    = 1.618034f;
    public static final float  IPHI   = 0.618034f;
    public static final double PHI_D  = 1.618033988749895;
    public static final double IPHI_D = 0.618033988749895;

    public static final double ANGOLO_AUREO_GRADI = 137.50776405003785;
    public static final double ANGOLO_AUREO_RAD   = Math.toRadians(ANGOLO_AUREO_GRADI);
    public static final double PROPORZIONE_MAGGIORE = IPHI_D;
    public static final double PROPORZIONE_MINORE   = 1.0 - IPHI_D;

    /** @return {@code valore * PHI^livello} */
    public static float scalaAurea(float valore, int livello) {
        return (float) (valore * Math.pow(PHI, livello));
    }
    public static float  phiMinore(float v)    { return v * IPHI; }
    public static float  phiMaggiore(float v)  { return v * PHI; }

    /** @return {@code valore * PHI_D^livello} */
    public static double scalaAurea(double valore, int livello) {
        return valore * Math.pow(PHI_D, livello);
    }
    public static double phiMinore(double v)   { return v * IPHI_D; }
    public static double phiMaggiore(double v) { return v * PHI_D; }

}
