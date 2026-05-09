package uni.gaben.iscat.game.components.entities.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.settings.VisualSettings;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.Objects;
import java.util.Random;

/**
 * Disegna il giocatore: sprite ruotato verso la direzione corrente.
 * Include effetto visivo di propulsione con particelle.
 */
public class PlayerView implements EntityRenderer<PlayerModel> {

    private static final double TILE_SIZE    = VisualSettings.DIMENSIONE_TILE;
    private static final double NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;
    private static final Random RANDOM = new Random();

    private final Image sprite = new Image(
            Objects.requireNonNull(
                    PlayerView.class.getResourceAsStream(
                            "/uni/gaben/iscat/sprites/battle_ship_1.png")));

    @Override
    public void draw(GraphicsContext gc, PlayerModel p) {
        double cx = p.getX() + TILE_SIZE / 2.0;
        double cy = p.getY() + TILE_SIZE / 2.0;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(p.getDirectionAngle() + NORTH_OFFSET);
        
        // Disegna effetto propulsione dietro la nave
        drawThrustEffect(gc, p);
        
        // Disegna la nave
        gc.drawImage(sprite, -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);
        
        gc.restore();
    }
    
    /**
     * Disegna l'effetto di propulsione con particelle dietro la nave.
     * L'intensità dipende dalla velocità del giocatore.
     * Le particelle formano un cono: più lontane = più disperse lateralmente.
     */
    private void drawThrustEffect(GraphicsContext gc, PlayerModel p) {
        // Calcola intensità basata sulla velocità
        double speed = Math.sqrt(p.getVelocity().x * p.getVelocity().x + 
                                 p.getVelocity().y * p.getVelocity().y);
        double intensity = Math.min(speed / PlayerSettings.VELOCITA_MAX, 3.0);
        
        if (intensity < 0.01) return; // Non disegnare se quasi fermo
        
        // Numero di particelle basato sull'intensità
        int particleCount = (int) (3 + intensity * 7);
        
        // Posizione di partenza: dietro la nave
        double baseY = TILE_SIZE /2;
        double maxThrustHeight = TILE_SIZE/2;
        
        for (int i = 0; i < particleCount; i++) {
            // Prima genera Y (distanza dalla nave)
            double offsetY = baseY + RANDOM.nextDouble() * maxThrustHeight;
            
            // Calcola quanto è lontana la particella (0 = vicino, 1 = lontano)
            double distanceRatio = (offsetY - baseY) / maxThrustHeight;
            
            // Range X si espande con la distanza (effetto cono)
            double maxSpreadX = TILE_SIZE * 0.15 * (1 + distanceRatio * 2);
            double offsetX = (RANDOM.nextDouble() - 0.5) * maxSpreadX * 2;
            
            // Dimensione particella: più piccola quando più lontana
            double size = (2 + RANDOM.nextDouble() * 3) * (1.2 - distanceRatio * 0.5);
            
            // Colore basato su distanza e casualità
            double colorMix = RANDOM.nextDouble();
            Color particleColor = getParticleColor(distanceRatio, intensity, colorMix);

            gc.setFill(particleColor);
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }
    }

    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix) {
        // Clamp intensity to valid range
        intensity = Math.min(intensity, 1.0);
        
        // Calculate base alpha and clamp to [0, 1]
        double alphaMix = Math.min(1.0, (1.0 - distanceRatio * 0.4) * intensity);

        // Usa i colori accent dal tema CSS
        Color particleColor;
        if (colorMix < 0.3 && distanceRatio < 0.5) {
            // Accent primary (grigio chiaro brillante, solo vicino)
            Color base = ThemeColors.getAccentPrimary();
            particleColor = Color.rgb(
                (int) (base.getRed() * 255),
                (int) (base.getGreen() * 255),
                (int) (base.getBlue() * 255),
                Math.min(1.0, 0.9 * alphaMix)
            );
        } else if (colorMix < 0.7) {
            // Accent secondary (grigio medio)
            Color base = ThemeColors.getAccentSecondary();
            particleColor = Color.rgb(
                (int) (base.getRed() * 255),
                (int) (base.getGreen() * 255),
                (int) (base.getBlue() * 255),
                Math.min(1.0, 0.8 * alphaMix)
            );
        } else {
            // Accent tertiary (grigio scuro, più comune quando lontano)
            Color base = ThemeColors.getAccentTertiary();
            particleColor = Color.rgb(
                (int) (base.getRed() * 255),
                (int) (base.getGreen() * 255),
                (int) (base.getBlue() * 255),
                Math.min(1.0, 0.7 * alphaMix)
            );
        }
        return particleColor;
    }
}
