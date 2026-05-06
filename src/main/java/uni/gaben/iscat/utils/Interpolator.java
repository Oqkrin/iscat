package uni.gaben.iscat.utils;

import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Funzioni di interpolazione statiche + preset selezionabili via {@link Preset}.
 * Per animazioni fisiche continue usare {@link Spring}.
 * Tutti i parametri {@code t} assumono t ∈ [0,1].
 */
public final class Interpolator {

    private Interpolator() {}

    // ------------------------------------------------------------------ preset

    /**
     * Preset di easing selezionabili — utili per passare la curva come parametro
     * (es. a un sistema di animazione, a GameSettings, a una UI di configurazione).
     *
     * Uso: {@code Interpolator.Preset.SMOOTH_STEP.apply(0, 100, t)}
     */
    public enum Preset {
        LINEAR      { @Override public double apply(double a, double b, double t) { return lerp(a, b, t); } },
        EASE_IN     { @Override public double apply(double a, double b, double t) { return easeIn(a, b, t); } },
        EASE_OUT    { @Override public double apply(double a, double b, double t) { return easeOut(a, b, t); } },
        EASE_IN_OUT { @Override public double apply(double a, double b, double t) { return smoothStep(a, b, t); } },
        SMOOTH_STEP { @Override public double apply(double a, double b, double t) { return smoothStep(a, b, t); } },
        SMOOTHER    { @Override public double apply(double a, double b, double t) { return smootherStep(a, b, t); } },
        BOUNCE_OUT  { @Override public double apply(double a, double b, double t) { return bounceOut(a, b, t); } },
        ELASTIC_OUT { @Override public double apply(double a, double b, double t) { return elasticOut(a, b, t); } },
        BACK_OUT    { @Override public double apply(double a, double b, double t) { return backOut(a, b, t); } };

        /** Interpola da {@code a} a {@code b} con questo preset. */
        public abstract double apply(double a, double b, double t);

        /** Versione Vec2. */
        public Vec2 apply(Vec2 a, Vec2 b, double t) {
            return new Vec2(apply(a.x, b.x, t), apply(a.y, b.y, t));
        }
    }

    // ------------------------------------------------------------------ base

    /** Lerp lineare: t=0→a, t=1→b. */
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Lerp su {@link Vec2} componente per componente. */
    public static Vec2 lerp(Vec2 a, Vec2 b, double t) {
        return new Vec2(lerp(a.x, b.x, t), lerp(a.y, b.y, t));
    }

    /** Rimappa {@code value} da [inMin,inMax] a [outMin,outMax]. */
    public static double remap(double value, double inMin, double inMax, double outMin, double outMax) {
        return lerp(outMin, outMax, (value - inMin) / (inMax - inMin));
    }

    // ------------------------------------------------------------------ ease in

    /** Accelera lentamente all'inizio. */
    public static double easeIn(double a, double b, double t) {
        t = clamp01(t);
        return lerp(a, b, t * t);
    }

    /** Accelera molto lentamente (cubica). */
    public static double easeInCubic(double a, double b, double t) {
        t = clamp01(t);
        return lerp(a, b, t * t * t);
    }

    // ------------------------------------------------------------------ ease out

    /** Decelera verso la fine. */
    public static double easeOut(double a, double b, double t) {
        t = clamp01(t);
        t = 1 - t;
        return lerp(a, b, 1 - t * t);
    }

    /** Decelera più marcatamente (cubica). */
    public static double easeOutCubic(double a, double b, double t) {
        t = clamp01(t);
        t = 1 - t;
        return lerp(a, b, 1 - t * t * t);
    }

    // ------------------------------------------------------------------ ease in-out

    /** Smooth-step: accelera all'inizio, decelera alla fine. */
    public static double smoothStep(double a, double b, double t) {
        t = clamp01(t);
        t = t * t * (3 - 2 * t);
        return lerp(a, b, t);
    }

    /** Smoother-step (Perlin): derivate prime e seconde nulle agli estremi. */
    public static double smootherStep(double a, double b, double t) {
        t = clamp01(t);
        t = t * t * t * (t * (t * 6 - 15) + 10);
        return lerp(a, b, t);
    }

    // ------------------------------------------------------------------ bounce / elastic

    /**
     * Bounce ease-out: rimbalza verso il target come una pallina.
     * Non richiede {@link Spring} — puramente matematico.
     */
    public static double bounceOut(double a, double b, double t) {
        t = clamp01(t);
        double n1 = 7.5625, d1 = 2.75, f;
        if      (t < 1/d1)       f = n1 * t * t;
        else if (t < 2/d1)       f = n1 * (t -= 1.5/d1)   * t + 0.75;
        else if (t < 2.5/d1)     f = n1 * (t -= 2.25/d1)  * t + 0.9375;
        else                     f = n1 * (t -= 2.625/d1)  * t + 0.984375;
        return lerp(a, b, f);
    }

    /**
     * Elastic ease-out: supera il target e torna oscillando.
     * Per animazioni UI vivaci; per fisica continua preferire {@link Spring}.
     *
     * @param amplitude ampiezza dell'overshoot (1.0 = standard)
     * @param period    periodo dell'oscillazione (0.3 = standard)
     */
    public static double elasticOut(double a, double b, double t, double amplitude, double period) {
        t = clamp01(t);
        if (t == 0 || t == 1) return lerp(a, b, t);
        double s = period / (2 * Math.PI) * Math.asin(1.0 / amplitude);
        double f = amplitude * Math.pow(2, -10 * t)
                * Math.sin((t - s) * (2 * Math.PI) / period) + 1;
        return lerp(a, b, f);
    }

    /** {@link #elasticOut} con parametri standard. */
    public static double elasticOut(double a, double b, double t) {
        return elasticOut(a, b, t, 1.0, 0.3);
    }

    // ------------------------------------------------------------------ back (overshoot)

    /**
     * Ease-out con overshoot: supera leggermente il target prima di fermarsi.
     * @param overshoot intensità (1.70158 = standard ~10% overshoot)
     */
    public static double backOut(double a, double b, double t, double overshoot) {
        t = clamp01(t) - 1;
        return lerp(a, b, t * t * ((overshoot + 1) * t + overshoot) + 1);
    }

    /** {@link #backOut} con overshoot standard. */
    public static double backOut(double a, double b, double t) {
        return backOut(a, b, t, 1.70158);
    }

    // ------------------------------------------------------------------ utility

    private static double clamp01(double t) { return Math.max(0, Math.min(1, t)); }
}
