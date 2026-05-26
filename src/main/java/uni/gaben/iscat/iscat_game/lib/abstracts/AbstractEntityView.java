package uni.gaben.iscat.iscat_game.lib.abstracts;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Circle;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.player.PlayerSettings;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.utils.DrawSettings;
import uni.gaben.iscat.utils.ThemeManager;
import java.util.Random;

public abstract class AbstractEntityView<M extends AbstractEntityModel> {
    protected double cx, cy, w, h, rotDeg, rotRad;
    protected double spriteScale = 1.0;
    protected double sw, sh;

    // Shockwave stuff
    protected boolean shockwaveActive = false;
    protected double shockwaveRadius = 0.0;
    protected double shockwaveAlpha = 1.0;
    protected double shakeIntensity = 0.0;
    private double shockwaveTimer = 0.0;
    protected double shockwaveDuration;
    protected double shockwaveMaxRadius;
    protected double shockwaveLineWidth;
    protected double maxShakeIntensity;
    protected boolean shakeEnabled;

    protected final Random random = new Random();

    public void setPos(M e) {
        cx = UU.mToPx(e.getTransform().getTranslationX());
        cy = UU.mToPx(e.getTransform().getTranslationY());
        w = e.getWidthPx();
        h = e.getHeightPx();
        sw = w;
        sh = h;
    }

    protected void setAngle(M e) {
        rotRad = e.getTransform().getRotationAngle() + DrawSettings.BASE_ROTRAD_OFFSET;
        rotDeg = Math.toDegrees(rotRad);
    }

    public final void setupGraphicsContextAndDrawContent(M entity, GraphicsContext gc, double assetAngularOffsetDeg) {
        setPos(entity);
        setAngle(entity);

        gc.save();

        // Applica lo shake all'intero contesto grafico di questa entità (se attivo)
        applyScreenShake(gc);

        if(entity instanceof Projectile p) {
            gc.setFill(p.getType().color);
        }

        // Traslazione e rotazione standard dello sprite
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg + assetAngularOffsetDeg);
        drawContent(entity, gc, -sw / 2, -sh / 2, sw, sh);
        gc.restore();

        // Disegna la barra della vita (se attiva)
        if (entity instanceof LifeDeath ld) {
            drawHpBar(ld, gc);
        }

        // Aggiorna e disegna l'anello dello shockwave (se attivo)
        updateAndDrawShockwave(entity, gc);

        gc.restore();
    }

    protected abstract void drawContent(M entity, GraphicsContext gc, double x, double y, double width, double height);

    /**
     * Chiamata di una Shockwave. Nota: per usare questo metodo devi chiamarlo in un eventa, tipo nemico che muore o usa un attacco
     */
    public void triggerShockwave(double duration, double maxRadius, double lineWidth, double maxShake, boolean enableShake) {
        this.shockwaveActive = true;
        this.shockwaveTimer = 0.0;
        this.shockwaveDuration = duration;
        this.shockwaveMaxRadius = maxRadius;
        this.shockwaveLineWidth = lineWidth;
        this.maxShakeIntensity = maxShake;
        this.shakeEnabled = enableShake;

        this.shockwaveAlpha = 1.0;
        this.shockwaveRadius = 0.0;
        this.shakeIntensity = enableShake ? maxShake : 0.0;
    }

    /**
     * Chiamata veloce di una Shockwave. Nota: per usare questo metodo devi chiamarlo in un eventa, tipo nemico che muore o usa un attacco
     */
    public void triggerShockwave(double duration, boolean enableShake) {
        triggerShockwave(duration, 2500.0, 15.0, 24.0, enableShake);
    }

    /**
     * Applica un effetto Shake al nemico
     */
    private void applyScreenShake(GraphicsContext gc) {
        if (shockwaveActive && shakeEnabled && shakeIntensity > 0.1) {
            double shakeX = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            double shakeY = (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            gc.translate(shakeX, shakeY);
        }
    }

    /**
     * Metodo che disegna la Shockwave
     */
    private void updateAndDrawShockwave(AbstractEntityModel entity, GraphicsContext gc) {
        if (!shockwaveActive) return;

        shockwaveTimer += UU.UNIVERSE_TICK;
        double progress = shockwaveTimer / shockwaveDuration;

        if (progress >= 1.0) {
            shockwaveActive = false;
            shakeIntensity = 0.0;
            return;
        }

        shockwaveRadius = progress * shockwaveMaxRadius;
        shockwaveAlpha = Math.max(0.0, 1.0 - progress);
        shakeIntensity = maxShakeIntensity * (1.0 - progress);

        gc.save();
        double centerX = UU.mToPx(entity.getTransform().getTranslationX());
        double centerY = UU.mToPx(entity.getTransform().getTranslationY());
        double diameter = shockwaveRadius * 2;
        double topLeftX = centerX - shockwaveRadius;
        double topLeftY = centerY - shockwaveRadius;

        double fillAlpha = shockwaveAlpha * 0.15;
        gc.setFill(Color.rgb(255, 255, 255, fillAlpha));
        gc.fillOval(topLeftX, topLeftY, diameter, diameter);

        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.3));
        gc.setLineWidth(shockwaveLineWidth * 3.5);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha * 0.6));
        gc.setLineWidth(shockwaveLineWidth * 1.8);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);

        gc.setStroke(Color.rgb(255, 255, 255, shockwaveAlpha));
        gc.setLineWidth(shockwaveLineWidth);
        gc.strokeOval(topLeftX, topLeftY, diameter, diameter);
        gc.restore();
    }

    protected void drawHpBar(LifeDeath entity, GraphicsContext gc) {
        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w, PlayerSettings.HP_BAR_HEIGHT);
        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w * (entity.getLife() / entity.getMaxLife()), PlayerSettings.HP_BAR_HEIGHT);
    }

    public void drawDebugCollision(M e, GraphicsContext gc) {
        setPos(e); setAngle(e);
        gc.save(); gc.translate(cx, cy); gc.rotate(rotDeg);
        gc.setStroke(Color.LIME); gc.setLineWidth(1.5);
        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof Circle) {
            double radiusPx = w / 2; gc.strokeOval(-radiusPx, -radiusPx, w, h);
        } else {
            gc.strokeRect(-w / 2, -h / 2, w, h);
        }
        gc.setStroke(Color.RED); gc.strokeLine(0, 0, w / 2, 0);
        gc.restore();
    }
}