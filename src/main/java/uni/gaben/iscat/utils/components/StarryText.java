package uni.gaben.iscat.utils.components;

import javafx.geometry.Point2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarryText {

    private final List<StarParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private double canvasWidth;
    private double canvasHeight;

    private boolean isForming = false;
    private final int PARTICLE_COUNT = 2000;

    public StarryText(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;

        // Inizializza le particelle sparse per lo schermo
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new StarParticle(
                    random.nextDouble() * width,
                    random.nextDouble() * height
            ));
        }
    }

    public void updateDimensions(double width, double height) {
        this.canvasWidth = width;
        this.canvasHeight = height;
    }

    /**
     * Rompe il testo attuale e sparge le stelle a caso.
     */
    public void scatter() {
        isForming = false;
        for (StarParticle p : particles) {
            p.targetX = random.nextDouble() * canvasWidth;
            p.targetY = random.nextDouble() * canvasHeight;
            p.speed = 0.02 + (random.nextDouble() * 0.03); // Velocità casuale
        }
    }

    /**
     * Legge i pixel del testo e comanda alle stelle di formarlo.
     */
    public void formText(String message, Font font) {
        // 1. Renderizza il testo in memoria
        Text textNode = new Text(message);
        textNode.setFont(font);
        textNode.setFill(Color.WHITE);

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage image = textNode.snapshot(params, null);

        // 2. Estrai le coordinate dei pixel non trasparenti
        PixelReader reader = image.getPixelReader();
        List<Point2D> targetPoints = new ArrayList<>();

        int step = 4; // Leggi 1 pixel ogni 4 (per non avere troppe collisioni)
        for (int y = 0; y < image.getHeight(); y += step) {
            for (int x = 0; x < image.getWidth(); x += step) {
                if (reader.getColor(x, y).getOpacity() > 0.1) {
                    targetPoints.add(new Point2D(x, y));
                }
            }
        }

        // 3. Assegna i target alle particelle
        if (targetPoints.isEmpty()) return;

        double offsetX = (canvasWidth - image.getWidth()) / 2;
        double offsetY = (canvasHeight - image.getHeight()) / 2;

        isForming = true;
        for (int i = 0; i < particles.size(); i++) {
            StarParticle p = particles.get(i);

            // Se abbiamo più particelle che pixel, le extra si nascondono nel testo
            Point2D target = targetPoints.get(i % targetPoints.size());

            p.targetX = target.getX() + offsetX;
            p.targetY = target.getY() + offsetY;
            p.speed = 0.05 + (random.nextDouble() * 0.1); // Ease-out variation
        }
    }

    public void updateAndDraw(GraphicsContext gc) {
        for (StarParticle p : particles) {
            // Lerp (Linear Interpolation) per un effetto Ease-Out fluido
            p.x += (p.targetX - p.x) * p.speed;
            p.y += (p.targetY - p.y) * p.speed;

            // Tremolio (jitter) leggero per farle sembrare vive
            double jitterX = isForming ? (random.nextDouble() - 0.5) : 0;
            double jitterY = isForming ? (random.nextDouble() - 0.5) : 0;

            gc.setFill(p.color);
            gc.fillRect(p.x + jitterX, p.y + jitterY, p.size, p.size);
        }
    }

    // --- Classe Interna per la singola Particella ---
    private class StarParticle {
        double x, y;
        double targetX, targetY;
        double speed;
        double size;
        Color color;

        StarParticle(double x, double y) {
            this.x = x;
            this.y = y;
            this.targetX = x;
            this.targetY = y;
            this.size = 1.5 + random.nextDouble() * 2;

            // Palette di colori spaziali (Azzurro, Bianco, Giallo tenue)
            Color[] colors = {
                    ThemeColors.getAccentPrimary(),
                    ThemeColors.getAccentSecondary(),
                    ThemeColors.getAccentTertiary()
            };
            this.color = colors[random.nextInt(colors.length)];
        }
    }
}