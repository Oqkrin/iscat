package uni.gaben.iscat.gamenex.lib.abstracts;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Lifecycle;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.player.PlayerSettings;
import uni.gaben.iscat.gamenex.view.ViewSettings;
import uni.gaben.iscat.utils.ThemeColors;

/**
 * Pipeline di rendering astratta e standardizzata per tutte le entità di gioco.
 * Gestisce l'isolamento della matrice del Canvas e calcola gli spazi locali.
 *
 * @param <M> Tipo specifico di modello fisico associato a questa vista.
 */
public abstract class AbstractEntityView<M extends AbstractEntityModel> {
    protected double cx, cy, w, h, rotDeg, rotRad;

    /**
     * Sincronizza la posizione globale dello schermo e le metriche delle
     * dimensioni direttamente estratti dai contorni fisici reali dell'entità.
     */
    public void setPos(M e) {
        cx = UU.mToPx(e.getTransform().getTranslationX());
        cy = UU.mToPx(e.getTransform().getTranslationY());
        w = e.getWidthPx();
        h = e.getHeightPx();
    }

    protected void setAngle(M e) {
        rotRad = e.getTransform().getRotationAngle() + ViewSettings.BASE_ROTRAD_OFFSET;
        rotDeg = Math.toDegrees(rotRad);
    }

    /**
     * IL MOTORE DEL PIPELINE STANDARD (Template Method)
     * Isola lo stack delle trasformazioni, calcola il punto di rotazione al centro,
     * e richiama il disegno specifico in coordinate locali (-w/2, -h/2).
     */
    public final void renderEntity(M entity, GraphicsContext gc, double assetAngularOffsetDeg) {
        setPos(entity);
        setAngle(entity);

        gc.save();

        // Trasla l'origine del Canvas esattamente al centro dell'entità
        gc.translate(cx, cy);
        gc.rotate(rotDeg + assetAngularOffsetDeg);

        // Delega l'esecuzione del disegno interno alle classi derivate.
        // In drawContent(), (0,0) corrisponde perfettamente al centro dell'entità!
        drawContent(entity, gc, -w / 2, -h / 2, w, h);

        gc.restore();
    }

    /**
     * Metodo astratto implementato dalle singole visual views per gestire i propri contenuti grafici.
     */
    protected abstract void drawContent(M entity, GraphicsContext gc, double x, double y, double width, double height);

    protected void drawHpBar(Lifecycle entity, GraphicsContext gc) {
        gc.setFill(ThemeColors.getColorError());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w, PlayerSettings.HP_BAR_HEIGHT);
        gc.setFill(ThemeColors.getColorSuccess());
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

        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof org.dyn4j.geometry.Circle) {
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