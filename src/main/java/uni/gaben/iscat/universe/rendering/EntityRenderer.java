package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.universe.enviroment.blackhole.BlackHoleModel;
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
        CUSTOM_RENDERERS.put(BlackHoleModel.class,  EntityRenderer::drawBlackHole);
    }

    private static void drawBlackHole(AbstractEntityModel abstractEntityModel, GraphicsContext gc) {
        BlackHoleModel bh = (BlackHoleModel) abstractEntityModel;
        double cx = UU.mToPx(bh.getTransform().getTranslationX());
        double cy = UU.mToPx(bh.getTransform().getTranslationY());

        gc.save();
        gc.translate(cx, cy);
        if(bh.shockwave().isActive()) VFXRenderer.drawShockwave(gc, bh.shockwave());
        gc.restore();

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
            double defDur = sprite.getFrameDuration();

            if (sheet != null) {
                // Supply per-row frame counts so variable-length rows animate correctly
                return new SpriteSheetsAnimator(defDur, sheet.getFramesPerRow());
            } else {
                return new SpriteSheetsAnimator(defDur, 1, 1);
            }
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
        if (vertices == null || vertices.length == 0) return;

        double[] xPoints = new double[vertices.length];
        double[] yPoints = new double[vertices.length];

        for (int i = 0; i < vertices.length; i++) {
            Vector2 worldPoint = asteroid.getTransform().getTransformed(vertices[i]);
            xPoints[i] = UU.mToPx(worldPoint.x);
            yPoints[i] = UU.mToPx(worldPoint.y);
        }

        // Draw Core Asteroid Body
        gc.setFill(ThemeManager.getInstance().getAccentTernary());
        gc.fillPolygon(xPoints, yPoints, vertices.length);
        gc.setStroke(ThemeManager.getInstance().getAccentPrimary());
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, vertices.length);

        double healthRatio = asteroid.getDurabilityHealthRatio();
        if (healthRatio < 0.85) {
            gc.save();
            // Cracks expand in width as health deteriorates
            double crackWidth = (1.0 - healthRatio) * 4.0;
            gc.setLineWidth(Math.max(2, crackWidth));

            // The structural fault plane is perpendicular to the child separation push angle
            double localFaultAngle = (asteroid.getSplitAngle() + Math.PI / 2) % (Math.PI * 2);
            if (localFaultAngle < 0) localFaultAngle += Math.PI * 2;

            // Find the boundary vertex closest to our structural fault line direction
            int startIndex = 0;
            double minDiff = Double.MAX_VALUE;
            for (int i = 0; i < vertices.length; i++) {
                double vAngle = Math.atan2(vertices[i].y, vertices[i].x);
                if (vAngle < 0) vAngle += Math.PI * 2;

                double diff = Math.abs(vAngle - localFaultAngle);
                diff = Math.min(diff, Math.PI * 2 - diff); // Keep difference inside circular bounds
                if (diff < minDiff) {
                    minDiff = diff;
                    startIndex = i;
                }
            }
            // The opposing point across the polygon hull layout
            int endIndex = (startIndex + vertices.length / 2) % vertices.length;

            double startX = xPoints[startIndex];
            double startY = yPoints[startIndex];
            double endX = xPoints[endIndex];
            double endY = yPoints[endIndex];

            // Jitter amount increases as health falls, making the fault line look stressed
            double jitterMag = (1.0 - healthRatio) * (asteroid.getSize() / 7.0);

            // Use object hash code as a static seed to prevent crack lines from flickering erratically
            double staticSeed = asteroid.hashCode();
            double midX = (startX + endX) / 2.0 + Math.sin(staticSeed) * jitterMag;
            double midY = (startY + endY) / 2.0 + Math.cos(staticSeed) * jitterMag;

            // 1. Draw Primary Division Crack (cuts through the core completely)
            gc.strokeLine(startX, startY, midX, midY);
            gc.strokeLine(midX, midY, endX, endY);

            // 2. Draw Secondary Transverse Branches if the asteroid is critically damaged (< 50% HP)
            if (healthRatio < 0.5) {
                gc.setLineWidth(Math.max(1.0, crackWidth * 0.65)); // Branch cracks are thinner

                // Branch left towards an orthogonal vertex
                int branchIndex1 = (startIndex + vertices.length / 4) % vertices.length;
                double bMidX1 = (midX + xPoints[branchIndex1]) / 2.0 + Math.sin(staticSeed + 1) * (jitterMag * 0.4);
                double bMidY1 = (midY + yPoints[branchIndex1]) / 2.0 + Math.cos(staticSeed + 1) * (jitterMag * 0.4);
                gc.strokeLine(midX, midY, bMidX1, bMidY1);
                gc.strokeLine(bMidX1, bMidY1, xPoints[branchIndex1], yPoints[branchIndex1]);

                // Branch right towards the opposite orthogonal vertex
                int branchIndex2 = (startIndex + (3 * vertices.length) / 4) % vertices.length;
                double bMidX2 = (midX + xPoints[branchIndex2]) / 2.0 + Math.sin(staticSeed + 2) * (jitterMag * 0.4);
                double bMidY2 = (midY + yPoints[branchIndex2]) / 2.0 + Math.cos(staticSeed + 2) * (jitterMag * 0.4);
                gc.strokeLine(midX, midY, bMidX2, bMidY2);
                gc.strokeLine(bMidX2, bMidY2, xPoints[branchIndex2], yPoints[branchIndex2]);
            }

            gc.restore();
        }
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
            gc.rotate(Math.toDegrees(player.getTransform().getRotationAngle())+RenderingSettings.BASE_ROTDEG_OFFSET+player.getVisualAngularOffsetDeg());
            VFXRenderer.drawThrust(gc, thrustProvider.thrust());
            gc.restore();
        }
    }
}