package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.attacks.AttackPattern;
import uni.gaben.iscat.iscat_game.universe.attacks.RepeaterAttack;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;
import java.util.function.DoubleSupplier;

public class ShooterBehaviour implements AiBehavior {

    private final double priorityValue;
    private final double combatRange;
    private final double preferredRange;
    private final double force;
    private final double rotationSpeed;
    private final DoubleSupplier cooldownSupplier;

    private Shooter<AbstractEntityModel> shooter = null;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();
    private final Random rand = new Random();
    private final AttackPattern[] attackPool;

    private int repeatedAttackLeft = 0;
    private AttackPattern currentRepeatedAttack = null;
    private final double REPEATED_ATTACK_COOLDOWN = 0.15;

    public ShooterBehaviour(double priorityValue, double combatRange, double preferredRange,
                            double force, double rotationSpeed, double globalCooldownS,
                            ProjectileType bulletType, AttackPattern... attacks) {
        this.priorityValue = priorityValue;
        this.combatRange = combatRange;
        this.preferredRange = preferredRange;
        this.force = force;
        this.rotationSpeed = rotationSpeed;
        this.cooldownSupplier = () -> globalCooldownS;
        this.bulletTemplate = new Projectile(bulletType);
        this.attackPool = attacks;
        this.fireCooldown.start(globalCooldownS);
    }

    public ShooterBehaviour(double priorityValue, double combatRange, double preferredRange,
                            double force, double rotationSpeed, DoubleSupplier cooldownSupplier,
                            ProjectileType bulletType, AttackPattern... attacks) {
        this.priorityValue = priorityValue;
        this.combatRange = combatRange;
        this.preferredRange = preferredRange;
        this.force = force;
        this.rotationSpeed = rotationSpeed;
        this.cooldownSupplier = cooldownSupplier;
        this.bulletTemplate = new Projectile(bulletType);
        this.attackPool = attacks;
        this.fireCooldown.start(cooldownSupplier.getAsDouble());
    }

    @Override
    public double getPriority(AbstractEntityModel enemy, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = enemy.getTransform().getTranslation().distance(player.getTransform().getTranslation());
        return dist <= combatRange ? priorityValue : 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (shooter == null) {
            this.shooter = new Shooter<>(npc);
        }

        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        Vector2 toPlayer = player.getTransform().getTranslation().copy().subtract(npc.getTransform().getTranslation());
        double dist = toPlayer.getMagnitude();

        if (dist < preferredRange) {
            npc.applyForce(toPlayer.getNormalized().multiply(-force * 0.6));
        } else if (dist > preferredRange * 1.2) {
            npc.applyForce(toPlayer.getNormalized().multiply(force * 0.4));
        }

        npc.setAngularVelocity(0.0);
        double current = npc.getTransform().getRotationAngle();
        double diff = toPlayer.getDirection() - current;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        npc.getTransform().setRotation(
                Interpolator.lerp(current, current + diff, Math.min(rotationSpeed * dt, 1.0))
        );

        if (!fireCooldown.isCoolingDown() && attackPool.length > 0) {
            double angleToPlayer = toPlayer.getDirection();

            // Se non c'è nessuna raffica attiva, analizziamo il prossimo attacco nel pool
            if (repeatedAttackLeft == 0) {
                AttackPattern selected = attackPool[rand.nextInt(attackPool.length)];

                if (selected instanceof RepeaterAttack repeater) {
                    // quando è un attacco a ripetizione configura la raffica con i suoi dati personalizzati
                    currentRepeatedAttack = repeater.getInner();
                    repeatedAttackLeft = repeater.getTimes();
                } else {
                    // quando è un attacco standard viene eseguito e messo il cooldown lungo
                    selected.execute(shooter, bulletTemplate, angleToPlayer, bullet -> {
                        bullet.setMaxLife(bullet.getLife());
                        bullet.setLife(bullet.getMaxLife());
                    });
                    fireCooldown.start(cooldownSupplier.getAsDouble());
                    return;
                }
            }

            // Se siamo qui, c'è una raffica attiva (derivata da un RepeaterAttack)
            currentRepeatedAttack.execute(shooter, bulletTemplate, angleToPlayer, bullet -> {
                bullet.setMaxLife(bullet.getLife());
                bullet.setLife(bullet.getMaxLife());
            });

            repeatedAttackLeft--;

            if (repeatedAttackLeft > 0) {
                fireCooldown.start(REPEATED_ATTACK_COOLDOWN);
            } else {
                currentRepeatedAttack = null;
                fireCooldown.start(cooldownSupplier.getAsDouble());
            }
        }
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        fireCooldown.update(dt);
    }
}