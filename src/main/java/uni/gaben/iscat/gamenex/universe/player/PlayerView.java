package uni.gaben.iscat.gamenex.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import java.util.Random;

public class PlayerView extends AbstractEntityView<PlayerModel>
        implements Drawable<PlayerModel>, DrawableSpriteSheet {

    // 1. THE FIX: Re-added the dropped Random instance generator
    private static final Random RANDOM = new Random();

    private SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator = new SpriteSheetsAnimator(0.1, 1, 1);

    public PlayerView() {
        updateSprite(PlayerSettings.getPlayerSkin());
        PlayerSettings.playerSkinProperty().addListener((obs, old, nv) -> updateSprite(nv));
    }

    private void updateSprite(String path) {
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(path, 32, 32);
        if (spriteSheet != null) {
            animator.constantDurationFiller(0.1, spriteSheet.getTotalFrames(), spriteSheet.getTotalStates());
        }
    }

    @Override public SpriteSheetsParser getSpriteSheet() { return spriteSheet; }
    @Override public SpriteSheetsAnimator getAnimator() { return animator; }

    @Override
    public void draw(PlayerModel entity, GraphicsContext gc) {
        animator.update(UU.UNIVERSE_TICK);
        // Player asset points right by default: pass 0.0 angular offset
        renderEntity(entity, gc, 0.0);
    }

    @Override
    protected void drawContent(PlayerModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        // Render base character asset
        drawSprite(gc, x, y, width, height);
        // 2. THE FIX: Pass width and height cleanly into the particle generator
        drawThrustEffect(gc, entity, width, height);
    }

    // 3. THE FIX: Updated signature to receive local bounds dimensions (w and h)
    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity, double w, double h) {
        // Safe meters-to-meters calculation without messy scale tracking
        double speed = entity.getLinearVelocity().getMagnitude();
        double intensity = Math.min(speed / PlayerSettings.VELOCITA_MAX, 1.0);

        if (intensity < 0.01) return;

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);

        // Prendiamo il colore primario dal ThemeManager (dinamico!)
        Color accentColor = ThemeManager.getInstance().getColor("accent-primary", Color.CYAN);

        for (int i = 0; i < particleCount; i++) {
            // Because the canvas is ALREADY centered and rotated, the tail of the ship
            // sits at positive half-height (+h / 2). Particles move downwards from there.
            double baseY = h / 2;
            double maxThrustHeight = h * PlayerSettings.THRUST_HEIGHT_FACTOR;
            double offsetY = baseY + RANDOM.nextDouble() * maxThrustHeight;
            double distanceRatio = (offsetY - baseY) / maxThrustHeight;

            double maxSpreadX = w * PlayerSettings.THRUST_SPREAD_X_FACTOR * (1 + distanceRatio * 2);
            double offsetX = (RANDOM.nextDouble() - 0.5) * maxSpreadX * 2;
            double size = (PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION) * (1.2 - distanceRatio * 0.5);

            gc.setFill(getParticleColor(distanceRatio, intensity, RANDOM.nextDouble(), accentColor));
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }
    }

    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
        double alphaMix = Math.min(1.0, (1.0 - distanceRatio * 0.4) * intensity);
        double brightness = (colorMix < 0.3) ? 1.5 : (colorMix < 0.6) ? 1.0 : 0.5;
        double alpha = (colorMix < 0.3) ? 0.8 : (colorMix < 0.6) ? 0.5 : 0.2;

        return Color.color(
                Math.min(1.0, brightness * accent.getRed()),
                Math.min(1.0, brightness * accent.getGreen()),
                Math.min(1.0, brightness * accent.getBlue()),
                Math.min(1.0, alpha * alphaMix));
    }
}