package uni.gaben.iscat.gamenex.universe.projectiles;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.player.PlayerSettings;
import uni.gaben.iscat.utils.ThemeColors;

/**
 * Disegna un proiettile: cerchio bianco con bagliore semitrasparente.
 */
public class ProjectileView extends AbstractEntityView implements Drawable<Projectile> {


    @Override
    public void draw(Projectile p,  GraphicsContext gc) {
        gc.save();
        setPos(p);
        gc.translate(cx, cy);

        setAngle(p);
        gc.rotate(rotDeg);

        setSize(32);

        ThemeColors.ensureLoaded();
        gc.setFill(ThemeColors.getAccentPrimary());
        gc.fillOval(0, 0, w, h);

        gc.restore();
    }
}
