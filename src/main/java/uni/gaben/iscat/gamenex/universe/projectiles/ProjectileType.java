package uni.gaben.iscat.gamenex.universe.projectiles;

import javafx.scene.paint.Color;
import org.dyn4j.collision.CategoryFilter;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;

public enum ProjectileType {

    PLAYER_BULLET(10.0, 18.0, 5.0, 3.0, UniverseCollisionLayers.PROJECTILE_FILTER,       Color.CYAN),
    ENEMY_BULLET ( 6.0, 12.0, 4.0, 4.0, UniverseCollisionLayers.ENEMY_PROJECTILE_FILTER, Color.ORANGERED);

    public final double         damage;
    public final double         terminalVelocity;
    public final double         radiusPx;
    public final double         lifespan;
    public final CategoryFilter filter;
    public final Color          color;

    ProjectileType(double damage, double terminalVelocity, double radiusPx,
                   double lifespan, CategoryFilter filter, Color color) {
        this.damage           = damage;
        this.terminalVelocity = terminalVelocity;
        this.radiusPx         = radiusPx;
        this.lifespan         = lifespan;
        this.filter           = filter;
        this.color            = color;
    }
}