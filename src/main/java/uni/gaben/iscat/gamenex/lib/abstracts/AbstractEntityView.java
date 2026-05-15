package uni.gaben.iscat.gamenex.lib.abstracts;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerSettings;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Lifecycle;
import uni.gaben.iscat.gamenex.view.ViewSettings;
import uni.gaben.iscat.utils.ThemeColors;

/**
 * Classe base per la visualizzazione grafica (View) delle entità.
 * Fornisce utilità per trasformare le coordinate fisiche (metri) in coordinate
 * grafiche (pixel) pronte per il rendering su {@link GraphicsContext}.
 */
public class AbstractEntityView  {
    /** Posizione X centrale calcolata in pixel. */
    protected double cx;
    /** Posizione Y centrale calcolata in pixel. */
    protected double cy;
    /** Larghezza dello sprite in pixel. */
    protected double w;
    /** Altezza dello sprite in pixel. */
    protected double h;
    /** Rotazione attuale in gradi. */
    protected double rotDeg;
    /** Rotazione attuale in radianti. */
    protected double rotRad;

    /**
     * Sincronizza la posizione della View con quella del Modello fisico.
     * Converte i metri del motore fisico in pixel per lo schermo.
     * @param e Il modello fisico da seguire.
     */
    public void setPos(AbstractEntityModel e) {
        cx = e.getTransform().getTranslationX() * UniverseSettings.SCALE;
        cy = e.getTransform().getTranslationY() * UniverseSettings.SCALE;
    }

    public void setW(Double w) {
        this.w  = w;
    }

    public void setH(Double h) {
        this.h  = h;
    }

    /**
     * Imposta le dimensioni quadrate dello sprite.
     */
    public void setSize(double size) {
        setW(size);
        setH(size);
    }

    /**
     * Calcola l'angolo di rotazione basandosi sul modello fisico.
     * Applica l'offset di rotazione base definito nelle impostazioni della View.
     */
    protected void setAngle(AbstractEntityModel e) {
        rotRad = e.getTransform().getRotationAngle() + ViewSettings.BASE_ROTRAD_OFFSET ;
        rotDeg = Math.toDegrees(rotRad);
    }

    /**
     * Disegna una barra della salute sopra l'entità.
     * @param entity L'entità vivente (che possiede dati sulla vita).
     * @param gc Il contesto grafico su cui disegnare.
     */
    protected void drawHpBar(Lifecycle entity, GraphicsContext gc) {
        gc.setFill(ThemeColors.getColorError());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w, PlayerSettings.HP_BAR_HEIGHT);
        gc.setFill(ThemeColors.getColorSuccess());
        gc.fillRect(cx - w / 2, cy - h / 2 - PlayerSettings.HP_BAR_OFFSET_Y, w * (entity.getLife() / entity.getMaxLife()), PlayerSettings.HP_BAR_HEIGHT);
    }

}
