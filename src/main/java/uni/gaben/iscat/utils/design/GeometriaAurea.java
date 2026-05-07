package uni.gaben.iscat.utils.design;

import javafx.geometry.*;
import javafx.scene.layout.CornerRadii;

import static uni.gaben.iscat.utils.design.ScalareAureo.*;

/**
 * Applicazione del rapporto aureo a primitive geometriche JavaFX:
 * {@link Rectangle2D}, {@link Dimension2D}, {@link Insets}, {@link Point2D}, {@link BoundingBox}.
 */
public final class GeometriaAurea {

    private GeometriaAurea() {}

    // --- Rectangle2D ---

    /** Riduce {@code r} con phiMinore, mantenendolo centrato. */
    public static Rectangle2D phiMinore(Rectangle2D r) {
        double w = ScalareAureo.phiMinore(r.getWidth());
        double h = ScalareAureo.phiMinore(r.getHeight());
        return new Rectangle2D(r.getMinX() + (r.getWidth() - w) / 2.0,
                               r.getMinY() + (r.getHeight() - h) / 2.0, w, h);
    }

    /** Espande {@code r} con phiMaggiore, mantenendolo centrato. */
    public static Rectangle2D phiMaggiore(Rectangle2D r) {
        double w = ScalareAureo.phiMaggiore(r.getWidth());
        double h = ScalareAureo.phiMaggiore(r.getHeight());
        return new Rectangle2D(r.getMinX() - (w - r.getWidth()) / 2.0,
                               r.getMinY() - (h - r.getHeight()) / 2.0, w, h);
    }

    /** Rettangolo aureo da larghezza (altezza = larghezza × IPHI), origine (0,0). */
    public static Rectangle2D rect2DaLarghezza(double larghezza) {
        return new Rectangle2D(0, 0, larghezza, ScalareAureo.phiMinore(larghezza));
    }

    /** Rettangolo aureo da altezza (larghezza = altezza × PHI), origine (0,0). */
    public static Rectangle2D rect2DaAltezza(double altezza) {
        return new Rectangle2D(0, 0, ScalareAureo.phiMaggiore(altezza), altezza);
    }

    // --- Dimension2D ---

    public static Dimension2D phiMinore(Dimension2D d) {
        return new Dimension2D(ScalareAureo.phiMinore(d.getWidth()), ScalareAureo.phiMinore(d.getHeight()));
    }

    public static Dimension2D phiMaggiore(Dimension2D d) {
        return new Dimension2D(ScalareAureo.phiMaggiore(d.getWidth()), ScalareAureo.phiMaggiore(d.getHeight()));
    }

    public static Dimension2D dim2DaLarghezza(double larghezza) {
        return new Dimension2D(larghezza, ScalareAureo.phiMinore(larghezza));
    }

    public static Dimension2D dim2DaAltezza(double altezza) {
        return new Dimension2D(ScalareAureo.phiMaggiore(altezza), altezza);
    }

    // --- Insets ---

    public static Insets phiMinore(Insets i) {
        return new Insets(ScalareAureo.phiMinore(i.getTop()), ScalareAureo.phiMinore(i.getRight()),
                          ScalareAureo.phiMinore(i.getBottom()), ScalareAureo.phiMinore(i.getLeft()));
    }

    public static Insets phiMaggiore(Insets i) {
        return new Insets(ScalareAureo.phiMaggiore(i.getTop()), ScalareAureo.phiMaggiore(i.getRight()),
                ScalareAureo.phiMaggiore(i.getBottom()), ScalareAureo.phiMaggiore(i.getLeft()));
    }

    /** Padding orizzontale = base, verticale = base × IPHI. Ideale per pulsanti. */
    public static Insets insetsOrizzontali(double base) {
        return new Insets(ScalareAureo.phiMinore(base), base, ScalareAureo.phiMinore(base), base);
    }

    /** Padding verticale = base, orizzontale = base × PHI. Ideale per pannelli. */
    public static Insets insetsVerticali(double base) {
        return new Insets(base, ScalareAureo.phiMaggiore(base), base, ScalareAureo.phiMaggiore(base));
    }

    // --- Point2D ---

    public static Point2D phiMaggiore(Point2D p) {
        return new Point2D(ScalareAureo.phiMaggiore(p.getX()), ScalareAureo.phiMaggiore(p.getY()));
    }

    public static Point2D phiMinore(Point2D p) {
        return new Point2D(ScalareAureo.phiMinore(p.getX()), ScalareAureo.phiMinore(p.getY()));
    }

    /** @return Punto scalato di {@code passi} applicazioni di IPHI_D. */
    public static Point2D scalaAurea(Point2D p, int passi) {
        double f = Math.pow(IPHI_D, passi);
        return new Point2D(p.getX() * f, p.getY() * f);
    }

    /**
     * Punto sulla spirale aurea logaritmica.
     * @param angolo angolo in radianti
     * @param scala  raggio al primo giro
     */
    public static Point2D puntoSpirale(double angolo, double scala) {
        double b = Math.log(PHI_D) / (Math.PI / 2.0);
        double r = scala * Math.exp(b * angolo);
        return new Point2D(r * Math.cos(angolo), r * Math.sin(angolo));
    }

    /**
     * Distribuisce {@code n} punti con l'angolo aureo (phyllotaxis di Vogel).
     * @param scala raggio massimo in pixel
     */
    public static Point2D[] distribuzione(int n, double scala) {
        Point2D[] pts = new Point2D[n];
        for (int i = 0; i < n; i++) {
            double r = scala * Math.sqrt((double) i / n);
            pts[i] = new Point2D(r * Math.cos(i * ANGOLO_AUREO_RAD),
                                 r * Math.sin(i * ANGOLO_AUREO_RAD));
        }
        return pts;
    }

    // --- BoundingBox ---

    public static BoundingBox phiMinore(BoundingBox b) {
        double w = ScalareAureo.phiMinore(b.getWidth());
        double h = ScalareAureo.phiMinore(b.getHeight());
        double d = ScalareAureo.phiMinore(b.getDepth());
        return new BoundingBox(b.getMinX() + (b.getWidth() - w) / 2.0,
                               b.getMinY() + (b.getHeight() - h) / 2.0,
                               b.getMinZ() + (b.getDepth() - d) / 2.0, w, h, d);
    }

    public static BoundingBox phiMaggiore(BoundingBox b) {
        double w = ScalareAureo.phiMaggiore(b.getWidth());
        double h = ScalareAureo.phiMaggiore(b.getHeight());
        double d = ScalareAureo.phiMaggiore(b.getDepth());
        return new BoundingBox(b.getMinX() - (w - b.getWidth()) / 2.0,
                               b.getMinY() - (h - b.getHeight()) / 2.0,
                               b.getMinZ() - (d - b.getDepth()) / 2.0, w, h, d);
    }

    /** BoundingBox aureo: profondità = larghezza × IPHI, origine (0,0,0). */
    public static BoundingBox boundingBox(double larghezza, double altezza) {
        return new BoundingBox(0, 0, 0, larghezza, altezza, ScalareAureo.phiMinore(larghezza));
    }

    // --- CornerRadii ---

    /** Raggio angolare armonico = {@code scalaAurea(dimensione, -2)}. */
    public static double raggioAngolare(double dimensione) {
        return ScalareAureo.scalaAurea(dimensione, -2);
    }

    /** {@link CornerRadii} uniforme con raggio proporzionato alla dimensione del componente. */
    public static CornerRadii raggioAureo(double dimensione) {
        return new CornerRadii(raggioAngolare(dimensione));
    }

    /** {@link CornerRadii} a pillola (50%). */
    public static CornerRadii raggioCircolare() {
        return new CornerRadii(50.0, true);
    }

    /** Progressione di 4 raggi: sm, md, lg, xl. */
    public static double[] scalaRaggi(double base) {
        return new double[]{ base, ScalareAureo.phiMaggiore(base), ScalareAureo.scalaAurea(base, 2), ScalareAureo.scalaAurea(base, 3) };
    }
}
