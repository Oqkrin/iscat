package uni.gaben.iscat.gamenex.universe.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.lib.interfaces.view.DrawableSpriteSheet;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;

import java.util.Random;

public class PlayerView extends AbstractEntityView implements Drawable<PlayerModel>, DrawableSpriteSheet {

    private static final Random RANDOM = new Random();

    private SpriteSheetsParser spriteSheet;
    // Inizializziamo l'animatore. Le dimensioni verranno impostate in updateSprite.
    private final SpriteSheetsAnimator animator = new SpriteSheetsAnimator(0.1, 1, 1);

    public PlayerView() {
        updateSprite(PlayerSettings.getPlayerSkin());
        PlayerSettings.playerSkinProperty().addListener((obs, old, newValue) -> updateSprite(newValue));
    }

    private void updateSprite(String path) {
        // 1. Carichiamo/Prendiamo lo spritesheet
        this.spriteSheet = SpritesLibrary.getInstance().getSprite(path, 32, 32);

        // 2. Sincronizziamo l'animatore con le nuove dimensioni dello spritesheet
        if (spriteSheet != null) {
            animator.constantDurationFiller(
                    0.1, // 100ms per frame di default
                    spriteSheet.getTotalFrames(),
                    spriteSheet.getTotalStates()
            );
        }
    }

    // --- Implementazione Drawable ---

    @Override
    public void draw(PlayerModel entity, GraphicsContext gc) {
        // Aggiorna il tempo dell'animazione
        animator.update(UU.UNIVERSE_TICK);

        gc.save();
        setPos(entity);
        gc.translate(cx, cy);

        setAngle(entity);
        gc.rotate(rotDeg);
        setSize(PlayerSettings.DIMENSIONE_DA_DISEGNARE);

        // Chiamata al metodo default dell'interfaccia
        drawSprite(gc, 0, 0, w, h);

        drawThrustEffect(gc, entity);
        gc.restore();

        drawHpBar(entity, gc);
    }

    private void drawThrustEffect(GraphicsContext gc, PlayerModel entity) {
        double vx = entity.getLinearVelocity().x;
        double vy = entity.getLinearVelocity().y;
        double speed = Math.sqrt(vx * vx + vy * vy) * UniverseSettings.SCALE;

        double intensity = Math.min(speed / (PlayerSettings.VELOCITA_MAX * UniverseSettings.SCALE), 1.0);
        if (intensity < 0.01) return;

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);

        // Prendiamo il colore primario dal ThemeManager (dinamico!)
        Color accentColor = ThemeManager.getInstance().getColor("accent-primary", Color.CYAN);

        for (int i = 0; i < particleCount; i++) {
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

    // Il metodo getParticleColor rimane simile ma ora accetta l'accent dinamico
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

    @Override
    public SpriteSheetsParser getSpriteSheet() {
        return spriteSheet;
    }

    @Override
    public SpriteSheetsAnimator getAnimator() {
        return animator;
    }
}