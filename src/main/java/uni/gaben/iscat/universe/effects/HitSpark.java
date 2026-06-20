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

    // ===== Physics constants (tweak for desired feel) =====
    private static final double GRAVITY = 0.15;          // pixels/sec²
    private static final double DRAG = 0.5;              // per second
    private static final double TERMINAL_VELOCITY = 3.0; // pixels/sec
    private static final double MAX_LIFETIME = 1.5;      // seconds

    // ===== Particle burst parameters =====
    private static final int CONF_COUNT = 30;
    private static final int SEQUIN_COUNT = 15;
    private static final double CONF_SPEED_MIN = 40;   // pixels/sec
    private static final double CONF_SPEED_MAX = 90;
    private static final double SEQUIN_SPEED_MIN = 30;
    private static final double SEQUIN_SPEED_MAX = 60;

    private final List<Particle> particles = new ArrayList<>();
    private boolean expired = false;
    private double duration = 0.0;

    // ------------------------------------------------------------------------
    // Factory: create a hit spark at a world position (metres)
    // ------------------------------------------------------------------------
    public static HitSpark create(Vector2 worldPos, CameraModel camera,
                                  Vector2 velocity, int countConfetti, int countSequins) {
        // Convert world position to world pixels (for camera‑space rendering)
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);
        // Convert projectile velocity to pixels/sec (for bias)
        double vxPx = UU.mToPx(velocity.x);
        double vyPx = UU.mToPx(velocity.y);

        return new HitSpark(px, py, vxPx, vyPx, countConfetti, countSequins);
    }

    private HitSpark(double worldX, double worldY, double biasVx, double biasVy,
                     int confettiCount, int sequinCount) {
        // Confetti
        for (int i = 0; i < confettiCount; i++) {
            particles.add(new ConfettiParticle(worldX, worldY, biasVx, biasVy));
        }
        // Sequins
        for (int i = 0; i < sequinCount; i++) {
            particles.add(new SequinParticle(worldX, worldY, biasVx, biasVy));
        }
    }

    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------
    @Override
    public void update(double dt) {
        duration += dt;
        if (duration >= MAX_LIFETIME) {
            expired = true;
            return;
        }
        boolean anyAlive = false;
        for (Particle p : particles) {
            p.update(dt);
            if (!p.isDead()) anyAlive = true;
        }
        if (!anyAlive) expired = true;
    }

    @Override
    public boolean shouldRemove() { return expired; }
    @Override
    public boolean setShouldRemove(boolean remove) { this.expired = remove; return true; }

    public List<Particle> getParticles() { return particles; }
    public boolean isExpired() { return expired; }

    // ------------------------------------------------------------------------
    // Particle hierarchy (world pixel coordinates)
    // ------------------------------------------------------------------------
    public abstract static class Particle {
        double x, y;          // world pixel coordinates
        double vx, vy;        // pixels/sec
        double alpha = 1.0;
        double decay;         // alpha decay per second
        boolean dead = false;

        // Trail positions (for drawing a faint line)
        double[] trailX = new double[3];
        double[] trailY = new double[3];
        public int trailIdx = 0;

        Particle(double x, double y) {
            this.x = x;
            this.y = y;
            for (int i = 0; i < 3; i++) {
                trailX[i] = x;
                trailY[i] = y;
            }
            decay = randomRange(0.4, 1.2); // alpha loss per second
        }

        abstract void update(double dt);
        boolean isDead() { return dead; }

        protected void applyPhysics(double dt, double drag) {
            // Drag
            vx -= vx * drag * dt;
            // Gravity (positive = down)
            vy += GRAVITY * dt;
            if (vy > TERMINAL_VELOCITY) vy = TERMINAL_VELOCITY;
            // Update position
            x += vx * dt;
            y += vy * dt;
            // Fade
            alpha -= decay * dt;
            if (alpha <= 0.01) dead = true;
            // Update trail
            trailX[trailIdx] = x;
            trailY[trailIdx] = y;
            trailIdx = (trailIdx + 1) % 3;
        }

        protected static double randomRange(double min, double max) {
            return RANDOM.nextDouble() * (max - min) + min;
        }

        // Getters for renderer
        public double getX() { return x; }
        public double getY() { return y; }
        public double getAlpha() { return alpha; }
        public double[] getTrailX() { return trailX; }
        public double[] getTrailY() { return trailY; }
        public abstract Color getColor();
        public abstract double getSize();
        public abstract boolean hasTrail();
    }

    // ===== Confetti =====
    public static class ConfettiParticle extends Particle {
        private final Color frontColor, backColor;
        private final double width, height;
        private double rotation;
        private double scaleY = 1.0;
        private final double randomModifier;

        ConfettiParticle(double x, double y, double biasVx, double biasVy) {
            super(x, y);
            // Add slight random offset for organic spread
            this.x += randomRange(-2, 2);
            this.y += randomRange(-2, 2);

            Color base = pickRandomColor();
            frontColor = base;
            backColor = base.darker();
            width = randomRange(5, 9);
            height = randomRange(8, 15);
            rotation = randomRange(0, 2 * Math.PI);
            randomModifier = randomRange(0, 99);

            // Isotropic burst with bias
            double angle = randomRange(0, 2 * Math.PI);
            double speed = randomRange(CONF_SPEED_MIN, CONF_SPEED_MAX);
            vx = Math.cos(angle) * speed + biasVx * 0.15;
            vy = Math.sin(angle) * speed + biasVy * 0.15;
        }

        @Override
        void update(double dt) {
            applyPhysics(dt, DRAG);
            // Spin effect: scaleY oscillates with y
            scaleY = Math.cos((y + randomModifier) * 0.09);
        }

        @Override
        public Color getColor() { return scaleY > 0 ? frontColor : backColor; }
        @Override
        public double getSize() { return width; } // not used directly
        @Override
        public boolean hasTrail() { return false; } // confetti no trail

        public double getWidth() { return width; }
        public double getHeight() { return height * scaleY; }
        public double getRotation() { return rotation; }
    }

    // ===== Sequin (simple circle) =====
    public static class SequinParticle extends Particle {
        public final Color color;
        public final double radius;

        SequinParticle(double x, double y, double biasVx, double biasVy) {
            super(x, y);
            this.x += randomRange(-1.5, 1.5);
            this.y += randomRange(-1.5, 1.5);
            color = pickRandomColor().darker();
            radius = randomRange(1, 3);

            double angle = randomRange(0, 2 * Math.PI);
            double speed = randomRange(SEQUIN_SPEED_MIN, SEQUIN_SPEED_MAX);
            vx = Math.cos(angle) * speed + biasVx * 0.1;
            vy = Math.sin(angle) * speed + biasVy * 0.1;
        }

        @Override
        void update(double dt) {
            applyPhysics(dt, DRAG * 0.7); // sequins have slightly less drag
        }

        @Override
        public Color getColor() { return color; }
        @Override
        public double getSize() { return radius; }
        @Override
        public boolean hasTrail() { return true; } // sequins leave a faint trail
    }

    // ===== Color palette =====
    private static Color pickRandomColor() {
        Color[] palette = {
                ThemeManager.getInstance().getAccentPrimary(),
                ThemeManager.getInstance().getAccentSecondary(),
                ThemeManager.getInstance().getAccentTernary(),
        };
        return palette[RANDOM.nextInt(palette.length)];
    }

    // ------------------------------------------------------------------------
    // Stateful stub
    // ------------------------------------------------------------------------
    @Override public int getState() { return 0; }
    @Override public void setState(int state) {}
    @Override public double getStateTime() { return duration; }
    @Override public void setStateTime(double stateTime) { this.duration = stateTime; }
    @Override public void updateStateTime(double dt) { duration += dt; }
}