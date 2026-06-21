package uni.gaben.iscat.utils.design;

/** Costanti e scalatura aurea su {@code float} e {@code double}. */
public final class ScalareAureo {

    private ScalareAureo() {}

    public static final double PHI_D  = 1.618033988749895;
    public static final double IPHI_D = 0.618033988749895;

    /** @return {@code valore * PHI_D^livello} */
    public static double scalaAurea(double valore, int livello) {
        return valore * Math.pow(PHI_D, livello);
    }
    public static double phiMinore(double v)   { return v * IPHI_D; }
    public static double phiMaggiore(double v) { return v * PHI_D; }

}
