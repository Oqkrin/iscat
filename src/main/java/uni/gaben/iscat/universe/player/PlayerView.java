package uni.gaben.iscat.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.theme.ThemeManager;
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

        hurt.update(UU.UNIVERSE_TICK);

        setupGraphicsContextAndDrawContent(entity, gc, 0.0,true);
        lastLife = entity.getLife();
    }

    @Override
    protected void drawContent(PlayerModel entity, GraphicsContext gc, double x, double y, double width, double height) {
        drawSprite(gc, x, y, width, height);
        drawThrustEffect(gc, entity, width, height);
    }

    @Override
    public void setAnimatorTime(double time) {
        animator.setTime(time);
    }

    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity, double w, double h) {
        Vector2 worldVelocity = entity.getLinearVelocity();
        double speedMps = worldVelocity.getMagnitude();
        double intensity = Math.min(speedMps / PlayerSettings.VELOCITA_MAX, 1.0);

        if (intensity < 0.05) intensity = 0.05;

        Vector2 localDrift = calculateLocalDrift(worldVelocity);
        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;

        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();

            double spreadX = calculateConeSpread(w, distRatio);
            double size = calculateParticleSize(distRatio, intensity);

            double whipX = calculateWhipCurveX(distRatio, localDrift.x, w);
            double dragY = localDrift.y * distRatio * (h * 0.1);

            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight) + dragY;

            offsetY = Math.max(offsetY, (h / 2));

            // Genera il colore basandosi unicamente sulla tonalità dell'accent
            gc.setFill(getParticleColor(distRatio, intensity, RANDOM.nextDouble(), accent));

            gc.fillOval(offsetX - size / 2, offsetY - size / 2, size, size);
        }

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
        return shipWidth * (0.15 + Math.pow(distRatio, 1.5) * PlayerSettings.THRUST_SPREAD_X_FACTOR);
    }

    private double calculateWhipCurveX(double distRatio, double localDriftX, double shipWidth) {
        if (distRatio <= 0.7) {
            return 0.0;
        }
        double curveRatio = (distRatio - 0.15) / (1.0 - 0.15);
        return localDriftX * Math.pow(curveRatio, 5) * (shipWidth * 2);
    }

    private double calculateParticleSize(double distRatio, double intensity) {
        double baseSize = PlayerSettings.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION;
        return baseSize * (1.2 - distRatio * 0.9) * (0.7 + intensity * 0.5);
    }

    /**
     * Calcola la transizione cromatica dei vettori particellari in totale dipendenza dall'accent color.
     */
    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
        // L'opacità decresce man mano che ci si allontana dall'ugello
        double alpha = (1.0 - distanceRatio) * (0.4 + intensity * 0.6);

        if (distanceRatio < 0.25) {
            // ZONE 1: CORE EMISSION - Nucleo iper-luminoso sbiadito verso il bianco partendo dall'accent
            // Invece di forzare valori statici, mixiamo l'accento puro verso il bianco (1.0)
            double t = distanceRatio / 0.25;
            return Color.color(
                    accent.getRed()   + (1.0 - accent.getRed())   * (1.0 - t),
                    accent.getGreen() + (1.0 - accent.getGreen()) * (1.0 - t),
                    accent.getBlue()  + (1.0 - accent.getBlue())  * (1.0 - t),
                    alpha
            );
        } else if (distanceRatio < 0.7) {
            // ZONE 2: ACCENT ENGINE FLAME - Colore dell'accento puro al 100%
            return Color.color(
                    accent.getRed(),
                    accent.getGreen(),
                    accent.getBlue(),
                    alpha * 0.85
            );
        } else {
            // ZONE 3: TAIL COLD FADE - Dissipazione termica e raffreddamento graduale dei canali dell'accento
            double t = (distanceRatio - 0.7) / 0.3;
            double cooling = 1.0 - (t * 0.75); // Abbassa l'energia dei canali cromatici originali
            return Color.color(
                    Math.max(0.0, accent.getRed()   * cooling),
                    Math.max(0.0, accent.getGreen() * cooling),
                    Math.max(0.0, accent.getBlue()  * cooling),
                    alpha * (1.0 - t)
            );
        }
    }

    @Override
    public Color getTint() {
        return hurt.isCoolingDown() ? ThemeManager.getInstance().getColorError() : ThemeManager.getInstance().getAccentPrimary();
    }
}