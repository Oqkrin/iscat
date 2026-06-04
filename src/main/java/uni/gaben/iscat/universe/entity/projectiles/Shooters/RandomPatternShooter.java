package uni.gaben.iscat.universe.entity.projectiles.Shooters;

import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Prende una lista di pattern e ne esegue uno a caso..
 */
public class RandomPatternShooter implements PatternShooter {

    private final List<PatternShooter> attackPool;
    private final Random rand = new Random();

    public RandomPatternShooter(PatternShooter... attacks) {
        if (attacks.length == 0) {
            throw new IllegalArgumentException("L'attack pool non può essere vuoto!");
        }
        this.attackPool = List.of(attacks);
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        // Sceglie un attacco a caso dal pool
        PatternShooter selected = attackPool.get(rand.nextInt(attackPool.size()));

        // Lo esegue passando i parametri corretti richiesti dall'interfaccia
        selected.execute(shooter, type, angle, customizer);
    }
}