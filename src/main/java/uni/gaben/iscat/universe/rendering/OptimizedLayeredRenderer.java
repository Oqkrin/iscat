package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.universe.camera.CameraModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OptimizedLayeredRenderer {

    private record SpriteBatch(Image image, double x, double y, double w, double h, double angle, Color tint) {}
    private record LineBatch(double x1, double y1, double x2, double y2, double lineWidth, Color color, double alpha) {}
    private record OvalBatch(double x, double y, double w, double h, Color color, double alpha, boolean fill, double lineWidth) {}    private record RectBatch(double x, double y, double w, double h, Color color, double alpha, boolean fill, double angle) {}
    private record PolygonBatch(double[] xPoints, double[] yPoints, Color color, double alpha, boolean fill, double lineWidth) {}
    record HpBarBatch(double x, double y, double w, double h, double percent) {
        // === Visuals & Dimensions ===
        public static final double HP_BAR_OFFSET_Y = 10.0;
        public static final double HP_BAR_HEIGHT = 4.0;
    }
    record TimeGaugeBarBatch(double x, double y, double w, double h, double percent) {
        // === Visuals & Dimensions ===
        public static final double TIME_BAR_OFFSET_Y = 6.0; // below HP bar
        public static final double TIME_BAR_HEIGHT = 2.0;
    }
    private record ThrustBatch(double cx, double cy, double angle, Thrust thrust) {}
    private record ShockwaveBatch(double cx, double cy, Shockwave shockwave, boolean isBlackHole) {}
    record ProjectileBatch(
            double cx, double cy, double w, double h, Color color,
            double trailX1, double trailY1, double trailX2, double trailY2,
            double trailWidth
    ) {}

    private static final Effect PROJECTILE_EFFECT = new GaussianBlur();

    private final List<ProjectileBatch> projectiles = new ArrayList<>();

    private final List<SpriteBatch> sprites = new ArrayList<>();
    private final List<LineBatch> lines = new ArrayList<>();
    private final List<OvalBatch> ovals = new ArrayList<>();
    private final List<RectBatch> rects = new ArrayList<>();
    private final List<PolygonBatch> polygons = new ArrayList<>();
    private final List<HpBarBatch> hpBars = new ArrayList<>();
    private final List<TimeGaugeBarBatch> timeGaugeBars = new ArrayList<>();
    private final List<ThrustBatch> thrusts = new ArrayList<>();
    private final List<ShockwaveBatch> shockwaves = new ArrayList<>();

    private GraphicsContext gc;
    private CameraModel camera;
    private double screenWidth, screenHeight;
    private double zoom, camX, camY;

    private final Bloom spriteBloom = new Bloom(0.3);

    public void begin(GraphicsContext gc, CameraModel camera, double screenWidth, double screenHeight) {
        this.gc = gc;
        this.camera = camera;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.zoom = camera.getZoom();
        this.camX = camera.getX();
        this.camY = camera.getY();

        sprites.clear();
        lines.clear();
        ovals.clear();
        rects.clear();
        polygons.clear();
        hpBars.clear();
        timeGaugeBars.clear();
        thrusts.clear();
        shockwaves.clear();
        projectiles.clear();
    }

    public void render() {
        gc.save();
        gc.translate(screenWidth / 2 - camX * zoom, screenHeight / 2 - camY * zoom);
        gc.scale(zoom, zoom);

        renderSprites();
        renderPolygons();
        renderLines();
        renderOvals();
        renderRect();
        renderHPbars();
        renderTimeGaugeBars();
        renderShockwaves();
        renderThrusts();
        renderProjectiles();

        gc.restore();
        gc.setGlobalAlpha(1.0);
    }

    private void renderProjectiles() {
        gc.setEffect(PROJECTILE_EFFECT);
        gc.setLineCap(StrokeLineCap.ROUND);                     // soft trail ends
        for (ProjectileBatch p : projectiles) {
            DrawVFX.drawProjectile(gc, p);
        }
        gc.setEffect(null);
        gc.setLineCap(StrokeLineCap.SQUARE);   // restore default
        gc.setGlobalAlpha(1.0);
    }

    private void renderThrusts() {
        for (ThrustBatch t : thrusts) {
            DrawVFX.drawThrustRaw(gc, t.cx, t.cy, t.angle, t.thrust);
        }
        gc.setEffect(null);                     // <<< CLEAR EFFECT
        gc.setGlobalBlendMode(BlendMode.SRC_OVER); // <<< RESET BLEND MODE
    }

    private void renderShockwaves() {
        for (ShockwaveBatch sw : shockwaves) {
            if (sw.isBlackHole) DrawVFX.drawBlackHoleRaw(gc, sw.cx, sw.cy, sw.shockwave);
            else DrawVFX.drawShockwaveRaw(gc, sw.cx, sw.cy, sw.shockwave);
        }
        gc.setGlobalAlpha(1.0);
    }

    private void renderHPbars() {
        for (HpBarBatch h : hpBars) {
            DrawVFX.drawHpBar(gc, h);
        }
    }

    private void renderTimeGaugeBars() {
        for (TimeGaugeBarBatch t : timeGaugeBars) {
            DrawVFX.drawTimeGaugeBar(gc, t);
        }
    }

    private void renderRect() {
        // Rects (some may be rotated)
        for (RectBatch r : rects) {
            if (r.angle != 0) {
                drawRotatedRect(r.x, r.y, r.w, r.h, r.angle, r.fill, r.color, r.alpha);
            } else {
                if (r.fill) {
                    gc.setFill(r.color);
                    gc.fillRect(r.x, r.y, r.w, r.h);
                } else {
                    gc.setStroke(r.color);
                    gc.strokeRect(r.x, r.y, r.w, r.h);
                }
            }
        }
    }

    private void drawRotatedRect(double x, double y, double w, double h, double angle, boolean fill, Color color, double alpha) {
        gc.save();
        gc.translate(x + w/2, y + h/2);
        gc.rotate(angle);
        gc.setGlobalAlpha(alpha);
        if (fill) {
            gc.setFill(color);
            gc.fillRect(-w/2, -h/2, w, h);
        } else {
            gc.setStroke(color);
            gc.strokeRect(-w/2, -h/2, w, h);
        }
        gc.restore();
    }

    private void renderOvals() {
        for (OvalBatch o : ovals) {
            if (o.fill) {
                gc.setFill(o.color);
                gc.fillOval(o.x, o.y, o.w, o.h);
            } else {
                gc.setStroke(o.color);
                gc.setLineWidth(o.lineWidth);
                gc.strokeOval(o.x, o.y, o.w, o.h);
            }
        }
    }

    private void renderLines() {
        // Lines
        for (LineBatch l : lines) {
            gc.setStroke(l.color);
            gc.setLineWidth(l.lineWidth);
            gc.setGlobalAlpha(l.alpha);
            gc.strokeLine(l.x1, l.y1, l.x2, l.y2);
        }
    }

    private void renderPolygons() {
        for (PolygonBatch p : polygons) {
            if (p.fill) {
                gc.setFill(p.color);
                gc.fillPolygon(p.xPoints, p.yPoints, p.xPoints.length);
            }
            if (p.lineWidth > 0) {
                gc.setStroke(p.color);
                gc.setLineWidth(p.lineWidth);
                gc.strokePolygon(p.xPoints, p.yPoints, p.xPoints.length);
            }
        }

    }

    private void renderSprites() {
        sprites.sort(Comparator.comparingInt(a -> a.image().hashCode()));
        for (SpriteBatch s : sprites) {
            gc.setEffect(spriteBloom);
            drawTransformedImage(s.image, s.x, s.y, s.w, s.h, s.angle, s.tint);
            gc.setEffect(null);
        }
    }

    private void drawTransformedImage(Image img, double x, double y, double w, double h, double angle, Color tint) {
        gc.save();
        gc.translate(x, y);
        gc.rotate(angle);
        if (tint != null) {
            gc.drawImage(img, -w/2, -h/2, w, h);
            gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.MULTIPLY);
            gc.setFill(tint);
            gc.fillRect(-w/2, -h/2, w, h);
            gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        } else {
            gc.drawImage(img, -w/2, -h/2, w, h);
        }
        gc.restore();
    }


    public void addSprite(Image image, double x, double y, double w, double h, double angle, Color tint) {
        sprites.add(new SpriteBatch(image, x, y, w, h, angle, tint));
    }

    public void addFilledPolygon(double[] xPoints, double[] yPoints, Color color) {
        polygons.add(new PolygonBatch(xPoints.clone(), yPoints.clone(), color, 1.0, true, 0));
    }

    public void addStrokedPolygon(double[] xPoints, double[] yPoints, Color color, double lineWidth) {
        polygons.add(new PolygonBatch(xPoints.clone(), yPoints.clone(), color, 1.0, false, lineWidth));
    }

    public void addLine(double x1, double y1, double x2, double y2, double lineWidth, Color color, double alpha) {
        lines.add(new LineBatch(x1, y1, x2, y2, lineWidth, color, alpha));
    }

    public void addThickLine(double x1, double y1, double x2, double y2, double width, Color color, double alpha) {
        addLine(x1, y1, x2, y2, width, color, alpha);
    }

    public void addFilledOval(double x, double y, double w, double h, Color color, double alpha) {
        ovals.add(new OvalBatch(x, y, w, h, color, alpha, true, 1.0));
    }

    public void addFilledRotatedRect(double x, double y, double w, double h, double angle, Color color, double alpha) {
        rects.add(new RectBatch(x, y, w, h, color, alpha, true, angle));
    }

    public void addStrokedOval(double x, double y, double w, double h, Color color) {
        ovals.add(new OvalBatch(x, y, w, h, color, 1.0, false, 1.0));
    }
    public void addStrokedOval(double x, double y, double w, double h, Color color, double lineWidth) {
        ovals.add(new OvalBatch(x, y, w, h, color, 1.0, false, lineWidth));
    }
    public void addStrokedRect(double x, double y, double w, double h, Color color, double angle) {
        rects.add(new RectBatch(x, y, w, h, color, 1.0, false, angle));
    }

    public void addHpBar(double x, double y, double w, double h, double percent) {
        hpBars.add(new HpBarBatch(x, y, w, h, percent));
    }

    public void addTimeGaugeBar(double x, double y, double w, double h, double percent) {
        timeGaugeBars.add(new TimeGaugeBarBatch(x, y, w, h, percent));
    }

    public void addThrust(double cx, double cy, double angle, Thrust thrust) {
        thrusts.add(new ThrustBatch(cx, cy, angle, thrust));
    }

    public void addShockwave(double cx, double cy, Shockwave shockwave) {
        shockwaves.add(new ShockwaveBatch(cx, cy, shockwave, false));
    }

    public void addBlackHoleShockwave(double cx, double cy, Shockwave shockwave) {
        shockwaves.add(new ShockwaveBatch(cx, cy, shockwave, true));
    }

    public void addProjectile(double cx, double cy, double w, double h, Color color,
                              double trailX1, double trailY1, double trailX2, double trailY2,
                              double trailWidth) {
        projectiles.add(new ProjectileBatch(cx, cy, w, h, color,
                trailX1, trailY1, trailX2, trailY2, trailWidth));
    }
}