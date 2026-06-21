package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import java.util.function.Consumer;

/**
 * Abilità di attacco base per l'esecuzione di uno sparo istantaneo (Shoot Ability).
 * A differenza delle raffiche temporizzate, risolve l'attacco in un singolo frame calcolando
 * l'angolo di mira predittivo e applicando direttamente il danno e il tipo di munizione
 * al proiettile istanziato tramite un inizializzatore dinamico (Consumer).
 */
public class ShootAbility extends AbstractShootAbility {

    private final Pattern pattern;
    private final double dannoProiettile;

    /**
     * Inizializza l'abilità di sparo standard configurando le metriche balistiche e il danno d'impatto.
     */
    public ShootAbility(double combatRange, double cooldownSec,
                        ProjectileType bulletType, Pattern pattern,
                        Target target, boolean aimAtTarget, double nerfPrediction,
                        double dannoProiettile, int attackStateIndex) {

        super("shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget, nerfPrediction, attackStateIndex);
        this.pattern = pattern;
        this.dannoProiettile = dannoProiettile;
    }

    /**
     * Attiva l'attacco istantaneo. Ricalcola l'allineamento balistico ottimale, configura
     * i parametri energetici e strutturali della munizione ed esegue il pattern sul motore di gioco.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        super.onActivate(brain, world);

        double angle = getAimAngle(brain, world, bulletType.terminalVelocity);

        // Configurazione dinamica dei parametri fisici del proiettile prima dell'immissione nel mondo
        Consumer<ProjectileModel> customizer = bullet -> {
            bullet.setType(bulletType);
            bullet.setEnergyDirect(dannoProiettile);
        };

        pattern.execute(brain.getShooter(), bulletType, angle, customizer);
        cooldown.start();
    }

    /**
     * Dichiara la fine immediata dell'attivazione dell'azione.
     * * @return {@code false} poiché l'attacco è di tipo istantaneo (single-frame) e non richiede aggiornamenti differiti.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        return false;
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}
}