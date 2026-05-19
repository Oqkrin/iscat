package uni.gaben.iscat.game.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.game.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import java.util.Random;

public class PlayerView extends AbstractEntityView<PlayerModel>
        implements Drawable<PlayerModel>, DrawableSpriteSheet {

    private static final Random RANDOM = new Random();

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
        animator.update(UU.UNIVERSE_TICK);
        setupGraphicsContextAndDrawContent(entity, gc, 0.0);
        drawHpBar(entity, gc);
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
        intensity *= 3;

        if (intensity < 0.02) return;

        Vector2 localDrift = calculateLocalDrift(worldVelocity);

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);

        double maxThrustHeight = ScalareAureo.phiMaggiore(h);

        Color accent = ThemeManager.getInstance().getColor("accent-primary", Color.CYAN);

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();

            // Cono rimodellato: compatto all'inizio, si apre stabilmente a ventaglio senza disperdersi
            double spreadX = calculateConeSpread(w, distRatio);
            double size = calculateParticleSize(distRatio, intensity);

            // Ottimizzazione Curva: Spostamento dinamico reattivo
            double whipX = calculateWhipCurveX(distRatio, localDrift.x, maxThrustHeight);
            double dragY = localDrift.y * distRatio * (h * 0.15);

            // Distribuzione gaussiana focalizzata per massimizzare la densità al centro della curva
            double offsetX = (RANDOM.nextGaussian() * 0.28) * spreadX + whipX;
            double offsetY = (h/2)  + (distRatio * maxThrustHeight) + dragY;

            // Evita che le particelle fluttuino avanti nella navicella
            offsetY = Math.max(offsetY, (h/2) + distRatio * (h * 0.1));

            gc.setFill(getParticleColor(distRatio, intensity, RANDOM.nextDouble(), accent));
            gc.fillRect(offsetX - size / 2, offsetY - size / 2, size, size);
        }

        // JavaFX gc.restore() doesn't restore BlendMode! Must be reset manually.
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }

    private Vector2 calculateLocalDrift(Vector2 worldVelocity) {
        double normVx = worldVelocity.x / PlayerSettings.VELOCITA_MAX;
        double normVy = worldVelocity.y / PlayerSettings.VELOCITA_MAX;

        double cos = Math.cos(rotRad);
        double sin = Math.sin(rotRad);

        double localDriftX = -normVx * cos - normVy * sin;
        double localDriftY =  normVx * sin - normVy * cos;

        return new Vector2(localDriftX, localDriftY);
    }

    private double calculateConeSpread(double shipWidth, double distRatio) {
        // Base d'uscita solida e progressione a imbuto proporzionale senza sgranature laterali
        return shipWidth * (0.12 + Math.pow(distRatio, 1.3) * PlayerSettings.THRUST_SPREAD_X_FACTOR * 5);
    }

    private double calculateWhipCurveX(double distRatio, double localDriftX, double maxThrustHeight) {
        // Blocco del primo h/3 (0.33) fisso per andare dritti
        double curveStartPoint = 0.33;

        if (distRatio <= curveStartPoint) {
            return 0.0;
        }

        double curveRatio = (distRatio - curveStartPoint) / (1.0 - curveStartPoint);

        // Curva iper-visibile: moltiplicatore aumentato e curva quasi lineare (1.05) per accentuare la sbandata
        return localDriftX * Math.pow(curveRatio, 1.05) * (maxThrustHeight * 3.4);
    }

    private double calculateParticleSize(double distRatio, double intensity) {
        return (PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION)
                * (1.45 - distRatio)
                * (0.85 + intensity * 0.4);
    }

    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
        double baseAlpha = (1.0 - distanceRatio) * intensity;
        double alpha = Math.min(1.0, baseAlpha * (0.85 + colorMix * 0.3));

        if (distanceRatio < 0.45) {
            // ZONE 1: NEBULA BRIGHT CORE
            // Forte sovraccarico di bianco per dare l'effetto di emissione energetica luminosa
            double t = 1-distanceRatio;
            return Color.color(
                    Math.min(1.0, 1.0 + accent.getRed() * t),
                    Math.min(1.0, 1.0 + accent.getGreen() * t),
                    Math.min(1.0, 1.0 + accent.getBlue() * t),
                    Math.max(0.0, Math.min(1.0, intensity * 0.98))
            );

        } else if (distanceRatio < 0.85) {
            // ZONE 2: SATURATED ACCENT FLAME
            // Incrementata la brillantezza a 1.8 per far risaltare il colore primario in ADD mode
            double brightness = 1.8;
            return Color.color(
                    Math.min(1.0, accent.getRed() * brightness),
                    Math.min(1.0, accent.getGreen() * brightness),
                    Math.min(1.0, accent.getBlue() * brightness),
                    Math.max(0.0, Math.min(1.0, alpha * 0.9))
            );

        } else {
            // ZONE 3: TAIL COLD FADE
            double t = (distanceRatio - 0.72) / 0.28;
            double cooling = 1.0 - (t * 0.4);
            return Color.color(
                    accent.getRed() * cooling,
                    accent.getGreen() * cooling,
                    accent.getBlue() * cooling,
                    Math.max(0.0, Math.min(1.0, alpha * 0.7))
            );
        }
    }
}