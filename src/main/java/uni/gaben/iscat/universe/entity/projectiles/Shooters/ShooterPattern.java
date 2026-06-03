package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

public interface ShooterPattern {
    /**
     * Esegue l'attacco custom.
     * @param shooter Lo shooter (es. il controller o l'entità che spara)
     * @param type Il tipo di proiettile
     * @param angle L'asse di puntamento attuale (es. angolo del mouse)
     * @param customizer Eventuali modifiche dinamiche al proiettile (es. bonus danno da livello)
     */
    void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer);
}