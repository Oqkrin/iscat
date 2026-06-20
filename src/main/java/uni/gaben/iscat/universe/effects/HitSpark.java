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

    // Physics constants (adjusted for time‑based movement)
    private static final double GRAVITY = 0.15;          // pixels/sec²
    private static final double DRAG_CONFETTI = 0.5;     // per second
    private static final double DRAG_SEQUINS = 0.3;      // per second
    private static final double TERMINAL_VELOCITY = 2.0; // pixels/sec

    private final List<Particle> particles = new ArrayList<>();
    private boolean expired = false;
    private double duration = 0.0;
    private static final double MAX_LIFETIME = 1.5; // seconds

    /**
     * Factory method: creates a hit spark at a world position, with an optional velocity bias.
     * @param worldPos  impact point in world metres
     * @param camera    current camera (to convert to screen pixels)
     * @param velocity  projectile's linear velocity (world metres/sec) – used to bias particles
     * @param canvasWidth, canvasHeight  viewport size in pixels
     */
    public static HitSpark create(Vector2 worldPos, CameraModel camera,
                                  Vector2 velocity, double canvasWidth, double canvasHeight,
                                  int countConfetti, int countSequins) {
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);
        double zoom = camera.getZoom();
        double screenX = (px - camera.getX()) * zoom + canvasWidth / 2.0;
        double screenY = (py - camera.getY()) * zoom + canvasHeight / 2.0;

        // Convert velocity to pixels/sec and scale for particle burst
        double vxPx = UU.mToPx(velocity.x) * zoom;
        double vyPx = UU.mToPx(velocity.y) * zoom;

        return new HitSpark(screenX, screenY, vxPx, vyPx, countConfetti, countSequins);
    }

    private HitSpark(double x, double y, double vx, double vy, int confettiCount, int sequinCount) {
        // Spawn confetti
        for (int i = 0; i < confettiCount; i++) {
            particles.add(new ConfettiParticle(x, y, vx, vy));
        }
        // Spawn sequins
        for (int i = 0; i < sequinCount; i++) {
            particles.add(new SequinParticle(x, y, vx, vy));
        }
    }

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
    // Particle hierarchy
    // ------------------------------------------------------------------------

    public abstract static class Particle {
        double x, y;
        double vx, vy;
        boolean dead = false;

        Particle(double x, double y) {
            this.x = x;
            this.y = y;
        }

        abstract void update(double dt);
        boolean isDead() { return dead; }

        // Time‑based physics
        protected void applyPhysics(double dt, double drag) {
            vx -= vx * drag * dt;                 // drag
            vy += GRAVITY * dt;                   // gravity
            if (vy > TERMINAL_VELOCITY) vy = TERMINAL_VELOCITY;
            x += vx * dt;
            y += vy * dt;
            // Kill if off‑screen (well beyond viewport)
            if (y > 10000) dead = true;
        }

        protected static double randomRange(double min, double max) {
            return RANDOM.nextDouble() * (max - min) + min;
        }
    }

    public static class ConfettiParticle extends Particle {
        private final Color frontColor, backColor;
        private final double width, height;
        private double rotation;
        private double scaleY = 1.0;
        private final double randomModifier;

        ConfettiParticle(double x, double y, double biasVx, double biasVy) {
            super(x, y);
            // Slight random offset to avoid exact clustering
            this.x += randomRange(-2, 2);
            this.y += randomRange(-2, 2);

            Color base = pickRandomColor();
            frontColor = base;
            backColor = base.darker();
            width = randomRange(5, 9);
            height = randomRange(8, 15);
            rotation = randomRange(0, 2 * Math.PI);
            randomModifier = randomRange(0, 99);

            // Isotropic burst with moderate speed
            double angle = randomRange(0, 2 * Math.PI);
            double speed = randomRange(30, 80); // pixels/sec
            vx = Math.cos(angle) * speed + biasVx * 0.15;
            vy = Math.sin(angle) * speed + biasVy * 0.15;
        }

        @Override
        void update(double dt) {
            applyPhysics(dt, DRAG_CONFETTI);
            scaleY = Math.cos((y + randomModifier) * 0.09);
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getWidth() { return width; }
        public double getHeight() { return height; }
        public double getRotation() { return rotation; }
        public double getScaleY() { return scaleY; }
        public Color getColor() { return scaleY > 0 ? frontColor : backColor; }
    }

    public static class SequinParticle extends Particle {
        private final Color color;
        private final double radius;

        SequinParticle(double x, double y, double biasVx, double biasVy) {
            super(x, y);
            this.x += randomRange(-1.5, 1.5);
            this.y += randomRange(-1.5, 1.5);

            color = pickRandomColor().darker();
            radius = randomRange(1, 2.5);

            double angle = randomRange(0, 2 * Math.PI);
            double speed = randomRange(20, 50); // pixels/sec
            vx = Math.cos(angle) * speed + biasVx * 0.1;
            vy = Math.sin(angle) * speed + biasVy * 0.1;
        }

        @Override
        void update(double dt) {
            applyPhysics(dt, DRAG_SEQUINS);
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getRadius() { return radius; }
        public Color getColor() { return color; }
    }

    private static Color pickRandomColor() {
        // Use your ThemeManager or a fixed palette
        Color[] palette = {
                ThemeManager.getInstance().getAccentPrimary(),
                ThemeManager.getInstance().getAccentSecondary(),
                ThemeManager.getInstance().getAccentTernary()
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