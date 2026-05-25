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

/**
 * Pipeline di rendering astratta e standardizzata per tutte le entità di gioco.
 * Gestisce l'isolamento della matrice del Canvas e calcola gli spazi locali.
 *
 * @param <M> Tipo specifico di modello fisico associato a questa vista.
 */
public abstract class AbstractEntityView<M extends AbstractEntityModel> {
    protected double cx, cy, w, h, rotDeg, rotRad;

    /** Fattore di scala visiva indipendente dall'hitbox fisico. Default = 1.0 */
    protected double spriteScale = 1.0;

    /** Dimensioni dello sprite calcolate: hitbox × spriteScale */
    protected double sw, sh;

    /**
     * Sincronizza la posizione globale dello schermo e le metriche delle
     * dimensioni direttamente estratti dai contorni fisici reali dell'entità.
     */
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

    /**
     * IL MOTORE DEL PIPELINE STANDARD (Template Method)
     * Isola lo stack delle trasformazioni, calcola il punto di rotazione al centro,
     * e richiama il disegno specifico in coordinate locali (-w/2, -h/2).
     */
    public final void setupGraphicsContextAndDrawContent(M entity, GraphicsContext gc, double assetAngularOffsetDeg) {
        setPos(entity);
        setAngle(entity);

        gc.save();

        if(entity instanceof Projectile p) {
            gc.setFill(p.getType().color);
        }
        // Trasla l'origine del Canvas esattamente al centro dell'entità
        gc.translate(cx, cy);
        gc.rotate(rotDeg + assetAngularOffsetDeg);

        // Delega l'esecuzione del disegno interno alle classi derivate.
        // In drawContent(), (0,0) corrisponde perfettamente al centro dell'entità!
        drawContent(entity, gc, -sw / 2, -sh / 2, sw, sh);

        gc.restore();
    }

    /**
     * Metodo astratto implementato dalle singole visual views per gestire i propri contenuti grafici.
     */
    protected abstract void drawContent(M entity, GraphicsContext gc, double x, double y, double width, double height);

    protected void drawHpBar(LifeDeath entity, GraphicsContext gc) {
        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w, PlayerSettings.HP_BAR_HEIGHT);
        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w * (entity.getLife() / entity.getMaxLife()), PlayerSettings.HP_BAR_HEIGHT);
    }

    /**
     * Visualizzatore Debug delle Collisioni geometriche basato sulle coordinate attuali.
     */
    public void drawDebugCollision(M e, GraphicsContext gc) {
        setPos(e);
        setAngle(e);

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(rotDeg);

        gc.setStroke(Color.LIME);
        gc.setLineWidth(1.5);

        if (e.getFixtureCount() > 0
                && e.getFixture(0).getShape() instanceof Circle) {
            double radiusPx = w / 2;
            gc.strokeOval(-radiusPx, -radiusPx, w, h);
        } else {
            gc.strokeRect(-w / 2, -h / 2, w, h);
        }

        gc.setStroke(Color.RED);
        gc.strokeLine(0, 0, w / 2, 0);

        gc.restore();
    }
}