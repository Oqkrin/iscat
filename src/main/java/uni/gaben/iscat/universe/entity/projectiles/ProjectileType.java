package uni.gaben.iscat.universe.entity.projectiles;

import javafx.scene.paint.Color;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseVelocitySettings;
import uni.gaben.iscat.universe.entity.player.PlayerSettings;
import uni.gaben.iscat.utils.theme.ThemeManager;

public enum ProjectileType {

    PLAYER_BULLET(UniverseVelocitySettings.PLAYER_BULLET_VELOCITY, 4.0, PlayerSettings.DANNO_PROIETTILE, UniverseCollisionLayers.PROJECTILE_FILTER, ThemeManager.getInstance().getAccentPrimary()),
    ENEMY_BULLET (UniverseVelocitySettings.ENEMY_BULLET_VELOCITY,  4.0, 4.0, UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER, ThemeManager.getInstance().getColorError()),
    STUN_BULLET (UniverseVelocitySettings.ENEMY_BULLET_VELOCITY, 7.0, 1.0, UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER, ThemeManager.getInstance().getAccentTernary()),;
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