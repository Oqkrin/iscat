package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.universe.entity.enviroment.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.HasSprite;
import uni.gaben.iscat.universe.entity.HasShockwave;
import uni.gaben.iscat.universe.entity.HasThrust;
import uni.gaben.iscat.universe.entity.LifeDeath;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EntityRenderer {

    // Using a Java Record as a map key entirely eliminates performance-heavy String concatenations every frame
    public record SpriteKey(String path, int width, int height) {}

    private static final Map<Class<?>, BiConsumer<AbstractEntityModel, GraphicsContext>> CUSTOM_RENDERERS = new HashMap<>();
    private static final Map<SpriteKey, SpriteSheetsParser> SHEET_CACHE = new HashMap<>();
    private static final Map<SpriteKey, SpriteSheetsAnimator> ANIMATOR_CACHE = new HashMap<>();

    private static final Effect projectileEffect = new GaussianBlur();
    private static final Effect spriteEffect = new Bloom();

    // Statically allocated primitive buffers to completely eliminate per-frame asteroid double[] garbage generation
    private static double[] xBuffer = new double[64];
    private static double[] yBuffer = new double[64];

    static {
        CUSTOM_RENDERERS.put(AsteroidModel.class, EntityRenderer::drawAsteroid);
        CUSTOM_RENDERERS.put(Projectile.class,   EntityRenderer::drawProjectile);
        CUSTOM_RENDERERS.put(PlayerModel.class,  EntityRenderer::drawPlayer);
        CUSTOM_RENDERERS.put(BlackHoleModel.class,  EntityRenderer::drawBlackHole);
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

    private static void drawBlackHole(AbstractEntityModel abstractEntityModel, GraphicsContext gc) {
        BlackHoleModel bh = (BlackHoleModel) abstractEntityModel;
        double cx = UU.mToPx(bh.getTransform().getTranslationX());
        double cy = UU.mToPx(bh.getTransform().getTranslationY());

        gc.save();
        gc.translate(cx, cy);
        if (bh.shockwave().isActive()) VFXRenderer.drawBlackHole(gc, bh.shockwave());
        gc.restore();
    }

    // ── Standard sprite pipeline ──────────────────────────────────────

    private static void drawSpriteEntity(AbstractEntityModel entity, HasSprite sprite, GraphicsContext gc) {
        double cx = UU.mToPx(entity.getTransform().getTranslationX());
        double cy = UU.mToPx(entity.getTransform().getTranslationY());
        double w  = entity.getWidthPx();
        double h  = entity.getHeightPx();

        gc.save();
        gc.translate(cx, cy);

        // NOTE ON PERFORMANCE: Calling gc.setEffect() inside hot loops forces JavaFX to break hardware-accelerated
        // batch rendering, incurring expensive render-target changes. For optimal performance, apply Bloom to
        // the entire parent Canvas layer once, or bake the glow effects straight into your sprite sheets.
        gc.setEffect(spriteEffect);

        SpriteKey key = new SpriteKey(sprite.getSpritePath(), sprite.getSpriteFrameWidth(), sprite.getSpriteFrameHeight());
        SpriteSheetsParser sheet = getSheet(key, sprite);
        if (sheet != null) {

            // Reused the local variable instead of recalculating getAnimator(sprite) 3 times sequentially
            SpriteSheetsAnimator animator = getAnimator(key, sprite, sheet);
            animator.setState(entity.getState());
            animator.setTime(entity.getStateTime());

            Image frame = sheet.getFrame(animator.getCurrentState(), animator.getCurrentFrame());
            if (frame != null) {
                Image tinted = ThemeManager.getInstance().getTintedImage(
                        frame,
                        entity instanceof PlayerModel ? ThemeManager.getInstance().getAccentPrimary() : ThemeManager.getInstance().getAccentSecondary()
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
            if ("iscat-master".equals(entity.getEntityKey())) {
                VFXRenderer.drawBlackHole(gc, sw.shockwave());
            } else {
                VFXRenderer.drawShockwave(gc, sw.shockwave());
            }
        }

        // HP bar
        if (entity instanceof LifeDeath ld) {
            VFXRenderer.drawHpBar(ld, gc, w, h);
        }

        gc.restore();
    }

    private static SpriteSheetsAnimator getAnimator(SpriteKey key, HasSprite sprite, SpriteSheetsParser sheet) {
        return ANIMATOR_CACHE.computeIfAbsent(key, k -> {
            double defDur = sprite.getFrameDuration();
            if (sheet != null) {
                return new SpriteSheetsAnimator(defDur, sheet.getFramesPerRow());
            } else {
                return new SpriteSheetsAnimator(defDur, 1, 1);
            }
        });
    }

    private static SpriteSheetsParser getSheet(SpriteKey key, HasSprite sprite) {
        return SHEET_CACHE.computeIfAbsent(key, k ->
                SpritesLibrary.getInstance().getSprite(
                        k.path(),
                        k.width(),
                        k.height()
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
        gc.setEffect(projectileEffect);
        gc.translate(cx, cy);

        // DELETED: Instantly removed the completely transparent outer fillOval call.
        // Filling an oval with a fully transparent color under default SRC_OVER blending has no
        // physical output but wastes substantial GPU pixel fill rate when many bullets are on-screen.
        gc.setFill(p.getType().color);
        gc.fillOval(-w / 2, -h / 2, w, h);
        gc.restore();
    }

    private static void drawAsteroid(AbstractEntityModel e, GraphicsContext gc) {
        AsteroidModel asteroid = (AsteroidModel) e;
        Vector2[] vertices = asteroid.getDisplayVertices();
        if (vertices == null || vertices.length == 0) return;

        int len = vertices.length;
        // Dynamically grow primitive cache bounds if an asteroid exceeds historical limits
        if (xBuffer.length < len) {
            xBuffer = new double[len * 2];
            yBuffer = new double[len * 2];
        }

        for (int i = 0; i < len; i++) {
            Vector2 worldPoint = asteroid.getTransform().getTransformed(vertices[i]);
            xBuffer[i] = UU.mToPx(worldPoint.x);
            yBuffer[i] = UU.mToPx(worldPoint.y);
        }

        // Draw Core Asteroid Body
        gc.setFill(ThemeManager.getInstance().getAccentTernary());
        gc.fillPolygon(xBuffer, yBuffer, len);
        gc.setStroke(ThemeManager.getInstance().getAccentPrimary());
        gc.setLineWidth(2);
        gc.strokePolygon(xBuffer, yBuffer, len);

        double healthRatio = asteroid.getDurabilityHealthRatio();
        if (healthRatio < 0.85) {
            gc.save();
            double crackWidth = (1.0 - healthRatio) * 4.0;
            gc.setLineWidth(Math.max(2, crackWidth));

            int startIndex = getStartIndex(asteroid, vertices);
            int endIndex = (startIndex + len / 2) % len;

            double startX = xBuffer[startIndex];
            double startY = yBuffer[startIndex];
            double endX = xBuffer[endIndex];
            double endY = yBuffer[endIndex];

            double jitterMag = (1.0 - healthRatio) * (asteroid.getSize() / 7.0);
            double staticSeed = asteroid.hashCode();
            double midX = (startX + endX) / 2.0 + Math.sin(staticSeed) * jitterMag;
            double midY = (startY + endY) / 2.0 + Math.cos(staticSeed) * jitterMag;

            // 1. Draw Primary Division Crack
            gc.strokeLine(startX, startY, midX, midY);
            gc.strokeLine(midX, midY, endX, endY);

            // 2. Draw Secondary Transverse Branches
            if (healthRatio < 0.5) {
                gc.setLineWidth(Math.max(1.0, crackWidth * 0.65));

                int branchIndex1 = (startIndex + len / 4) % len;
                double bMidX1 = (midX + xBuffer[branchIndex1]) / 2.0 + Math.sin(staticSeed + 1) * (jitterMag * 0.4);
                double bMidY1 = (midY + yBuffer[branchIndex1]) / 2.0 + Math.cos(staticSeed + 1) * (jitterMag * 0.4);
                gc.strokeLine(midX, midY, bMidX1, bMidY1);
                gc.strokeLine(bMidX1, bMidY1, xBuffer[branchIndex1], yBuffer[branchIndex1]);

                int branchIndex2 = (startIndex + (3 * len) / 4) % len;
                double bMidX2 = (midX + xBuffer[branchIndex2]) / 2.0 + Math.sin(staticSeed + 2) * (jitterMag * 0.4);
                double bMidY2 = (midY + yBuffer[branchIndex2]) / 2.0 + Math.cos(staticSeed + 2) * (jitterMag * 0.4);
                gc.strokeLine(midX, midY, bMidX2, bMidY2);
                gc.strokeLine(bMidX2, bMidY2, xBuffer[branchIndex2], yBuffer[branchIndex2]);
            }

            gc.restore();
        }
    }

    private static int getStartIndex(AsteroidModel asteroid, Vector2[] vertices) {
        double localFaultAngle = (asteroid.getSplitAngle() + Math.PI / 2) % (Math.PI * 2);
        if (localFaultAngle < 0) localFaultAngle += Math.PI * 2;

        int startIndex = 0;
        double minDiff = Double.MAX_VALUE;
        for (int i = 0; i < vertices.length; i++) {
            double vAngle = Math.atan2(vertices[i].y, vertices[i].x);
            if (vAngle < 0) vAngle += Math.PI * 2;

            double diff = Math.abs(vAngle - localFaultAngle);
            diff = Math.min(diff, Math.PI * 2 - diff);
            if (diff < minDiff) {
                minDiff = diff;
                startIndex = i;
            }
        }
        return startIndex;
    }

    private static void drawPlayer(AbstractEntityModel e, GraphicsContext gc) {
        PlayerModel player = (PlayerModel) e;

        if (player instanceof HasSprite sprite) {
            drawSpriteEntity(player, sprite, gc);
        }

        if (player instanceof HasThrust thrustProvider && thrustProvider.thrust().isActive()) {
            double cx = UU.mToPx(player.getTransform().getTranslationX());
            double cy = UU.mToPx(player.getTransform().getTranslationY());
            gc.save();
            gc.translate(cx, cy);
            gc.rotate(Math.toDegrees(player.getTransform().getRotationAngle()) + RenderingSettings.BASE_ROTDEG_OFFSET + player.getVisualAngularOffsetDeg());
            VFXRenderer.drawThrust(gc, thrustProvider.thrust());
            gc.restore();
        }
    }
}