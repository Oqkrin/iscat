package uni.gaben.iscat.game.utils.physics;

/**
 * Vettore 2D immutabile. Coppia (x, y) grezza — NON normalizzata per design.
 * Usare {@link #add}, {@link #scale}, {@link #dot} per l'aritmetica; creare nuove istanze invece di mutare.
 */
public final class Vec2 {

    public static final Vec2 ZERO = new Vec2(0, 0);

    public final double x;
    public final double y;

    public Vec2(double x, double y) { this.x = x; this.y = y; }

    /** Somma componente per componente. */
    public Vec2 add(Vec2 other)            { return new Vec2(x + other.x, y + other.y); }

    /** Somma per scalari. */
    public Vec2 add(double dx, double dy)  { return new Vec2(x + dx, y + dy); }

    /** Moltiplicazione scalare. */
    public Vec2 scale(double factor)       { return new Vec2(x * factor, y * factor); }

    /** Prodotto scalare. */
    public double dot(Vec2 other)          { return x * other.x + y * other.y; }

    /** Modulo al quadrato (evita sqrt). */
    public double magnitudeSq()            { return x * x + y * y; }

    /** Modulo euclideo. */
    public double magnitude()              { return Math.sqrt(magnitudeSq()); }

    /** Distanza da un altro punto. */
    public double distanceTo(Vec2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /** Distanza al quadrato (economica). */
    public double distanceSqTo(Vec2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    @Override public String toString() { return "Vec2(" + x + ", " + y + ")"; }
}
