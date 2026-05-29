package uni.gaben.iscat.universe.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.*;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class RandomizedShootAction extends AbstractShootAction {
    private static final double BURST_INTERVAL_S = 0.15;

    private final List<AttackPattern> attackPool;
    private final Random rand = new Random();
    private Consumer<Projectile> customizer;

    // Burst state (exactly like old ShooterBehaviour)
    private int burstLeft = 0;
    private AttackPattern burstPattern = null;

    public RandomizedShootAction(double combatRange, double cooldownSec,
                                 ProjectileType bulletType,
                                 Function<UniverseModel, Vector2> targetSupplier,
                                 AttackPattern... attacks) {
        super("randomized-shoot", combatRange, cooldownSec, bulletType, targetSupplier);
        this.attackPool = List.of(attacks);
    }

    public static RandomizedShootAction targetingPlayer(double combatRange, double cooldownSec,
                                                        ProjectileType bulletType,
                                                        AttackPattern... attacks) {
        return new RandomizedShootAction(combatRange, cooldownSec, bulletType,
                world -> {
                    var p = world.getPlayer();
                    return p != null ? p.getTransform().getTranslation() : null;
                },
                attacks);
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        double angle = getAimAngle(brain, world);
        AttackPattern selected = attackPool.get(rand.nextInt(attackPool.size()));

        if (selected instanceof RepeaterAttack repeater) {
            burstPattern = repeater.getInner();
            burstLeft = repeater.getTimes();
            // Fire first burst shot
            burstPattern.execute(brain.getShooter(), createBullet(), angle, customizer);
            burstLeft--;
            cooldown.start(burstLeft > 0 ? BURST_INTERVAL_S : cooldown.getDefaultDuration());
        } else {
            selected.execute(brain.getShooter(), createBullet(), angle, customizer);
            cooldown.start();
        }
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        if (burstLeft > 0) {
            cooldown.update(dt);
            if (cooldown.isReady()) {
                Vector2 target = targetSupplier.apply(world);
                if (target == null) {
                    burstLeft = 0;
                    return false;
                }
                double angle = brain.angleToTarget(target);
                burstPattern.execute(brain.getShooter(), createBullet(), angle, customizer);
                burstLeft--;
                cooldown.start(burstLeft > 0 ? BURST_INTERVAL_S : cooldown.getDefaultDuration());
            }
            return true; // still bursting
        }
        return false; // burst finished
    }

    public void setCustomizer(Consumer<Projectile> customizer) {
        this.customizer = customizer;
    }
}