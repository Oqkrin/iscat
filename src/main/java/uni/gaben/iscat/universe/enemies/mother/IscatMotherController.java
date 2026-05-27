package uni.gaben.iscat.universe.enemies.mother;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;

import static uni.gaben.iscat.universe.enemies.mother.IscatMotherSettings.ISCATMOTHER;

/**
 * BUG 4 FIXED: The COMBAT state used {@code add(new AiBehavior(){...})}.
 * {@code AiBehavior} is not recognised by {@code add()}, so the entire combat
 * behavior (movement AND shooting) was silently dropped — IscatMother sat
 * motionless and never fired.
 *
 * FIX: split into:
 *   • MovementBehavior — maintains distance from the player
 *   • AttackBehavior   — fires the burst spread when in range
 * The spawn-minions AttackBehavior was already correct.
 */
public class IscatMotherController extends AiBehaviours<IscatMotherModel> {

    private final Shooter<IscatMotherModel> shooter;
    private final Projectile bulletTemplate;

    public IscatMotherController(IscatMotherModel mother) {
        super(mother, ISCATMOTHER.force, ISCATMOTHER.maxVelocity, ISCATMOTHER.rotationSpeed);

        shooter        = new Shooter<>(mother);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        mother.setOnDeath(this::spawnHorde);

        // ── ATTACK: spawn minions (highest priority, one-shot) ────────────────
        addAttack(new AttackBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return aiEntity.shouldSpawnMinions() ? 100.0 : 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                spawnMinions();
                aiEntity.markMinionsSpawned();
            }
        });

        // ── MOVEMENT: maintain ideal distance from player ─────────────────────
        // FIX BUG 4: was add(new AiBehavior()) — now properly typed MovementBehavior
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return universe.getPlayer() != null ? 50.0 : 0.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();

                Vector2 myPos     = aiEntity.getTransform().getTranslation();
                Vector2 toPlayer  = player.getTransform().getTranslation().copy().subtract(myPos);
                double  dist      = toPlayer.getMagnitude();
                double  idealDist = IscatMotherSettings.DISTANZA_IDEALE_M;

                Vector2 desiredVelocity = null;
                if (dist < idealDist - IscatMotherSettings.DISTANZA_TOLLERANZA_VICINO) {
                    desiredVelocity = toPlayer.getNormalized().multiply(-ISCATMOTHER.maxVelocity);
                } else if (dist > idealDist + IscatMotherSettings.DISTANZA_TOLLERANZA_LONTANO) {
                    desiredVelocity = toPlayer.getNormalized().multiply(ISCATMOTHER.maxVelocity);
                }
                // Always face the player (rotation target even when velocity is null)
                return MovementRequest.of(desiredVelocity, toPlayer.getDirection());
            }
        });

        // ── ATTACK: fire burst when in combat range ────────────────────────────
        // FIX BUG 4: shooting was inside the dropped AiBehavior — now an AttackBehavior
        addAttack(new AttackBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null || !aiEntity.isFireReady()) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return (dist >= IscatMotherSettings.COMBAT_RANGE_MIN
                        && dist <= IscatMotherSettings.COMBAT_RANGE_MAX) ? 60.0 : 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                shootBurst();
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;
        aiEntity.update(dt);
        super.aiUpdate(universeModel, dt);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void shootBurst() {
        double baseAngle  = aiEntity.getTransform().getRotationAngle();
        double spreadRad  = Math.toRadians(IscatMotherSettings.SPREAD_ANGLE_DEG);

        for (int i = -1; i <= 1; i++) {
            double  angle             = baseAngle + (i * spreadRad);
            Vector2 bulletTranslation = aiEntity.getTransform().getTranslation().copy()
                                                .add(Vector2.create(1.0, angle));
            shooter.shoot(bulletTemplate, bulletTranslation, angle);
        }
        aiEntity.startFireCooldown();
    }

    private void spawnMinions() {
        Vector2 myPos  = aiEntity.getTransform().getTranslation();
        double  radius = IscatMotherSettings.MINION_SPAWN_RADIUS;
        int     total  = IscatMotherSettings.MINION_ISCAT_COUNT + IscatMotherSettings.MINION_EATER_COUNT;

        for (int i = 0; i < total; i++) {
            double angle = Math.toRadians(i * (360.0 / total));
            double px    = UU.mToPx(myPos.x + Math.cos(angle) * radius);
            double py    = UU.mToPx(myPos.y + Math.sin(angle) * radius);
            UniverseSpawner.getInstance().spawn(
                    (i < IscatMotherSettings.MINION_ISCAT_COUNT
                            ? UniverseSpawnable.ISCAT_MOB
                            : UniverseSpawnable.EATER).name(), px, py);
        }
    }

    private void spawnHorde() {
        Vector2 center = aiEntity.getTransform().getTranslation();
        for (int i = 0; i < IscatMotherSettings.HORDE_SIZE; i++) {
            double angle = Math.toRadians(Math.random() * 360);
            double dist  = IscatMotherSettings.HORDE_RADIUS
                           + Math.random() * IscatMotherSettings.HORDE_RADIUS_VARIANCE;
            double px = UU.mToPx(center.x + Math.cos(angle) * dist);
            double py = UU.mToPx(center.y + Math.sin(angle) * dist);
            UniverseSpawner.getInstance().spawn(
                    (i % 2 == 0 ? UniverseSpawnable.HEART : UniverseSpawnable.EATER).name(), px, py);
        }
    }
}
