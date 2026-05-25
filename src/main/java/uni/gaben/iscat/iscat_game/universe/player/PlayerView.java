package uni.gaben.iscat.iscat_game.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import java.util.Random;

public class PlayerView extends AbstractEntityView<PlayerModel>
        implements Drawable<PlayerModel>, DrawableSpriteSheet {

    private static final Random RANDOM = new Random();

    private final Cooldown hurt = new Cooldown();
    private double lastLife = 0.0;

    private SpriteSheetsParser spriteSheet;
    private final SpriteSheetsAnimator animator = new SpriteSheetsAnimator(0.1, 1, 1);

    public PlayerView() {
        spriteScale = PlayerSettings.MASSA;
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
        if(lastLife >= 0.0 && lastLife > entity.getLife()) {
            hurt.start(.1);
        }

        animator.update(UU.UNIVERSE_TICK);
        hurt.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 0.0);
        drawHpBar(entity, gc);
        lastLife = entity.getLife();
    }

    @Override
    protected void drawContent(PlayerModel entity, GraphicsContext gc, double x, double y, double width, double height) {

        drawSprite(gc, x, y, width, height);
        drawThrustEffect(gc, entity, width, height);

    }

    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity, double w, double h) {
        Vector2 worldVelocity = entity.getLinearVelocity();
        double speedMps = worldVelocity.getMagnitude();
        double intensity = Math.min(speedMps / PlayerSettings.VELOCITA_MAX, 1.0);

        // Keep a subtle idling flame even when stationary
        if (intensity < 0.05) intensity = 0.05;

        Vector2 localDrift = calculateLocalDrift(worldVelocity);
        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;

        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();

            // Calculate tight cone expansion and particle sizes
            double spreadX = calculateConeSpread(w, distRatio);
            double size = calculateParticleSize(distRatio, intensity);

            // Sane trailing drift interpolation (prevents detaching from nozzles)
            double whipX = calculateWhipCurveX(distRatio, localDrift.x, w);
            double dragY = localDrift.y * distRatio * (h * 0.1);

            // Refined Gaussian layout focused heavily down the center line
            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight) + dragY;

            // Clamp particle spawn origin so they don't leak inside the ship model
            offsetY = Math.max(offsetY, (h / 2));

            gc.setFill(getParticleColor(distRatio, intensity, RANDOM.nextDouble(), accent));

            // Render smooth circles instead of harsh blocky squares
            gc.fillOval(offsetX - size / 2, offsetY - size / 2, size, size);
        }

        // Explicitly clear blend mode before popping the graphics stack
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }

    private Vector2 calculateLocalDrift(Vector2 worldVelocity) {
        double normVx = worldVelocity.x / PlayerSettings.VELOCITA_MAX;
        double normVy = worldVelocity.y / PlayerSettings.VELOCITA_MAX;

        double cos = Math.cos(rotRad);
        double sin = Math.sin(rotRad);

        // Map global velocity vector to the local relative coordinate space of the ship orientation
        double localDriftX = -normVx * cos - normVy * sin;
        double localDriftY =  normVx * sin - normVy * cos;

        return new Vector2(localDriftX, localDriftY);
    }

    private double calculateConeSpread(double shipWidth, double distRatio) {
        // Keeps the nozzle base narrow and tapers out smoothly near the exhaust tip
        return shipWidth * (0.15 + Math.pow(distRatio, 1.5) * PlayerSettings.THRUST_SPREAD_X_FACTOR);
    }

    private double calculateWhipCurveX(double distRatio, double localDriftX, double shipWidth) {
        if (distRatio <= 0.7) {
            return 0.0; // Keep the core anchored straight out of the physical nozzle
        }
        double curveRatio = (distRatio - 0.15) / (1.0 - 0.15);
        // Uses ship width as base scaling instead of an infinite multiplier to prevent tearing
        return localDriftX * Math.pow(curveRatio, 5) * (shipWidth * 2);
    }

    private double calculateParticleSize(double distRatio, double intensity) {
        double baseSize = PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION;
        // High intensity swells the flame, particles shrink down to a fine tip at the tail end
        return baseSize * (1.2 - distRatio * 0.9) * (0.7 + intensity * 0.5);
    }

    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
        double alpha = (1.0 - distanceRatio) * (0.4 + intensity * 0.6);

        if (distanceRatio < 0.25) {
            // ZONE 1: CORE EMISSION - Bright white-hot plasma heart
            double t = distanceRatio / 0.25;
            return Color.color(
                    1.0,
                    Math.min(1.0, 0.9 + accent.getGreen() * t),
                    Math.min(1.0, 0.8 + accent.getBlue() * t),
                    alpha
            );
        } else if (distanceRatio < 0.7) {
            // ZONE 2: ACCENT ENGINE FLAME - Saturated primary energy color
            return Color.color(
                    accent.getRed(),
                    accent.getGreen(),
                    accent.getBlue(),
                    alpha * 0.85
            );
        } else {
            // ZONE 3: TAIL COLD FADE - Thermal dissipation into deep vacuum
            double t = (distanceRatio - 0.7) / 0.3;
            double cooling = 1.0 - (t * 0.5);
            return Color.color(
                    accent.getRed() * cooling,
                    accent.getGreen() * cooling,
                    accent.getBlue() * cooling,
                    alpha * (1.0 - t)
            );
        }
    }

    @Override
    public Color getTint() {
        return hurt.isCoolingDown() ? ThemeManager.getInstance().getColorError() : DrawableSpriteSheet.super.getTint();
    }
}