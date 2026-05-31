package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasSprite;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.lib.interfaces.model.HasThrust;
import uni.gaben.iscat.universe.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.rendering.vfx.VFXRenderer;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EntityRenderer {

    private static final Map<Class<?>, BiConsumer<AbstractEntityModel, GraphicsContext>> CUSTOM_RENDERERS = new HashMap<>();
    // Cache: path|frameW|frameH -> SpriteSheetsParser
    private static final Map<String, SpriteSheetsParser> SHEET_CACHE = new HashMap<>();
    // Cache: path|frameW|frameH -> shared SpriteSheetsAnimator
    private static final Map<String, SpriteSheetsAnimator> ANIMATOR_CACHE = new HashMap<>();

    static {
        CUSTOM_RENDERERS.put(AsteroidModel.class, EntityRenderer::drawAsteroid);
        CUSTOM_RENDERERS.put(Projectile.class,   EntityRenderer::drawProjectile);
        CUSTOM_RENDERERS.put(PlayerModel.class,  EntityRenderer::drawPlayer);
    }

    private EntityRenderer() {}

    // ── Main entry point ──────────────────────────────────────────────

    public static void draw(AbstractEntityModel entity, GraphicsContext gc) {
        if (entity == null || entity.shouldRemove()) return;

        BiConsumer<AbstractEntityModel, GraphicsContext> custom = CUSTOM_RENDERERS.get(entity.getClass());
        if (custom != null) {
            custom.accept(entity, gc);
            return;
        }

        if (entity instanceof HasSprite sprite) {
            drawSpriteEntity(entity, sprite, gc);
        }
    }

    // ── Standard sprite pipeline (using shared animator) ──────────────

    private static void drawSpriteEntity(AbstractEntityModel entity, HasSprite sprite, GraphicsContext gc) {
        double cx = UU.mToPx(entity.getTransform().getTranslationX());
        double cy = UU.mToPx(entity.getTransform().getTranslationY());
        double w  = entity.getWidthPx();
        double h  = entity.getHeightPx();

        gc.save();
        gc.translate(cx, cy);

        SpriteSheetsParser sheet = getSheet(sprite);
        if (sheet != null) {

            // Use the shared animator to compute the current frame index
            SpriteSheetsAnimator animator = getAnimator(sprite);
            animator.setState(entity.getState());
            animator.setTime(entity.getStateTime());

            Image frame = sheet.getFrame(getAnimator(sprite).getCurrentState(), getAnimator(sprite).getCurrentFrame());
            if (frame != null) {
                Image tinted = ThemeManager.getInstance().getTintedImage(
                        frame,
                        ThemeManager.getInstance().globalTintProperty().get()
                );

                double baseAngle = Math.toDegrees(
                        sprite.canRotate()
                                ? entity.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET
                                : RenderingSettings.BASE_ROTRAD_OFFSET
                );
                double finalAngle = baseAngle + sprite.getVisualAngularOffsetDeg();

                gc.save();
                gc.rotate(finalAngle);
                gc.drawImage(tinted, -w / 2, -h / 2, w, h);
                gc.restore();
            }
        }

        // Shockwave
        if (entity instanceof HasShockwave sw && sw.shockwave().isActive()) {
            VFXRenderer.drawShockwave(gc, sw.shockwave());
        }

        // HP bar
        if (entity instanceof LifeDeath ld) {
            VFXRenderer.drawHpBar(ld, gc, w, h);
        }

        gc.restore();
    }

    private static SpriteSheetsAnimator getAnimator(HasSprite sprite) {
        String key = sprite.getSpritePath() + "|" + sprite.getSpriteFrameWidth() + "x" + sprite.getSpriteFrameHeight();
        return ANIMATOR_CACHE.computeIfAbsent(key, k -> {
            SpriteSheetsParser sheet = getSheet(sprite);
            int totalFrames = (sheet != null) ? sheet.getTotalFrames() : 1;
            int totalStates = (sheet != null) ? sheet.getTotalStates() : 1;
            // Use the default frame duration from HasSprite, or 1/6 as fallback
            double defDur = sprite.getFrameDuration();
            return new SpriteSheetsAnimator(defDur, totalFrames, totalStates);
        });
    }

    private static SpriteSheetsParser getSheet(HasSprite sprite) {
        String key = sprite.getSpritePath() + "|" + sprite.getSpriteFrameWidth() + "x" + sprite.getSpriteFrameHeight();
        return SHEET_CACHE.computeIfAbsent(key, k ->
                SpritesLibrary.getInstance().getSprite(
                        sprite.getSpritePath(),
                        sprite.getSpriteFrameWidth(),
                        sprite.getSpriteFrameHeight()
                )
        );
    }

    // ── Custom handlers ───────────────────────────────────────────────

    private static void drawProjectile(AbstractEntityModel e, GraphicsContext gc) {
        Projectile p = (Projectile) e;
        double w = e.getWidthPx(), h = e.getHeightPx();
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());

        gc.save();
        gc.translate(cx, cy);
        gc.setFill(p.getType().color);
        gc.fillOval(-w / 2, -h / 2, w, h);
        gc.restore();
    }

    private static void drawAsteroid(AbstractEntityModel e, GraphicsContext gc) {
        AsteroidModel asteroid = (AsteroidModel) e;
        Vector2[] vertices = asteroid.getDisplayVertices();
        double[] xPoints = new double[vertices.length];
        double[] yPoints = new double[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            Vector2 worldPoint = asteroid.getTransform().getTransformed(vertices[i]);
            xPoints[i] = UU.mToPx(worldPoint.x);
            yPoints[i] = UU.mToPx(worldPoint.y);
        }

        gc.setFill(ThemeManager.getInstance().getAccentTernary());
        gc.fillPolygon(xPoints, yPoints, vertices.length);
        gc.setStroke(ThemeManager.getInstance().getAccentPrimary());
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, vertices.length);
    }

    private static void drawPlayer(AbstractEntityModel e, GraphicsContext gc) {
        PlayerModel player = (PlayerModel) e;

        // 1. Draw the ship sprite using the standard pipeline
        if (player instanceof HasSprite sprite) {
            drawSpriteEntity(player, sprite, gc);
        }

        // 2. Draw the thrust effect (assumes PlayerModel implements HasThrust)
        if (player instanceof HasThrust thrustProvider && thrustProvider.thrust().isActive()) {
            double cx = UU.mToPx(player.getTransform().getTranslationX());
            double cy = UU.mToPx(player.getTransform().getTranslationY());
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(Math.toDegrees(player.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET));
            VFXRenderer.drawThrust(gc, thrustProvider.thrust());
            gc.restore();
        }
    }


}