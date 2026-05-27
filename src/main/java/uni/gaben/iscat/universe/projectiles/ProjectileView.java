package uni.gaben.iscat.universe.projectiles;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.utils.theme.ThemeManager;

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
        // Passa il modello alla pipeline principale.
        // L'angolo dell'asset di default è 0.0.
        setupGraphicsContextAndDrawContent(p, gc, 0.0);
    }

    @Override
    protected void drawContent(Projectile p, GraphicsContext gc, double x, double y, double width, double height) {
        // IL FIX: x e y arrivano precalcolate come (-width/2, -height/2).
        // Disegnando qui, il cerchio grafico si allinea millimetricamente
        // alla fixture fisica di Dyn4j invece di sporgere dall'angolo (0,0).
        Color bulletColor = ThemeManager.getInstance().getAccentTernary();
        gc.setFill(bulletColor);
        gc.fillOval(x, y, width, height);
    }
}