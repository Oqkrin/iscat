package uni.gaben.iscat.iscat_game.universe.projectiles;

import javafx.scene.paint.Color;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.iscat_game.universe.VelocitySettings;
import uni.gaben.iscat.iscat_game.universe.player.PlayerSettings;
import uni.gaben.iscat.utils.ThemeManager;

public enum ProjectileType {

    PLAYER_BULLET(VelocitySettings.PLAYER_BULLET_VELOCITY, 7.0, PlayerSettings.DANNO_PROIETTILE, UniverseCollisionLayers.PROJECTILE_FILTER, ThemeManager.getInstance().getColorWarning()),
    ENEMY_BULLET (VelocitySettings.ENEMY_BULLET_VELOCITY,  7.0, 4.0, UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER, ThemeManager.getInstance().getColorError());

    public final double         terminalVelocity;
    public final double         radiusPx;
    public final double energy;
    public final CategoryFilter filter;
    public final Color          color;

    ProjectileType(double terminalVelocity, double radiusPx,
                   double energy, CategoryFilter filter, Color color) {
        this.terminalVelocity = terminalVelocity;
        this.radiusPx         = radiusPx;
        this.energy = energy;
        this.filter           = filter;
        this.color            = color;
    }
}