// HitSpark.java
package uni.gaben.iscat.universe.effects;

import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.interfaces.Removable;
import uni.gaben.iscat.universe.entities.interfaces.Stateful;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HitSpark implements Stateful, Updatable, Removable {

    private static final Random RANDOM = new Random();

    // ===== Particle burst parameters =====
    private static final int PARTICLE_COUNT = 15;
    private static final double PARTICLE_RADIUS_MIN = 8;   // world pixels
    private static final double PARTICLE_RADIUS_MAX = 16;
    private static final double EXPLOSION_RADIUS_MIN = 25;  // world pixels
    private static final double EXPLOSION_RADIUS_MAX = 90;
    private static final double SHOCKWAVE_RADIUS_MIN = 40;
    private static final double SHOCKWAVE_RADIUS_MAX = 80;
    private static final double SHOCKWAVE_LINE_WIDTH = 3;

    // ===== Color palette =====
    private static final Color[] PALETTE = {
            ThemeManager.getInstance().getAccentPrimary(),
            ThemeManager.getInstance().getAccentSecondary(),
            ThemeManager.getInstance().getAccentTernary(),
    };

    // ===== Internal state =====
    private final List<SparkParticle> particles = new ArrayList<>();
    private final ShockwaveCircle shockwave;
    private boolean expired = false;
    private double elapsed = 0.0;
    private double duration; // max duration among all particles & shockwave

    // ------------------------------------------------------------------------
    // Factory: create a hit spark at a world position (metres)
    // ------------------------------------------------------------------------
    public static HitSpark create(Vector2 worldPos, CameraModel camera,
                                  Vector2 velocity, int particleCount) {
        // Convert world position to world pixels (for camera‑space rendering)
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);

        // Optional velocity bias (ignored in this fireworks effect)
        return new HitSpark(px, py, particleCount);
    }

    // Overloaded create with default particle count
    public static HitSpark create(Vector2 worldPos, CameraModel camera, Vector2 velocity) {
        return create(worldPos, camera, velocity, PARTICLE_COUNT);
    }

    private HitSpark(double worldX, double worldY, int particleCount) {
        // ---- Generate particles ----
        for (int i = 0; i < particleCount; i++) {
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            double distance = RANDOM.nextDouble() * (EXPLOSION_RADIUS_MAX - EXPLOSION_RADIUS_MIN) + EXPLOSION_RADIUS_MIN;
            double endX = worldX + Math.cos(angle) * distance;
            double endY = worldY + Math.sin(angle) * distance;
            double radius = RANDOM.nextDouble() * (PARTICLE_RADIUS_MAX - PARTICLE_RADIUS_MIN) + PARTICLE_RADIUS_MIN;
            Color color = PALETTE[RANDOM.nextInt(PALETTE.length)];
            double particleDuration = RANDOM.nextDouble() * 0.6 + 1.2; // 1.2–1.8 sec
            particles.add(new SparkParticle(worldX, worldY, endX, endY, radius, color, particleDuration));
        }

        // ---- Shockwave circle ----
        double shockwaveDuration = RANDOM.nextDouble() * 0.6 + 1.2;
        double startRadius = 0.1;
        double endRadius = RANDOM.nextDouble() * (SHOCKWAVE_RADIUS_MAX - SHOCKWAVE_RADIUS_MIN) + SHOCKWAVE_RADIUS_MIN;
        double startAlpha = 0.5;
        double endAlpha = 0.0;
        shockwave = new ShockwaveCircle(worldX, worldY, startRadius, endRadius, startAlpha, endAlpha, shockwaveDuration);

        // Determine overall duration (max of all)
        duration = particles.stream().mapToDouble(p -> p.duration).max().orElse(1.5);
        duration = Math.max(duration, shockwave.duration);
    }

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------
    @Override
    public void update(double dt) {
        elapsed += dt;
        if (elapsed >= duration) {
            expired = true;
            return;
        }
        boolean anyAlive = false;
        // Update particles
        for (SparkParticle p : particles) {
            p.update(elapsed);
            if (!p.isDead()) anyAlive = true;
        }
        // Update shockwave
        shockwave.update(elapsed);
        if (!shockwave.isDead()) anyAlive = true;

        if (!anyAlive) expired = true;
    }

    @Override
    public boolean shouldRemove() { return expired; }

    @Override
    public boolean setShouldRemove(boolean remove) { this.expired = remove; return true; }

    public List<SparkParticle> getParticles() { return particles; }
    public ShockwaveCircle getShockwave() { return shockwave; }
    public boolean isExpired() { return expired; }

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    /**
     * A single coloured particle that moves from start to end with easing,
     * shrinking its radius over time.
     */
    public static class SparkParticle {
        private final double startX, startY;
        private final double endX, endY;
        private final double startRadius;
        private final double endRadius; // typically ~0.1
        private final Color color;
        private final double duration;
        private boolean dead = false;

        // Current interpolated values (computed each frame)
        private double x, y, radius, alpha;

        SparkParticle(double startX, double startY, double endX, double endY,
                      double startRadius, Color color, double duration) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startRadius = startRadius;
            this.endRadius = 0.1; // shrink to near zero
            this.color = color;
            this.duration = duration;
            this.alpha = 1.0;
        }

        void update(double elapsed) {
            double progress = Math.min(elapsed / duration, 1.0);
            // Ease out expo
            double t = (progress == 1.0) ? 1.0 : 1.0 - Math.pow(2, -10 * progress);
            x = startX + (endX - startX) * t;
            y = startY + (endY - startY) * t;
            radius = startRadius + (endRadius - startRadius) * t;
            alpha = 1.0 - t; // fade out linearly
            if (progress >= 1.0) dead = true;
        }

        public boolean isDead() { return dead; }
        public double getX() { return x; }
        public double getY() { return y; }
        public double getRadius() { return radius; }
        public double getAlpha() { return alpha; }
        public Color getColor() { return color; }
    }

    /**
     * A ring that expands and fades away.
     */
    public static class ShockwaveCircle {
        private final double centerX, centerY;
        private final double startRadius, endRadius;
        private final double startAlpha, endAlpha;
        private final double duration;
        private double currentRadius, currentAlpha;
        private boolean dead = false;

        ShockwaveCircle(double cx, double cy,
                        double startRadius, double endRadius,
                        double startAlpha, double endAlpha,
                        double duration) {
            this.centerX = cx;
            this.centerY = cy;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.startAlpha = startAlpha;
            this.endAlpha = endAlpha;
            this.duration = duration;
        }

        void update(double elapsed) {
            double progress = Math.min(elapsed / duration, 1.0);
            // Ease out expo
            double t = (progress == 1.0) ? 1.0 : 1.0 - Math.pow(2, -10 * progress);
            currentRadius = startRadius + (endRadius - startRadius) * t;
            currentAlpha = startAlpha + (endAlpha - startAlpha) * t;
            if (progress >= 1.0) dead = true;
        }

        public boolean isDead() { return dead; }
        public double getCenterX() { return centerX; }
        public double getCenterY() { return centerY; }
        public double getRadius() { return currentRadius; }
        public double getAlpha() { return currentAlpha; }
        public double getLineWidth() { return SHOCKWAVE_LINE_WIDTH; }
    }

    // ------------------------------------------------------------------------
    // Stateful stub
    // ------------------------------------------------------------------------
    @Override public int getState() { return 0; }
    @Override public void setState(int state) {}
    @Override public double getStateTime() { return elapsed; }
    @Override public void setStateTime(double stateTime) { this.elapsed = stateTime; }
    @Override public void updateStateTime(double dt) { elapsed += dt; }
}