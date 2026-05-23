package uni.gaben.iscat.game.universe.projectiles;

import javafx.scene.paint.Color;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.VelocitySettings;
import uni.gaben.iscat.game.universe.player.PlayerSettings;

public enum ProjectileType {

    PLAYER_BULLET(VelocitySettings.PLAYER_BULLET_VELOCITY, 8.0, PlayerSettings.DANNO_PROIETTILE, UniverseCollisionLayers.PROJECTILE_FILTER,       Color.CYAN),
    ENEMY_BULLET (VelocitySettings.ENEMY_BULLET_VELOCITY,  8.0, 4.0, UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER, Color.ORANGERED);

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