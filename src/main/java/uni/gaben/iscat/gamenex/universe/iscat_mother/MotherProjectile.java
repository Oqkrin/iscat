package uni.gaben.iscat.gamenex.universe.iscat_mother;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.components.entities.player.projectile.ProjectileModel;
import uni.gaben.iscat.game.utils.interfaces.Drawable;
import uni.gaben.iscat.game.utils.interfaces.HasRenderer;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.utils.ThemeManager;
/*
public class MotherProjectile extends AbstractProjectileModel {

    public MotherProjectile(Vec2 pos, Vec2 vel) {
        super(pos, vel);
        this.mass = 1.0;
    }

    @Override
    public double getCollisionRadius() { return DIM_PROIETTILE / 2.0; }

    @Override
    public int getCollisionMask() { return LAYER_PLAYER; }

    @Override
    public Drawable<ProjectileModel> getRenderer() {
        return (gc, p) -> {
            double r = DIM_PROIETTILE / 2.0;
            Color tint = ThemeManager.getInstance().globalTintProperty().get();

            gc.save();
            gc.setFill(tint);                          // era ORANGERED
            gc.fillOval(p.getX() - r, p.getY() - r, r * 2, r * 2);

            gc.setGlobalAlpha(0.3);
            gc.setFill(tint.darker());                 // era RED
            gc.fillOval(p.getX() - r * 1.5, p.getY() - r * 1.5, r * 3, r * 3);
            gc.restore();
        };
    }

    @Override
    public AbstractProjectileModel blueprint() {
        return new MotherProjectile();
    }
}
 */