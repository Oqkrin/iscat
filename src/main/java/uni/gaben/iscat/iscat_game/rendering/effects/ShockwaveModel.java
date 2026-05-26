package uni.gaben.iscat.iscat_game.rendering.effects;

public class ShockwaveModel {

    private boolean active = false;

    private double radius = 0.0;
    private double alpha = 1.0;
    private double timer = 0.0;

    private double duration;
    private double maxRadius;
    private double lineWidth;

    public void trigger(double duration,
                        double maxRadius,
                        double lineWidth) {

        this.active = true;

        this.timer = 0.0;
        this.radius = 0.0;
        this.alpha = 1.0;

        this.duration = duration;
        this.maxRadius = maxRadius;
        this.lineWidth = lineWidth;
    }

    public void update(double dt) {
        if (!active) return;

        timer += dt;

        double progress = timer / duration;

        if (progress >= 1.0) {
            active = false;
            return;
        }

        radius = progress * maxRadius;
        alpha = 1.0 - progress;
    }

    public boolean isActive() {
        return active;
    }

    public double getRadius() {
        return radius;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getLineWidth() {
        return lineWidth;
    }
}