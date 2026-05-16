package uni.gaben.iscat.gamenex.lib.utils;

/*Universe Units classe utility per passare da unita dyn4j ovvero k m s^2 a ? px tick*/
public class UU {
    public static final double UNIVERSE_SCALE = 64.0;
    public static final double UNIVERSE_TICK = 1 / 60.0;

    private double px;
    private double m;

    private UU() {

    }
    public static double pxToM(double value) {
        return value * UNIVERSE_SCALE;
    }

    public static double mToPx(double value) {
        return value / UNIVERSE_SCALE;
    }

}
