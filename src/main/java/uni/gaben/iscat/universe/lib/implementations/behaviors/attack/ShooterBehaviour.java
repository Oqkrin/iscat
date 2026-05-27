package uni.gaben.iscat.universe.lib.implementations.behaviors.attack;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

/**
 * Fires projectiles at the player using a pool of {@link AttackPattern}s.
 *
 * <p>This is now a pure {@link AttackBehavior}. It no longer applies movement
 * forces for "preferred range" maintenance — that concern belongs to a
 * companion {@link uni.gaben.iscat.universe.lib.implementations.behaviors.movement.OrbitPlayerBehavior}
 * configured with the same radius. The two behaviors compose cleanly because
 * they operate on separate tracks.</p>
 *
 * <h3>Attack sequencing</h3>
 * <ol>
 *   <li>A random pattern is selected from {@code attackPool}.</li>
 *   <li>If it is a {@link RepeaterAttack}, each burst shot fires at
 *       {@value #BURST_INTERVAL_S}s intervals until the burst is exhausted,
 *       then the global cooldown restarts.</li>
 *   <li>Otherwise the pattern fires once and the global cooldown restarts.</li>
 * </ol>
 */
public class ShooterBehaviour implements AttackBehavior {

    private static final double BURST_INTERVAL_S = 0.15;

    private final double          combatRange;
    private final double          priorityValue;
    private final DoubleSupplier  cooldownSupplier;
    private final Projectile      bulletTemplate;
    private final AttackPattern[] attackPool;
    private final Cooldown        fireCooldown = new Cooldown();
    private final Random          rand         = new Random();

    private Shooter<AbstractEntityModel> shooter = null;
    private int                          burstLeft       = 0;
    private AttackPattern                burstPattern    = null;
    private Consumer<Projectile>         customizer      = null;

    public ShooterBehaviour(double priorityValue, double combatRange,
                             double cooldownSeconds, ProjectileType bulletType,
                             AttackPattern... attacks) {
        this(priorityValue, combatRange, () -> cooldownSeconds, bulletType, attacks);
    }

    public ShooterBehaviour(double priorityValue, double combatRange,
                             DoubleSupplier cooldownSupplier, ProjectileType bulletType,
                             AttackPattern... attacks) {
        this.priorityValue    = priorityValue;
        this.combatRange      = combatRange;
        this.cooldownSupplier = cooldownSupplier;
        this.bulletTemplate   = new Projectile(bulletType);
        this.attackPool       = attacks;
        this.fireCooldown.start(cooldownSupplier.getAsDouble());
    }

    // ── AttackBehavior ───────────────────────────────────────────────────────

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = npc.getTransform().getTranslation()
                         .distance(player.getTransform().getTranslation());
        return dist <= combatRange ? priorityValue : 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (shooter == null) shooter = new Shooter<>(npc);

        PlayerModel player = universe.getPlayer();
        if (player == null) return;
        if (fireCooldown.isCoolingDown() || attackPool.length == 0) return;

        double angleToPlayer = player.getTransform().getTranslation()
                                     .copy().subtract(npc.getTransform().getTranslation())
                                     .getDirection();

        if (burstLeft == 0) {
            // Select next attack
            AttackPattern selected = attackPool[rand.nextInt(attackPool.length)];
            if (selected instanceof RepeaterAttack repeater) {
                burstPattern = repeater.getInner();
                burstLeft    = repeater.getTimes();
            } else {
                selected.execute(shooter, bulletTemplate, angleToPlayer, customizer);
                fireCooldown.start(cooldownSupplier.getAsDouble());
                return;
            }
        }

        // Burst shot
        burstPattern.execute(shooter, bulletTemplate, angleToPlayer, customizer);
        burstLeft--;

        fireCooldown.start(burstLeft > 0 ? BURST_INTERVAL_S : cooldownSupplier.getAsDouble());
        if (burstLeft == 0) burstPattern = null;
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        fireCooldown.update(dt);
    }

    public void setCustomizer(Consumer<Projectile> customizer) {
        this.customizer = customizer;
    }
}
