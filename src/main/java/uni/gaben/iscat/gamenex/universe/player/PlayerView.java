package uni.gaben.iscat.gamenex.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeColors;

import java.util.Objects;
import java.util.Random;

/**
 * Renders the player sprite with rotation and a thrust particle effect
 * ported from game/components/entities/player/PlayerView.
 */
public class PlayerView extends AbstractEntityView implements Drawable<PlayerModel> {

    private static final Random RANDOM = new Random();

    private Image currentSprite;

    // Caricamento iniziale dello sprite, serve per far in modo che lo sprite del player si carichi all'inizio
    public PlayerView() {
        reloadSprite();
    }

    @Override
    public void draw(PlayerModel entity, GraphicsContext gc) {

        setPos(entity);
        setAngle(entity);
        setSize(PlayerSettings.DIMENSIONE_SPRITE);

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg);

        drawThrustEffect(gc, entity);

        gc.drawImage(currentSprite, -w / 2, -h / 2, w, h);
        gc.restore();

        // HP bar (drawn in screen space after restore)
        drawHpBar(entity, gc);
    }



    /** Pixel-art thrust plume behind the ship (in local rotated space). */
    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity) {
        double vx = entity.getLinearVelocity().x;
        double vy = entity.getLinearVelocity().y;
        double speed = Math.sqrt(vx * vx + vy * vy) * UniverseSettings.SCALE;

        // Intensity 0..1 based on speed vs max speed (pixels/sec)
        double intensity = Math.min(speed / (PlayerSettings.VELOCITA_MAX * UniverseSettings.SCALE), 1.0);
        if (intensity < 0.01) return;

        int    particleCount   = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);
        double baseY           = h / 2;
        double maxThrustHeight = h * PlayerSettings.THRUST_HEIGHT_FACTOR;

        Color accentColor = ThemeColors.getAccentPrimary();

        for (int i = 0; i < particleCount; i++) {
            double offsetY       = baseY + RANDOM.nextDouble() * maxThrustHeight;
            double distanceRatio = (offsetY - baseY) / maxThrustHeight;
            double maxSpreadX    = w * PlayerSettings.THRUST_SPREAD_X_FACTOR * (1 + distanceRatio * 2);
            double offsetX       = (RANDOM.nextDouble() - 0.5) * maxSpreadX * 2;
            double size          = (PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION) * (1.2 - distanceRatio * 0.5);
            double colorMix      = RANDOM.nextDouble();

            gc.setFill(getParticleColor(distanceRatio, intensity, colorMix, accentColor));
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }
    }

    private static Color getParticleColor(double distanceRatio, double intensity,
                                          double colorMix, Color accent) {
        double alphaMix = Math.min(1.0, (1.0 - distanceRatio * 0.4) * intensity);
        double brightness;
        double alpha;

        if (colorMix < PlayerSettings.PARTICLE_CORE_THRESHOLD && distanceRatio < 0.5) {
            brightness = PlayerSettings.PARTICLE_CORE_BRIGHTNESS;  
            alpha = PlayerSettings.PARTICLE_CORE_ALPHA * alphaMix;  // white-hot core
        } else if (colorMix < PlayerSettings.PARTICLE_MID_THRESHOLD) {
            brightness = PlayerSettings.PARTICLE_MID_BRIGHTNESS; 
            alpha = PlayerSettings.PARTICLE_MID_ALPHA * alphaMix;  // accent mid-flame
        } else {
            brightness = PlayerSettings.PARTICLE_TAIL_BRIGHTNESS; 
            alpha = PlayerSettings.PARTICLE_TAIL_ALPHA * alphaMix;  // dark ember tail
        }

        return Color.color(
                Math.min(1.0, brightness * accent.getRed()),
                Math.min(1.0, brightness * accent.getGreen()),
                Math.min(1.0, brightness * accent.getBlue()),
                Math.min(1.0, alpha));
    }

    // Metodo che ridisegna lo sprite quando viene cambiata la skin
    public void reloadSprite() {
        try {
            String path = IscatSettings.player_skin;

            currentSprite = new Image(
                    Objects.requireNonNull(
                            PlayerView.class.getResourceAsStream(path),
                            "Sprite non trovato: " + path
                    )
            );

            //System.out.println("Player sprite ricaricato con successo: " + path);

        } catch (Exception e) {
            System.err.println("Errore caricamento sprite: " + IscatSettings.player_skin);
            e.printStackTrace();
        }
    }
}
