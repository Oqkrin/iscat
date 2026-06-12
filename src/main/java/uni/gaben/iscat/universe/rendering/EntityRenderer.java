package uni.gaben.iscat.universe.rendering;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.entity.*;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.universe.entity.modules.MovementModule;
import uni.gaben.iscat.universe.entity.modules.PhysicsModule;
import uni.gaben.iscat.universe.entity.modules.SpriteModule;
import uni.gaben.iscat.universe.entity.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entity.player.PlayerSettings;
import uni.gaben.iscat.utils.sprite.SpriteSheetsAnimator;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpriteUtils;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.HashMap;
import java.util.Map;

public final class EntityRenderer {

    public record SpriteKey(String path, int width, int height) {}

    private static final Map<SpriteKey, SpriteSheetsParser> SHEET_CACHE = new HashMap<>();
    private static final Map<SpriteKey, SpriteSheetsAnimator> ANIMATOR_CACHE = new HashMap<>();

    private EntityRenderer() {}

    public static void drawBatched(GameEntity entity, BatchedDrawCollector batcher, boolean debug) {
        if (entity == null || entity.shouldRemove()) return;

        if (entity.getRecord() != null && entity.getRecord().physics() != null && entity.getRecord().physics().isProjectile()) {
            drawProjectileBatched(entity, batcher);
        } else {
            String key = entity.getRecord().identity().entityKey();
            if (key != null) {
                if (key.equals("asteroid") || key.equals("small_asteroid")) {
                    drawAsteroidBatched(entity, batcher);
                } else if (key.equals("blackhole")) {
                    drawBlackHoleBatched(entity, batcher);
                } else if (key.contains("player")) {
                    drawPlayerBatched(entity, batcher);
                } else if (entity.hasModule(SpriteModule.class)) {
                    drawSpriteEntityBatched(entity, entity.getModule(SpriteModule.class), batcher);
                }
            } else if (entity.hasModule(SpriteModule.class)) {
                drawSpriteEntityBatched(entity, entity.getModule(SpriteModule.class), batcher);
            }
        }

        if (debug) {
            drawDebugCollisionBatched(entity, batcher);
        }
    }

    private static void drawSpriteEntityBatched(GameEntity entity, SpriteModule sprite, BatchedDrawCollector batcher) {
        double cx = UU.mToPx(entity.getTransform().getTranslationX());
        double cy = UU.mToPx(entity.getTransform().getTranslationY());
        double w  = sprite.getSpriteFrameWidth() * sprite.getVisualScale();
        double h  = sprite.getSpriteFrameHeight() * sprite.getVisualScale();

        double baseAngle = Math.toDegrees(entity.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET);
        double finalAngle = baseAngle + sprite.getVisualAngularOffsetDeg();

        SpriteKey key = new SpriteKey(sprite.getSpritePath(), sprite.getSpriteFrameWidth(), sprite.getSpriteFrameHeight());
        SpriteSheetsParser sheet = getSheet(key, sprite);
        if (sheet != null) {
            SpriteSheetsAnimator animator = getAnimator(key, sprite, sheet);
            // Default to idle state unless a state module manages states
            int state = 1; 
            double time = 0;
            if (entity.hasModule(uni.gaben.iscat.universe.entity.modules.StateModule.class)) {
                uni.gaben.iscat.universe.entity.modules.StateModule sm = entity.getModule(uni.gaben.iscat.universe.entity.modules.StateModule.class);
                state = sm.getState();
                time = sm.getStateTime();
            }
            animator.setState(state);
            animator.setTime(time);

            Image frame = sheet.getFrame(animator.getCurrentState(), animator.getCurrentFrame());
            if (frame != null) {
                Color tint = (entity.getRecord().identity().entityKey().contains("player"))
                        ? ThemeManager.getInstance().getAccentPrimary()
                        : ThemeManager.getInstance().getAccentSecondary();

                batcher.addSprite(SpriteUtils.tinted(frame, tint), cx, cy, w, h, finalAngle, null);
            }
        }

        if (entity instanceof HasShockwave sw && sw.shockwave().isActive()) {
            if ("iscat_master".equals(entity.getRecord().identity().entityKey())) {
                batcher.addBlackHoleShockwave(cx, cy, sw.shockwave());
            } else {
                batcher.addShockwave(cx, cy, sw.shockwave());
            }
        }

        if (entity.hasModule(EnduranceModule.class)) {
            EnduranceModule em = entity.getModule(EnduranceModule.class);
            double barX = cx - w/2;
            double barY = cy - h/2 - PlayerSettings.HP_BAR_OFFSET_Y;
            double percent = em.getEndurance() / em.getMaxEndurance();
            batcher.addHpBar(barX, barY, w, PlayerSettings.HP_BAR_HEIGHT, percent);
        }
    }

    private static void drawProjectileBatched(GameEntity e, BatchedDrawCollector batcher) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w  = 8.0; // Default width
        double h  = 8.0; // Default height
        if (e.getRecord() != null && e.getRecord().sprite() != null) {
            w = e.getRecord().sprite().frameW() * e.getRecord().sprite().scale();
            h = e.getRecord().sprite().frameH() * e.getRecord().sprite().scale();
        } else if (e.getRecord() != null && e.getRecord().physics() != null) {
            w = e.getRecord().physics().radius() * 2;
            h = e.getRecord().physics().radius() * 2;
        }

        Vector2 vel = e.getLinearVelocity();
        double speed = vel.getMagnitude();
        double trailX2 = cx, trailY2 = cy;
        double trailWidth = h * 0.75;
        if (speed > 0.1) {
            trailX2 = cx - vel.x * 1.618;
            trailY2 = cy - vel.y * 1.618;
        }

        // Determina il colore dal record, se enemy rosso altrimenti blu (default giallo se manca info)
        Color color = Color.YELLOW;
        if (e.getRecord() != null && e.getRecord().identity() != null) {
            if (e.getRecord().identity().isEnemy()) color = Color.RED;
            else if (e.getRecord().identity().entityKey().contains("player")) color = Color.CYAN;
        }

        batcher.addProjectile(cx, cy, w, h, color, cx, cy, trailX2, trailY2, trailWidth);
    }

    private static void drawAsteroidBatched(GameEntity e, BatchedDrawCollector batcher) {
        if (!e.hasModule(PhysicsModule.class)) return;
        org.dyn4j.geometry.Shape shape = e.getModule(PhysicsModule.class).getFixture().getShape();
        if (!(shape instanceof Polygon poly)) return;
        
        Vector2[] vertices = poly.getVertices();
        if (vertices == null || vertices.length == 0) return;

        int len = vertices.length;
        double[] xPoints = new double[len];
        double[] yPoints = new double[len];
        for (int i = 0; i < len; i++) {
            Vector2 worldPoint = e.getTransform().getTransformed(vertices[i]);
            xPoints[i] = UU.mToPx(worldPoint.x);
            yPoints[i] = UU.mToPx(worldPoint.y);
        }

        batcher.addFilledPolygon(xPoints, yPoints, ThemeManager.getInstance().getAccentTernary());
        batcher.addStrokedPolygon(xPoints, yPoints, ThemeManager.getInstance().getAccentPrimary(), 2.0);

        if (e.hasModule(EnduranceModule.class)) {
            EnduranceModule em = e.getModule(EnduranceModule.class);
            double healthRatio = em.getEndurance() / em.getMaxEndurance();
            if (healthRatio < 0.85) {
                double crackWidth = Math.max(2, (1.0 - healthRatio) * 4.0);
                double staticSeed = e.hashCode();
                int startIndex = 0;
                int endIndex = len / 2;
                double startX = xPoints[startIndex];
                double startY = yPoints[startIndex];
                double endX = xPoints[endIndex];
                double endY = yPoints[endIndex];
                
                double size = poly.getRadius();
                double jitterMag = (1.0 - healthRatio) * (size / 7.0);
                double midX = (startX + endX) / 2.0 + Math.sin(staticSeed) * jitterMag;
                double midY = (startY + endY) / 2.0 + Math.cos(staticSeed) * jitterMag;

                batcher.addLine(startX, startY, midX, midY, crackWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
                batcher.addLine(midX, midY, endX, endY, crackWidth, ThemeManager.getInstance().getAccentPrimary(), 1.0);
            }
        }
    }

    private static void drawPlayerBatched(GameEntity e, BatchedDrawCollector batcher) {
        if (e.hasModule(SpriteModule.class)) {
            drawSpriteEntityBatched(e, e.getModule(SpriteModule.class), batcher);
        }
        if (e.hasModule(MovementModule.class)) {
            MovementModule mov = e.getModule(MovementModule.class);
            if (mov.thrustState() != null && mov.thrustState().isActive()) {
                double cx = UU.mToPx(e.getTransform().getTranslationX());
                double cy = UU.mToPx(e.getTransform().getTranslationY());
                double angle = Math.toDegrees(e.getTransform().getRotationAngle()) + RenderingSettings.BASE_ROTDEG_OFFSET;
                batcher.addThrust(cx, cy, angle, mov.thrustState());
            }
        }
    }

    private static void drawBlackHoleBatched(GameEntity e, BatchedDrawCollector batcher) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        if (e instanceof HasShockwave hw && hw.shockwave().isActive()) {
            batcher.addBlackHoleShockwave(cx, cy, hw.shockwave());
        }
    }

    private static void drawDebugCollisionBatched(GameEntity e, BatchedDrawCollector batcher) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w = 32;
        double h = 32;
        if (e.hasModule(SpriteModule.class)) {
            w = e.getModule(SpriteModule.class).getSpriteFrameWidth() * e.getModule(SpriteModule.class).getVisualScale();
            h = e.getModule(SpriteModule.class).getSpriteFrameHeight() * e.getModule(SpriteModule.class).getVisualScale();
        }
        
        double angle = Math.toDegrees(e.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET);

        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof org.dyn4j.geometry.Circle) {
            double r = w / 2;
            batcher.addStrokedOval(cx - r, cy - r, w, h, Color.LIME, 1.5, angle);
        } else {
            batcher.addStrokedRect(cx - w/2, cy - h/2, w, h, Color.LIME, 1.5, angle);
        }
        batcher.addLine(cx, cy, cx + w/2, cy, 1.5, Color.RED, 1.0);
    }

    private static SpriteSheetsAnimator getAnimator(SpriteKey key, SpriteModule sprite, SpriteSheetsParser sheet) {
        return ANIMATOR_CACHE.computeIfAbsent(key, k -> {
            double defDur = sprite.getFrameDuration();
            if (sheet != null) {
                return new SpriteSheetsAnimator(defDur, sheet.getFramesPerRow());
            } else {
                return new SpriteSheetsAnimator(defDur, 1, 1);
            }
        });
    }

    private static SpriteSheetsParser getSheet(SpriteKey key, SpriteModule sprite) {
        return SHEET_CACHE.computeIfAbsent(key, k ->
                SpritesLibrary.getInstance().getSprite(k.path(), k.width(), k.height())
        );
    }
}
