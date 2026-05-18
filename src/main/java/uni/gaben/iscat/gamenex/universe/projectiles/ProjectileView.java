package uni.gaben.iscat.gamenex.universe.projectiles;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.utils.ThemeColors;

/**
 * Vista standardizzata per il Proiettile.
 * Risolve i problemi di centratura e scala dimensionale ereditando dalla pipeline centrale.
 */
public class ProjectileView extends AbstractEntityView<Projectile> implements Drawable<Projectile> {

    public ProjectileView() {
        spriteScale = 1.0;
    }
    @Override
    public void draw(Projectile p, GraphicsContext gc) {
        // Carica i colori prima dell'esecuzione del loop
        ThemeColors.ensureLoaded();

        // Passa il modello alla pipeline principale.
        // L'angolo dell'asset di default è 0.0.
        setupGraphicsContextAndDrawContent(p, gc, 0.0);
    }

    @Override
    protected void drawContent(Projectile p, GraphicsContext gc, double x, double y, double width, double height) {
        // Applica il colore dell'accento globale impostato nel tema
        gc.setFill(ThemeColors.getAccentPrimary());

        // IL FIX: x e y arrivano precalcolate come (-width/2, -height/2).
        // Disegnando qui, il cerchio grafico si allinea millimetricamente
        // alla fixture fisica di Dyn4j invece di sporgere dall'angolo (0,0).
        gc.fillOval(x, y, width, height);
    }
}