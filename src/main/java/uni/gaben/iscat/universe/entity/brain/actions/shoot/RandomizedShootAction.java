package uni.gaben.iscat.universe.entity.brain.actions.shoot;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.RepeaterShooter;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.ShooterPattern;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;

import java.util.*;
import java.util.function.Consumer;

public class RandomizedShootAction extends AbstractShootAction {
    private static final double BURST_INTERVAL_S = 0.15;

    private final List<ShooterPattern> attackPool;
    private final Random rand = new Random();
    private Consumer<Projectile> customizer;

    // Burst state
    private int burstLeft = 0;
    private ShooterPattern burstPattern = null;

    public RandomizedShootAction(double combatRange, double cooldownSec,
                                 ProjectileType bulletType,
                                 Target target, boolean aimAtTarget,
                                 ShooterPattern... attacks) {
        super("randomized-shoot", combatRange, cooldownSec, bulletType, target, aimAtTarget);
        this.attackPool = List.of(attacks);
    }

    public static RandomizedShootAction targetingPlayer(double combatRange, double cooldownSec,
                                                        ProjectileType bulletType, boolean aimAtTarget,
                                                        ShooterPattern... attacks) {
        return new RandomizedShootAction(combatRange, cooldownSec, bulletType, universe -> Collections.singletonList(universe.getPlayer()), aimAtTarget,
                attacks);
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {
        double angle = getAimAngle(brain, world);
        ShooterPattern selected = attackPool.get(rand.nextInt(attackPool.size()));

        if (selected instanceof RepeaterShooter repeater) {
            burstPattern = repeater.getInner();
            burstLeft = repeater.getTimes();
            burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
            burstLeft--;
            cooldown.start(burstLeft > 0 ? BURST_INTERVAL_S : cooldown.getDefaultDuration());
        } else {
            selected.execute(brain.getShooter(), bulletType, angle, customizer);
            cooldown.start();
        }
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        if (burstLeft > 0) {
            cooldown.update(dt);
            if (cooldown.isReady()) {
                if (aimAtTarget) {
                    Vector2 targetPos = target.getPosition(world);
                    if (targetPos == null) {
                        burstLeft = 0;
                        return false;
                    }
                }
                double angle = getAimAngle(brain, world);
                burstPattern.execute(brain.getShooter(), bulletType, angle, customizer);
                burstLeft--;
                cooldown.start(burstLeft > 0 ? BURST_INTERVAL_S : cooldown.getDefaultDuration());
            }
            return true;
        }
        return false;
    }

    public void setCustomizer(Consumer<Projectile> customizer) {
        this.customizer = customizer;
    }
}