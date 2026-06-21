package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.shooters.RepeaterPattern;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import java.util.*;
import java.util.function.Consumer;

/**
 * Abilità di attacco IA con selezione stocastica del pattern e supporto a raffiche temporizzate (Randomized Shoot Ability).
 * <p>
 * Seleziona casualmente un pattern balistico da un pool a ogni attivazione. Supporta i pattern complessi di tipo
 * {@link RepeaterPattern}: in tal caso, esegue immediatamente la prima salva e delega l'aggiornamento (update-loop)
 * dei proiettili successivi alla propria routine interna, mantenendo l'intera esecuzione sincrona sul thread di gioco.
 * </p>
 */
public class RandomizedShootAbility extends AbstractShootAbility {

    private final List<Pattern> attackPool;
    private final Random rand = new Random();
    private Consumer<ProjectileModel> customizer;

    // --- Stato di gestione della Raffica (Burst State) ---
    private int burstLeft = 0;
    private Pattern burstPattern = null;
    private double burstInterval = 0.15;
    private double burstTimer = 0.0;

    /**
     * Inizializza l'abilità impostando il pool di pattern di attacco e configurando il modificatore di danno standard.
     */
    public RandomizedShootAbility(double combatRange, double cooldownSec,
                                  ProjectileType bulletType,
                                  Target target, boolean aimAtTarget, double nerfPrediction,
                                  double damage, int attackStateIndex,
                                  Pattern... attacks) {
        super("randomized-shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget, nerfPrediction, attackStateIndex);
        this.attackPool = List.of(attacks);

        // Customizer lambda predefinito per iniettare il danno specifico ad ogni proiettile generato
        this.customizer = projectile -> projectile.setEnergyDirect(damage);
    }

    /**
     * Factory method per generare rapidamente un'abilità di sparo casuale ancorata e mirata verso il giocatore umano.
     */
    public static RandomizedShootAbility targetingPlayer(double combatRange, double cooldownSec,
                                                         ProjectileType bulletType, boolean aimAtTarget,
                                                         double nerfPrediction,
                                                         double damage, int attackStateIndex,
                                                         Pattern... attacks) {
        return new RandomizedShootAbility(combatRange, cooldownSec, bulletType,
                universe -> Collections.singletonList(universe.getPlayer()), aimAtTarget, nerfPrediction, damage, attackStateIndex, attacks);
    }

    /**
     * Attiva l'abilità selezionando un pattern casuale dal pool. Se il pattern estratto è un {@link RepeaterPattern},
     * ne inizializza lo stato interno e i timer per le raffiche differite, altrimenti esegue lo sparo istantaneo in colpo singolo.
     */
    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        super.onActivate(brain, world);

        double angle = getAimAngle(brain, world, bulletType.terminalVelocity);
        Pattern selected = attackPool.get(rand.nextInt(attackPool.size()));

        if (selected instanceof RepeaterPattern(int times, double intervalSeconds, Pattern inner)) {
            burstPattern = inner;
            burstLeft = times - 1;
            burstInterval = intervalSeconds;
            burstTimer = burstInterval;
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
        } else {
            burstLeft = 0;
            burstPattern = null;
            selected.execute(brain.getShooter(), bulletType, angle, customizer);
        }
        cooldown.start();
    }

    /**
     * Aggiorna e fa avanzare lo stato delle raffiche nel tempo (Burst Loop).
     * Ricalcola progressivamente l'angolo di mira predittivo a ogni intervallo della raffica e determina
     * la fine del ciclo vitale dell'azione all'esaurimento dei colpi.
     *
     * @return {@code true} se vi sono ancora colpi in coda per la raffica corrente, altrimenti {@code false}.
     */
    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        if (burstLeft <= 0) return false;

        burstTimer -= dt;
        if (burstTimer <= 0) {
            burstTimer = burstInterval;

            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) {
                burstLeft = 0;
                return false;
            }

            // Ricalcola dinamicamente l'intercettazione balistica prima di emettere la nuova raffica
            double angle = getAimAngle(brain, world, bulletType.terminalVelocity);
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
            burstLeft--;
        }

        return burstLeft > 0;
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}

    /**
     * Inietta una logica di modifica personalizzata (Consumer) applicata istantaneamente a ogni proiettile istanziato.
     */
    public void setCustomizer(Consumer<ProjectileModel> customizer) {
        this.customizer = customizer;
    }
}