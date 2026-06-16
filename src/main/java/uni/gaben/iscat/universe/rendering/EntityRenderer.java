package uni.gaben.iscat.universe.rendering;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.entity.*;
import uni.gaben.iscat.universe.entity.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entity.hardcoded.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;
import uni.gaben.iscat.universe.entity.interfaces.HasThrust;
import uni.gaben.iscat.universe.entity.interfaces.Alterable;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpriteUtils;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public final class EntityRenderer {

    public record SpriteKey(String path, int width, int height) {}

    private static final Map<Class<?>, BiConsumer<AbstractEntityModel, OptimizedLayeredRenderer>> LAYERED_RENDERERS = new HashMap<>();
    private static final Map<SpriteKey, SpriteSheetsAnimator> ANIMATOR_CACHE = new HashMap<>();

    static {
        LAYERED_RENDERERS.put(AsteroidModel.class, EntityRenderer::renderAsteroidBatched);
        LAYERED_RENDERERS.put(ProjectileModel.class,   EntityRenderer::renderProjectileBatched);
        LAYERED_RENDERERS.put(PlayerModel.class,  EntityRenderer::renderPlayerBatched);
        LAYERED_RENDERERS.put(BlackHoleModel.class, EntityRenderer::renderBlackHoleBatched);
    }

    private EntityRenderer() {}

    // Main entry point for batched rendering
    public static void renderLayered(AbstractEntityModel entity, OptimizedLayeredRenderer layers, boolean debug) {
        if (entity == null || entity.shouldRemove()) return;

        BiConsumer<AbstractEntityModel, OptimizedLayeredRenderer> custom = LAYERED_RENDERERS.get(entity.getClass());
        if (custom != null) {
            custom.accept(entity, layers);
        } else if (entity instanceof HasSprite sprite) {
            renderSpriteEntityBatched(entity, sprite, layers);
        }

        if (debug) {
            renderDebugCollisionBatched(entity, layers);
        }
    }

    // ------------------------------------------------------------------
    // Batched Sprite pipeline – collects transform + image + color tint
    // ------------------------------------------------------------------
    private static void renderSpriteEntityBatched(AbstractEntityModel entity, HasSprite sprite, OptimizedLayeredRenderer layers) {
        double cx = UU.mToPx(entity.getTransform().getTranslationX());
        double cy = UU.mToPx(entity.getTransform().getTranslationY());
        double w  = entity.getWidthPx();
        double h  = entity.getHeightPx();

        double baseAngle = Math.toDegrees(
                sprite.canRotate()
                        ? entity.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET
                        : RenderingSettings.BASE_ROTRAD_OFFSET
        );
        double finalAngle = baseAngle + sprite.getVisualAngularOffsetDeg();

        // Retrieve or create the sprite sheet & animator
        SpriteKey key = new SpriteKey(sprite.getSpritePath(), sprite.getSpriteFrameWidth(), sprite.getSpriteFrameHeight());
        SpriteSheetsParser sheet = SpritesLibrary.getInstance().getSprite(sprite.getSpritePath(), sprite.getSpriteFrameWidth(), sprite.getSpriteFrameHeight());
        if (sheet != null) {
            SpriteSheetsAnimator animator = getAnimator(key, sprite, sheet);
            animator.setState(entity.getState());
            animator.setTime(entity.getStateTime());

            Image frame = sheet.getFrame(animator.getCurrentState(), animator.getCurrentFrame());
            if (frame != null) {
                Color tint = (entity instanceof PlayerModel)
                        ? ThemeManager.getInstance().getAccentPrimary()
                        : ThemeManager.getInstance().getAccentSecondary();

                layers.addSprite(SpriteUtils.tinted(frame, tint), cx, cy, w, h, finalAngle, null);
            }
        }

        // Shockwave (if present) – also batched as a special effect
        if (entity instanceof HasShockwave sw && sw.shockwave().isActive()) {
            if ("iscat-master".equals(entity.getEntityRecord().entityKey())) {
                layers.addBlackHoleShockwave(cx, cy, sw.shockwave());
            } else {
                layers.addShockwave(cx, cy, sw.shockwave());
            }
        }

        // HP bar – queued as a rectangle pair (background + fill)
        if (entity instanceof Alterable ld) {
            double barX = cx - w/2;
            double barY = cy - h/2 - PlayerSettings.HP_BAR_OFFSET_Y;
            double percent = ld.getEndurance() / ld.getMaxEndurance();
            layers.addHpBar(barX, barY, w, PlayerSettings.HP_BAR_HEIGHT, percent);
        }
    }

    // ------------------------------------------------------------------
    // Custom batched handlers
    // ------------------------------------------------------------------
    private static void renderProjectileBatched(AbstractEntityModel e, OptimizedLayeredRenderer layers) {
        ProjectileModel p = (ProjectileModel) e;
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w  = e.getWidthPx();
        double h  = e.getHeightPx();

        Vector2 vel = p.getLinearVelocity();
        double speed = vel.getMagnitude();
        double trailX2 = cx;
        double trailY2 = cy;
        double trailWidth = ScalareAureo.phiMinore(h);
        
        if (speed > 0.1) {
            trailX2 = cx - ScalareAureo.phiMaggiore(vel.x);
            trailY2 = cy - ScalareAureo.phiMaggiore(vel.y);
        }

        Color baseColor = p.getType().color;
        Color strokeColor = baseColor.darker();

        layers.addProjectile(cx, cy, ScalareAureo.phiMaggiore(w), ScalareAureo.phiMaggiore(h), strokeColor,
                cx, cy, trailX2, trailY2, ScalareAureo.phiMaggiore(trailWidth));
        layers.addProjectile(cx, cy, w, h, baseColor,
                cx, cy, trailX2, trailY2, trailWidth);
    }

    private static void renderAsteroidBatched(AbstractEntityModel e, OptimizedLayeredRenderer batcher) {
        AsteroidModel asteroid = (AsteroidModel) e;
        Vector2[] vertices = asteroid.getDisplayVertices();
        if (vertices == null || vertices.length == 0) return;

        int len = vertices.length;
        double[] xPoints = new double[len];
        double[] yPoints = new double[len];
        for (int i = 0; i < len; i++) {
            Vector2 worldPoint = asteroid.getTransform().getTransformed(vertices[i]);
            xPoints[i] = UU.mToPx(worldPoint.x);
            yPoints[i] = UU.mToPx(worldPoint.y);
        }

        // Asteroid body (filled polygon + stroke)
        batcher.addFilledPolygon(xPoints, yPoints, ThemeManager.getInstance().getAccentTernary());
        batcher.addStrokedPolygon(xPoints, yPoints, ThemeManager.getInstance().getAccentPrimary(), 2.0);

        // Cracks (only when health low) – simple lines batched
        double healthRatio = asteroid.getDurabilityHealthRatio();
        if (healthRatio < 0.85) {
            double crackWidth = Math.max(2, (1.0 - healthRatio) * 4.0);
            int startIndex = getStartIndex(asteroid, vertices);
            int endIndex = (startIndex + len / 2) % len;
            double startX = xPoints[startIndex];
            double startY = yPoints[startIndex];
            double endX = xPoints[endIndex];
            double endY = yPoints[endIndex];
            double jitterMag = (1.0 - healthRatio) * (asteroid.getSize() / 7.0);
            double staticSeed = asteroid.hashCode();
            double midX = (startX + endX) / 2.0 + Math.sin(staticSeed) * jitterMag;
            double midY = (startY + endY) / 2.0 + Math.cos(staticSeed) * jitterMag;

            batcher.addLine(startX, startY, midX, midY, crackWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
            batcher.addLine(midX, midY, endX, endY, crackWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);

            if (healthRatio < 0.5) {
                double subWidth = crackWidth * 0.65;
                int branchIndex1 = (startIndex + len / 4) % len;
                double bMidX1 = (midX + xPoints[branchIndex1]) / 2.0 + Math.sin(staticSeed + 1) * (jitterMag * 0.4);
                double bMidY1 = (midY + yPoints[branchIndex1]) / 2.0 + Math.cos(staticSeed + 1) * (jitterMag * 0.4);
                batcher.addLine(midX, midY, bMidX1, bMidY1, subWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
                batcher.addLine(bMidX1, bMidY1, xPoints[branchIndex1], yPoints[branchIndex1], subWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);

                int branchIndex2 = (startIndex + (3 * len) / 4) % len;
                double bMidX2 = (midX + xPoints[branchIndex2]) / 2.0 + Math.sin(staticSeed + 2) * (jitterMag * 0.4);
                double bMidY2 = (midY + yPoints[branchIndex2]) / 2.0 + Math.cos(staticSeed + 2) * (jitterMag * 0.4);
                batcher.addLine(midX, midY, bMidX2, bMidY2, subWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
                batcher.addLine(bMidX2, bMidY2, xPoints[branchIndex2], yPoints[branchIndex2], subWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
            }
        }
    }

    private static void renderPlayerBatched(AbstractEntityModel e, OptimizedLayeredRenderer batcher) {
        PlayerModel player = (PlayerModel) e;
        // Draw the player sprite (if any)
        if (player instanceof HasSprite sprite) {
            renderSpriteEntityBatched(player, sprite, batcher);
        }
        // Thrust effect – batched as a group of particles (handled inside batcher)
        if (player instanceof HasThrust thrustProvider && thrustProvider.thrust().isActive()) {
            double cx = UU.mToPx(player.getTransform().getTranslationX());
            double cy = UU.mToPx(player.getTransform().getTranslationY());
            double angle = Math.toDegrees(player.getTransform().getRotationAngle())
                    + RenderingSettings.BASE_ROTDEG_OFFSET
                    + player.getVisualAngularOffsetDeg();
            batcher.addThrust(cx, cy, angle, thrustProvider.thrust());
        }
    }

    private static void renderBlackHoleBatched(AbstractEntityModel e, OptimizedLayeredRenderer batcher) {
        BlackHoleModel bh = (BlackHoleModel) e;
        double cx = UU.mToPx(bh.getTransform().getTranslationX());
        double cy = UU.mToPx(bh.getTransform().getTranslationY());
        if (bh.shockwave().isActive()) {
            batcher.addBlackHoleShockwave(cx, cy, bh.shockwave());
        }
    }

    private static void renderDebugCollisionBatched(AbstractEntityModel e, OptimizedLayeredRenderer renderer) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w = e.getWidthPx();
        double h = e.getHeightPx();
        double angle = Math.toDegrees(e.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET);

        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof Circle) {
            renderer.addStrokedOval(cx - (w/2), cy - (w/2), w, h, Color.LIME);
        } else {
            renderer.addStrokedRect(cx - w/2, cy - h/2, w, h, Color.LIME, angle);
        }
        renderer.addLine(cx, cy, cx + w/2, cy, 1.5, Color.RED, 1.0);
    }

    private static SpriteSheetsAnimator getAnimator(SpriteKey key, HasSprite sprite, SpriteSheetsParser sheet) {
        return ANIMATOR_CACHE.computeIfAbsent(key, k -> {
            double defDur = sprite.getFrameDuration();
            if (sheet != null) {
                return new SpriteSheetsAnimator(defDur, sheet.getFramesPerRow());
            } else {
                // Fallback: 1 state, 1 frame
                return new SpriteSheetsAnimator(defDur, 1, 1);
            }
        });
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
}