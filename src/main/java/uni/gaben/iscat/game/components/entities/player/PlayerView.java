package uni.gaben.iscat.game.components.entities.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.components.entities.npcs.SpriteUtils;
import uni.gaben.iscat.game.utils.interfaces.Drawable;
import uni.gaben.iscat.game.utils.settings.VisualSettings;
import uni.gaben.iscat.utils.ThemeManager;

import java.util.Objects;
import java.util.Random;

/**
 * Disegna il giocatore: sprite ruotato verso la direzione corrente.
 * Include effetto visivo di propulsione con particelle.
 */
public class PlayerView implements Drawable<PlayerModel> {

    private static final double TILE_SIZE    = VisualSettings.DIMENSIONE_TILE;
    private static final double NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;
    private static final Random RANDOM = new Random();

    private Image currentSprite;

    // Cache: ricalcola lo sprite tintato solo quando il tint cambia
    private Color lastTint     = Color.WHITE;
    private Image cachedTinted;

    public PlayerView() {
        reloadSprite();
    }

    // Ricarica lo sprite quando viene cambiata la skin
    public void reloadSprite() {
        try {
            String path = uni.gaben.iscat.gamenex.universe.player.PlayerSettings.getPlayerSkin();

            if (path == null || path.isBlank()) {
                path = "/uni/gaben/iscat/sprites/player1.png";
            }

            currentSprite = new Image(
                    Objects.requireNonNull(
                            PlayerView.class.getResourceAsStream(path),
                            "[PlayerView di Game] Sprite non trovato: " + path
                    )
            );

            cachedTinted = currentSprite;
            //System.out.println("[PlayerView di Game] Player sprite caricato correttamente: " + path);

        } catch (Exception e) {
            //System.err.println("[PlayerView di Game] Errore caricamento sprite: " + uni.gaben.iscat.gamenex.universe.player.PlayerSettings.getPlayerSkin());
            e.printStackTrace();
        }
    }

    private Image getTintedSprite(Color tint) {
        if (currentSprite == null) reloadSprite();

        if (!tint.equals(lastTint) || cachedTinted == null) {
            lastTint = tint;
            cachedTinted = SpriteUtils.tinted(currentSprite, tint);
        }
        return cachedTinted;
    }

    @Override
    public void draw(GraphicsContext gc, PlayerModel p) {
        if (currentSprite == null) {
            reloadSprite();
        }

        Color tint = ThemeManager.getInstance().globalTintProperty().get();

        double cx = p.getX() + TILE_SIZE / 2.0;
        double cy = p.getY() + TILE_SIZE / 2.0;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(p.getDirectionAngle() + NORTH_OFFSET);

        drawThrustEffect(gc, p, tint);
        gc.drawImage(getTintedSprite(tint), -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);

        gc.restore();
    }

    private void drawThrustEffect(GraphicsContext gc, PlayerModel p, Color tint) {
        double speed = Math.sqrt(p.getVelocity().x * p.getVelocity().x +
                p.getVelocity().y * p.getVelocity().y);
        double intensity = Math.min(speed / PlayerSettings.VELOCITA_MAX, 3.0);
        if (intensity < 0.01) return;

        int    particleCount   = (int) (3 + intensity * 7);
        double baseY           = TILE_SIZE / 2;
        double maxThrustHeight = TILE_SIZE / 2;

        for (int i = 0; i < particleCount; i++) {
            double offsetY       = baseY + RANDOM.nextDouble() * maxThrustHeight;
            double distanceRatio = (offsetY - baseY) / maxThrustHeight;
            double maxSpreadX    = TILE_SIZE * 0.15 * (1 + distanceRatio * 2);
            double offsetX       = (RANDOM.nextDouble() - 0.5) * maxSpreadX * 2;
            double size          = (2 + RANDOM.nextDouble() * 3) * (1.2 - distanceRatio * 0.5);
            double colorMix      = RANDOM.nextDouble();

            gc.setFill(getParticleColor(distanceRatio, intensity, colorMix, tint));
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }
    }

    private static Color getParticleColor(double distanceRatio, double intensity,
                                          double colorMix, Color tint) {
        intensity = Math.min(intensity, 1.0);
        double alphaMix = Math.min(1.0, (1.0 - distanceRatio * 0.4) * intensity);

        double brightness;
        double alpha;
        if (colorMix < 0.3 && distanceRatio < 0.5) {
            brightness = 1.0;   alpha = 0.9 * alphaMix;
        } else if (colorMix < 0.7) {
            brightness = 0.66;  alpha = 0.8 * alphaMix;
        } else {
            brightness = 0.2;   alpha = 0.7 * alphaMix;
        }

        return Color.color(
                Math.min(1.0, brightness * tint.getRed()),
                Math.min(1.0, brightness * tint.getGreen()),
                Math.min(1.0, brightness * tint.getBlue()),
                Math.min(1.0, alpha));
    }
}
