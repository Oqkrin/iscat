package uni.gaben.iscat.game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.attacks.AttackPattern;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;
import java.util.Random;

public class ShooterBehaviour<T extends AbstractEntityModel & HasProjectile<? extends AbstractProjectileModel>> implements AiBehavior {

    private final double priorityValue;
    private final double combatRange;
    private final double preferredRange;
    private final double force;
    private final double rotationSpeed;
    private final double globalCooldownS;

    private Shooter<T> shooter = null;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();
    private final Random rand = new Random();

    private final AttackPattern<T>[] attackPool;
    private AttackPattern<T> activeAttack = null;

    @SafeVarargs
    public ShooterBehaviour(double priorityValue, double combatRange, double preferredRange,
                            double force, double rotationSpeed, double globalCooldownS,
                            ProjectileType bulletType, AttackPattern<T>... attacks) {
        this.priorityValue = priorityValue;
        this.combatRange = combatRange;
        this.preferredRange = preferredRange;
        this.force = force;
        this.rotationSpeed = rotationSpeed;
        this.globalCooldownS = globalCooldownS;
        this.bulletTemplate = new Projectile(bulletType);
        this.attackPool = attacks;
    }

    @Override
    public double getPriority(AbstractEntityModel enemy, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = enemy.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());
        return dist <= combatRange ? priorityValue : 0.0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        T entity = (T) npc;
        fireCooldown.update(dt);

        if (shooter == null) {
            this.shooter = new Shooter<>(entity);
        }

        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        Vector2 toPlayer = player.getTransform().getTranslation().copy().subtract(entity.getTransform().getTranslation());
        double dist = toPlayer.getMagnitude();

        // Kiting
        if (dist < preferredRange) {
            entity.applyForce(toPlayer.getNormalized().multiply(-force * 0.6));
        } else if (dist > preferredRange * 1.2) {
            entity.applyForce(toPlayer.getNormalized().multiply(force * 0.4));
        }

        // Rotazione
        entity.setAngularVelocity(0.0);
        double current = entity.getTransform().getRotationAngle();
        double diff = toPlayer.getDirection() - current;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        entity.getTransform().setRotation(Interpolator.lerp(current, current + diff, Math.min(rotationSpeed * dt, 1.0)));

        // Pattern di attacco
        double angleToPlayer = toPlayer.getDirection();
        if (activeAttack != null) {
            if (activeAttack.updateAndExecute(entity, shooter, bulletTemplate, angleToPlayer, dt)) {
                activeAttack = null;
            }
        } else if (!fireCooldown.isCoolingDown() && attackPool.length > 0) {
            activeAttack = attackPool[rand.nextInt(attackPool.length)];
            activeAttack.reset(); // ← QUI, subito dopo aver scelto l'attacco
            fireCooldown.start(globalCooldownS);
        }
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        fireCooldown.update(dt);
    }
}