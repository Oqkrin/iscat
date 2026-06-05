package uni.gaben.iscat.universe.entity.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.projectiles.shooters.PatternShooter;
import uni.gaben.iscat.universe.entity.projectiles.shooters.RepeaterPatternShooter;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;

import java.util.*;
import java.util.function.Consumer;

/**
 * Shoot action that randomly selects an attack pattern from a pool each time it fires.
 * <p>
 * Supports {@link RepeaterPatternShooter}: when selected, the first burst fires immediately
 * and subsequent bursts are driven by this action's own update loop at the repeater's interval,
 * keeping all physics interactions on the game thread.
 */
public class RandomizedShootAction extends AbstractShootAction {

    private final List<PatternShooter> attackPool;
    private final Random rand = new Random();
    private Consumer<Projectile> customizer;

    // Burst state — only active when a RepeaterPatternShooter was selected
    private int burstLeft = 0;
    private PatternShooter burstPattern = null;
    private double burstInterval = 0.15;
    private double burstTimer = 0.0;

    public RandomizedShootAction(double combatRange, double cooldownSec,
                                 ProjectileType bulletType,
                                 Target target, boolean aimAtTarget,
                                 PatternShooter... attacks) {
        super("randomized-shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget);
        this.attackPool = List.of(attacks);
    }

    public static RandomizedShootAction targetingPlayer(double combatRange, double cooldownSec,
                                                        ProjectileType bulletType, boolean aimAtTarget,
                                                        PatternShooter... attacks) {
        return new RandomizedShootAction(combatRange, cooldownSec, bulletType,
                universe -> Collections.singletonList(universe.getPlayer()), aimAtTarget, attacks);
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        double angle = getAimAngle(brain, world);
        PatternShooter selected = attackPool.get(rand.nextInt(attackPool.size()));

        if (selected instanceof RepeaterPatternShooter repeater) {
            burstPattern = repeater.inner();
            burstLeft = repeater.times() - 1; // first shot fires now
            burstInterval = repeater.intervalSeconds();
            burstTimer = burstInterval;
            // Fire the first burst immediately
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
        } else {
            burstLeft = 0;
            burstPattern = null;
            selected.execute(brain.getShooter(), bulletType, angle, customizer);
        }
        // Always start the main cooldown immediately — burst continuation is handled in update()
        cooldown.start();
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        if (burstLeft <= 0) return false; // done, action can be dequeued

        burstTimer -= dt;
        if (burstTimer <= 0) {
            burstTimer = burstInterval;

            Vector2 targetPos = target.getPosition(world);
            if (targetPos == null) {
                burstLeft = 0;
                return false;
            }

            double angle = getAimAngle(brain, world);
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
            burstLeft--;
        }

        return burstLeft > 0; // keep alive while bursts remain
    }

    public void setCustomizer(Consumer<Projectile> customizer) {
        this.customizer = customizer;
    }
}