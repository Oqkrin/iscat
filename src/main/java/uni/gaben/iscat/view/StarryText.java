package uni.gaben.iscat.view;

import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarryText {

    private final List<StarParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private double canvasWidth;
    private double canvasHeight;

    public StarryText(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    public void updateDimensions(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    /**
     * Clear particles.
     */
    public void scatter() {
        particles.clear();
    }

    /**
     * Reads the text snapshot pixels and instantly generates particles inside the shape.
     */
    public void formText(String message, Font font) {
        // 1. Render text off-screen
        Text textNode = new Text(message);
        textNode.setFont(font);
        textNode.setFill(Color.WHITE);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = textNode.snapshot(params, null);

        // 2. Extract non-transparent pixel coordinates
        PixelReader reader = image.getPixelReader();
        List<Point2D> targetPoints = new ArrayList<>();

        // Reading every 2 pixels for a denser, higher-fidelity shape representation
        int step = 2; 
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                if (reader.getColor(x, y).getOpacity() > 0.3) {
                    targetPoints.add(new Point2D(x, y));
                }
            }
        }

        if (targetPoints.isEmpty()) {
            particles.clear();
            return;
        }

        double offsetX = (canvasWidth - image.getWidth()) / 2;
        double offsetY = (canvasHeight - image.getHeight()) / 2;

        // 3. Clear current particles and place new ones instantly
        particles.clear();
        for (Point2D target : targetPoints) {
            // Apply a minor jitter offset so it forms a natural, starry organic cluster
            double px = target.getX() + offsetX + (random.nextDouble() - 0.5) * 1.5;
            double py = target.getY() + offsetY + (random.nextDouble() - 0.5) * 1.5;
            particles.add(new StarParticle(px, py));
        }
    }

    public void updateAndDraw(GraphicsContext gc) {
        for (StarParticle p : new ArrayList<>(particles)) {
            p.update();
            gc.setGlobalAlpha(p.getAlpha());
            gc.setFill(p.color);
            gc.fillRect(p.x, p.y, p.size, p.size);
        }
        gc.setGlobalAlpha(1.0);
    }

    // --- Inner Class for single Star Particle ---
    private class StarParticle {
        double x, y;
        double size;
        double baseAlpha;
        double phase;
        double phaseSpeed;
        Color color;

        StarParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.size = 1.0 + random.nextDouble() * 1.5; // size between 1.0 and 2.5
            this.baseAlpha = 0.5 + random.nextDouble() * 0.4; // base alpha between 0.5 and 0.9
            this.phase = random.nextDouble() * Math.PI * 2;
            this.phaseSpeed = 0.05 + random.nextDouble() * 0.05; // speed of twinkling pulse

            Color[] colors = {
                    ThemeManager.getInstance().getAccentPrimary(),
                    ThemeManager.getInstance().getAccentSecondary(),
                    ThemeManager.getInstance().getAccentTernary()
            };
            this.color = colors[random.nextInt(colors.length)];
        }

        void update() {
            phase += phaseSpeed;
        }

        double getAlpha() {
            return Math.clamp(baseAlpha + Math.sin(phase) * 0.25, 0.2, 1.0);
        }
    }
}