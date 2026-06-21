package uni.gaben.iscat.universe.rendering;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.entities.*;
import uni.gaben.iscat.universe.entities.asteroids.AsteroidModel;
import uni.gaben.iscat.universe.entities.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;
import uni.gaben.iscat.universe.entities.interfaces.HasThrust;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.sprite.SpritesLibrary;
import uni.gaben.iscat.utils.sprite.SpriteSheetsParser;
import uni.gaben.iscat.utils.sprite.SpriteUtils;
import uni.gaben.iscat.utils.theme.ThemeManager;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Gestore accorpato del disegno e del loop di rendering stratificato (Entity Renderer).
 * Ottimizza le chiamate tramite batch grafici e applica trasformazioni e colorazioni dinamiche (tint) basate sui temi.
 */
public final class EntityRenderer {

    public record SpriteKey(String path, int width, int height) {}

    private static final Map<Class<?>, BiConsumer<AbstractPhysicalEntityModel, OptimizedLayeredRenderer>> LAYERED_RENDERERS = new HashMap<>();

    private static Color cachedAccentPrimary   = Color.WHITE;
    private static Color cachedAccentSecondary = Color.WHITE;
    private static Color cachedAccentTernary   = Color.GRAY;

    private static double currentZoom = 1.0;
    private static long lastFrameNano = -1;

    static {
        LAYERED_RENDERERS.put(AsteroidModel.class,   EntityRenderer::renderAsteroidBatched);
        LAYERED_RENDERERS.put(ProjectileModel.class, EntityRenderer::renderProjectileBatched);
        LAYERED_RENDERERS.put(PlayerModel.class,     EntityRenderer::renderPlayerBatched);
        LAYERED_RENDERERS.put(BlackHoleModel.class,  EntityRenderer::renderBlackHoleBatched);
    }

    private EntityRenderer() {}

    /** Configura i parametri globali per il frame corrente (es. livello di zoom dello screen). */
    public static void beginFrame(double zoom) {
        currentZoom = zoom;
        refreshFrameCache();
    }

    private static void refreshFrameCache() {
        long now = System.nanoTime();
        if (now - lastFrameNano < 1_000_000L) return;
        lastFrameNano = now;
        ThemeManager tm = ThemeManager.getInstance();
        cachedAccentPrimary   = tm.getAccentPrimary();
        cachedAccentSecondary = tm.getAccentSecondary();
        cachedAccentTernary   = tm.getAccentTertiary();
    }

    /**
     * Smista l'entità verso la sua routine di rendering specifica o al rendering standard degli sprite.
     */
    public static void renderLayered(AbstractPhysicalEntityModel entity, OptimizedLayeredRenderer layers, boolean debug) {
        if (entity == null || entity.shouldRemove()) return;
        refreshFrameCache();

        BiConsumer<AbstractPhysicalEntityModel, OptimizedLayeredRenderer> custom = LAYERED_RENDERERS.get(entity.getClass());
        if (custom == null && entity instanceof ProjectileModel) {
            custom = LAYERED_RENDERERS.get(ProjectileModel.class);
        }

        if (custom != null) {
            custom.accept(entity, layers);
        } else if (entity instanceof HasSprite sprite) {
            renderSpriteEntityBatched(entity, sprite, layers);
        }

        if (debug) renderDebugCollisionBatched(entity, layers);
    }

    /**
     * Elabora le animazioni da fogli di sprite (SpriteSheets) calcolando l'indice di riga e il frame corretto.
     */
    private static void renderSpriteEntityBatched(AbstractPhysicalEntityModel entity, HasSprite sprite, OptimizedLayeredRenderer layers) {
        double cx = UU.mToPx(entity.getTransform().getTranslationX());
        double cy = UU.mToPx(entity.getTransform().getTranslationY());
        double w  = entity.getWidthPx();
        double h  = entity.getHeightPx();

        double baseAngle = Math.toDegrees(sprite.canRotate() ? entity.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET : RenderingSettings.BASE_ROTRAD_OFFSET);
        double finalAngle = baseAngle + sprite.getVisualAngularOffsetDeg();

        SpriteSheetsParser sheet = SpritesLibrary.getInstance().getSprite(sprite.getSpritePath(), sprite.getSpriteFrameWidth(), sprite.getSpriteFrameHeight());
        if (sheet != null) {
            int row = entity.getState();
            int[] framesPerRow = sheet.getFramesPerRow();
            int currentFrame = 0;

            if (framesPerRow == null || row < 0 || row >= framesPerRow.length || framesPerRow[row] <= 0) {
                row = 0;
            }

            if (framesPerRow != null && row < framesPerRow.length) {
                int totalFrames = framesPerRow[row];
                if (totalFrames > 0) {
                    boolean isIdle = (row == 0) || (entity instanceof EntityModel em && em.getCurrentEntityState() == EntityState.IDLE);
                    int calculatedFrame = (int) (entity.getStateTime() / sprite.getFrameDuration());
                    currentFrame = isIdle ? calculatedFrame % totalFrames : Math.clamp(calculatedFrame, 0, totalFrames - 1);
                }
            }

            Image frame = sheet.getFrame(row, currentFrame);
            if (frame == null) frame = sheet.getFrame(0, 0);

            if (frame != null) {
                Color tint = (entity instanceof PlayerModel) ? cachedAccentPrimary : cachedAccentSecondary;
                layers.addSprite(SpriteUtils.tinted(frame, tint), cx, cy, w, h, finalAngle, null);
            }
        }

        if (entity instanceof HasShockwave sw && sw.shockwave().isActive()) {
            if ("iscat-master".equals(entity.getEntityRecord().entityKey())) {
                layers.addBlackHoleShockwave(cx, cy, sw.shockwave());
            } else {
                layers.addShockwave(cx, cy, sw.shockwave());
            }
        }

        if (entity instanceof Alterable ld) {
            if ((w * currentZoom) >= 6.0) {
                double barX = cx - w / 2;
                double barY = cy - h / 2 - OptimizedLayeredRenderer.HpBarBatch.HP_BAR_OFFSET_Y;
                layers.addHpBar(barX, barY, w, OptimizedLayeredRenderer.HpBarBatch.HP_BAR_HEIGHT, ld.getEndurance() / ld.getMaxEndurance());

                if (entity instanceof PlayerModel pm) {
                    double tgY = barY + OptimizedLayeredRenderer.HpBarBatch.HP_BAR_HEIGHT + OptimizedLayeredRenderer.TimeGaugeBarBatch.TIME_BAR_OFFSET_Y;
                    layers.addTimeGaugeBar(barX, tgY, w, OptimizedLayeredRenderer.TimeGaugeBarBatch.TIME_BAR_HEIGHT, pm.getTimeGauge() / pm.getMaxTimeGauge());
                }
            }
        }

        if (entity.getTemporaryVelocity() != -1 || entity instanceof PlayerModel pm && pm.isDashing()) {
            double dcx = UU.mToPx(entity.getTransform().getTranslationX());
            double dcy = UU.mToPx(entity.getTransform().getTranslationY());
            Vector2 dVel = entity.getLinearVelocity();
            double dw = entity.getWidthPx();
            double dh = entity.getHeightPx();
            layers.addDash(dcx, dcy, dVel.x, dVel.y, dw, dh);
        }

    }

    private static void renderProjectileBatched(AbstractPhysicalEntityModel e, OptimizedLayeredRenderer layers) {
        ProjectileModel p = (ProjectileModel) e;
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w  = e.getWidthPx();
        double h  = e.getHeightPx();

        double screenSize = w * currentZoom;
        if (screenSize < 1.5) return;

        Vector2 vel = p.getLinearVelocity();
        double trailX2 = cx, trailY2 = cy;
        double trailWidth = ScalareAureo.phiMinore(h);

        if (vel.getMagnitudeSquared() > 0.01) {
            trailX2 = cx - ScalareAureo.phiMaggiore(vel.x);
            trailY2 = cy - ScalareAureo.phiMaggiore(vel.y);
        }

        Color baseColor = p.getType().color;
        if (screenSize < 4.0) {
            layers.addProjectile(cx, cy, w, h, baseColor, cx, cy, trailX2, trailY2, trailWidth);
        } else {
            layers.addProjectile(cx, cy, ScalareAureo.phiMaggiore(w), ScalareAureo.phiMaggiore(h), baseColor.darker(), cx, cy, trailX2, trailY2, ScalareAureo.phiMaggiore(trailWidth));
            layers.addProjectile(cx, cy, w, h, baseColor, cx, cy, trailX2, trailY2, trailWidth);
        }
    }

    /**
     * Renderizza la struttura poligonale degli asteroidi e disegna le crepe di danneggiamento dinamiche.
     */
    private static void renderAsteroidBatched(AbstractPhysicalEntityModel e, OptimizedLayeredRenderer batcher) {
        AsteroidModel asteroid = (AsteroidModel) e;
        double screenSize = asteroid.getWidthPx() * currentZoom;
        if (screenSize < 8.0) return;

        double cx = UU.mToPx(asteroid.getTransform().getTranslationX());
        double cy = UU.mToPx(asteroid.getTransform().getTranslationY());

        if (screenSize < 20.0) {
            batcher.addFilledOval(cx - asteroid.getWidthPx()/2.0, cy - asteroid.getHeightPx()/2.0, asteroid.getWidthPx(), asteroid.getHeightPx(), cachedAccentTernary, 1.0);
            return;
        }

        double[] xPoints = asteroid.getCachedXPoints();
        double[] yPoints = asteroid.getCachedYPoints();
        int len = xPoints.length;

        batcher.addFilledPolygon(xPoints, yPoints, cachedAccentTernary);
        batcher.addStrokedPolygon(xPoints, yPoints, cachedAccentPrimary, 2.0);

        double healthRatio = asteroid.getDurabilityHealthRatio();
        if (healthRatio < 0.85 && screenSize >= 30.0) {
            double crackWidth = Math.max(2, (1.0 - healthRatio) * 4.0);
            int startIndex = getStartIndex(asteroid, asteroid.getDisplayVertices());
            int endIndex = (startIndex + len / 2) % len;

            double staticSeed = asteroid.hashCode();
            double jitterMag = (1.0 - healthRatio) * (asteroid.getSize() / 7.0);
            double midX = (xPoints[startIndex] + xPoints[endIndex]) / 2.0 + Math.sin(staticSeed) * jitterMag;
            double midY = (yPoints[startIndex] + yPoints[endIndex]) / 2.0 + Math.cos(staticSeed) * jitterMag;

            batcher.addLine(xPoints[startIndex], yPoints[startIndex], midX, midY, crackWidth, cachedAccentPrimary, 1.0);
            batcher.addLine(midX, midY, xPoints[endIndex], yPoints[endIndex], crackWidth, cachedAccentPrimary, 1.0);

            if (healthRatio < 0.5 && screenSize >= 50.0) {
                double subWidth = crackWidth * 0.65;
                int branchIndex1 = (startIndex + len / 4) % len;
                double bMidX1 = (midX + xPoints[branchIndex1]) / 2.0 + Math.sin(staticSeed + 1) * (jitterMag * 0.4);
                double bMidY1 = (midY + yPoints[branchIndex1]) / 2.0 + Math.cos(staticSeed + 1) * (jitterMag * 0.4);
                batcher.addLine(midX, midY, bMidX1, bMidY1, subWidth, cachedAccentPrimary, 1.0);
                batcher.addLine(bMidX1, bMidY1, xPoints[branchIndex1], yPoints[branchIndex1], subWidth, cachedAccentPrimary, 1.0);

                int branchIndex2 = (startIndex + (3 * len) / 4) % len;
                double bMidX2 = (midX + xPoints[branchIndex2]) / 2.0 + Math.sin(staticSeed + 2) * (jitterMag * 0.4);
                double bMidY2 = (midY + yPoints[branchIndex2]) / 2.0 + Math.cos(staticSeed + 2) * (jitterMag * 0.4);
                batcher.addLine(midX, midY, bMidX2, bMidY2, subWidth, cachedAccentPrimary, 1.0);
                batcher.addLine(bMidX2, bMidY2, xPoints[branchIndex2], yPoints[branchIndex2], subWidth, cachedAccentPrimary, 1.0);
            }
        }
    }

    private static void renderPlayerBatched(AbstractPhysicalEntityModel e, OptimizedLayeredRenderer batcher) {
        PlayerModel player = (PlayerModel) e;
        if (player instanceof HasSprite sprite) renderSpriteEntityBatched(player, sprite, batcher);

        if (player instanceof HasThrust thrustProvider && thrustProvider.thrust().isActive()) {
            double cx = UU.mToPx(player.getTransform().getTranslationX());
            double cy = UU.mToPx(player.getTransform().getTranslationY());
            double angle = Math.toDegrees(player.getTransform().getRotationAngle()) + RenderingSettings.BASE_ROTDEG_OFFSET + player.getVisualAngularOffsetDeg();
            batcher.addThrust(cx, cy, angle, thrustProvider.thrust());
        }
    }

    private static void renderBlackHoleBatched(AbstractPhysicalEntityModel e, OptimizedLayeredRenderer batcher) {
        BlackHoleModel bh = (BlackHoleModel) e;
        if (bh.getWidthPx() * currentZoom < 8.0) return;

        if (bh.shockwave().isActive()) {
            batcher.addBlackHoleShockwave(UU.mToPx(bh.getTransform().getTranslationX()), UU.mToPx(bh.getTransform().getTranslationY()), bh.shockwave());
        }
    }

    private static void renderDebugCollisionBatched(AbstractPhysicalEntityModel e, OptimizedLayeredRenderer renderer) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w  = e.getWidthPx();
        double h  = e.getHeightPx();
        double angle = Math.toDegrees(e.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET);

        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof Circle) {
            renderer.addStrokedOval(cx - (w / 2), cy - (w / 2), w, h, Color.LIME);
        } else {
            renderer.addStrokedRect(cx - w / 2, cy - h / 2, w, h, Color.LIME, angle);
        }
        renderer.addLine(cx, cy, cx + w / 2, cy, 1.5, Color.RED, 1.0);
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