package uni.gaben.iscat.universe.entities.brain.abilities.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.universe.entities.shooters.Pattern;
import uni.gaben.iscat.universe.entities.shooters.RepeaterPattern;
import uni.gaben.iscat.universe.entities.brain.*;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Shoot action that randomly selects an attack pattern from a pool each time it fires.
 * <p>
 * Supports {@link RepeaterPattern}: when selected, the first burst fires immediately
 * and subsequent bursts are driven by this action's own update loop at the repeater's interval,
 * keeping all physics interactions on the game thread.
 */
public class RandomizedShootAbility extends AbstractShootAbility {

    private final List<Pattern> attackPool;
    private final Random rand = new Random();
    private Consumer<ProjectileModel> customizer;
    private final double damage;

    // Burst state — only active when a RepeaterPattern was selected
    private int burstLeft = 0;
    private Pattern burstPattern = null;
    private double burstInterval = 0.15;
    private double burstTimer = 0.0;


    public RandomizedShootAbility(double combatRange, double cooldownSec,
                                  ProjectileType bulletType,
                                  Target target, boolean aimAtTarget, double nerfPrediction,
                                  double damage, int attackStateIndex,
                                  Pattern... attacks) {
        super("randomized-shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget, nerfPrediction, attackStateIndex);
        this.damage = damage;
        this.attackPool = List.of(attacks);

        this.customizer = projectile -> projectile.setEnergyDirect(damage);
    }

    public static RandomizedShootAbility targetingPlayer(double combatRange, double cooldownSec,
                                                         ProjectileType bulletType, boolean aimAtTarget,
                                                         double nerfPrediction,
                                                         double damage, int attackStateIndex,
                                                         Pattern... attacks) {
        return new RandomizedShootAbility(combatRange, cooldownSec, bulletType,
                universe -> Collections.singletonList(universe.getPlayer()), aimAtTarget, nerfPrediction, damage, attackStateIndex, attacks);
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        super.onActivate(brain, world);

        double angle = getAimAngle(brain, world, bulletType.terminalVelocity);
        Pattern selected = attackPool.get(rand.nextInt(attackPool.size()));

        if (selected instanceof RepeaterPattern repeater) {
            burstPattern = repeater.inner();
            burstLeft = repeater.times() - 1;
            burstInterval = repeater.intervalSeconds();
            burstTimer = burstInterval;
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
        } else {
            burstLeft = 0;
            burstPattern = null;
            selected.execute(brain.getShooter(), bulletType, angle, customizer);
        }
        cooldown.start();
    }

    @Override
    public boolean progressActivation(Brain<?> brain, UniverseModel world, double dt) {
        if (burstLeft <= 0) return false; // done, action can be dequeued

        burstTimer -= dt;
        if (burstTimer <= 0) {
            burstTimer = burstInterval;

            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) {
                burstLeft = 0;
                return false;
            }

            double angle = getAimAngle(brain, world, bulletType.terminalVelocity);
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
            burstLeft--;
        }

        return burstLeft > 0; // keep alive while bursts remain
    }

    @Override
    public void update(Brain<?> brain, UniverseModel world, double dt) {}

    public void setCustomizer(Consumer<ProjectileModel> customizer) {
        this.customizer = customizer;
    }
}