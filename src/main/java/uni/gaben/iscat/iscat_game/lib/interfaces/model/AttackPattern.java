package uni.gaben.iscat.iscat_game.lib.interfaces.model;

import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

public interface AttackPattern {
    /**
     * Esegue l'attacco custom.
     * @param shooter Lo shooter (es. il controller o l'entità che spara)
     * @param template Il tipo di proiettile base
     * @param angle L'asse di puntamento attuale (es. angolo del mouse)
     * @param customizer Eventuali modifiche dinamiche al proiettile (es. bonus danno da livello)
     */
    void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer);
}