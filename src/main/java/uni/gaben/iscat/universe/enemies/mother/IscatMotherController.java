package uni.gaben.iscat.universe.enemies.mother;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ObstacleAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.UniverseSpawner;

import static uni.gaben.iscat.universe.enemies.mother.IscatMotherSettings.ISCATMOTHER;

public class IscatMotherController extends AiController {

    private final Shooter<IscatMotherModel> shooter;
    private final Projectile bulletTemplate;

    public IscatMotherController(IscatMotherModel mother) {
        super(mother, ISCATMOTHER.force, ISCATMOTHER.maxVelocity, ISCATMOTHER.rotationSpeed);

        shooter = new Shooter<>(mother);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // Death callback – spawn horde of hearts and eaters
        mother.setOnDeath(this::spawnHorde);

        // Movement: maintain ideal distance from the player (slow)
        setMovementStrategy(new MotherDistanceStrategy());

        // Avoidance modifiers
        addModifier(new ObstacleAvoidanceModifier());
        addModifier(new ProjectileAvoidanceModifier());

        // Attack 1: Spawn minions when damaged (one‑shot, highest priority)
        addAttack(new SpawnMinionsAttack());

        // Attack 2: Spread burst when in combat range and fire ready
        addAttack(new SpreadBurstAttack());
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null || entity.shouldRemove()) return;
        if (entity instanceof IscatMotherModel mother) {
            mother.update(dt);   // entity‑specific updates (cooldowns, flags)
        }
        super.update(universe, dt);
    }

    // ── Movement strategy: maintain ideal distance ────────────────────────

    private class MotherDistanceStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 myPos = entity.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            Vector2 toPlayer = playerPos.copy().subtract(myPos);
            double dist = toPlayer.getMagnitude();
            double ideal = IscatMotherSettings.DISTANZA_IDEALE_M;

            if (dist < ideal - IscatMotherSettings.DISTANZA_TOLLERANZA_VICINO) {
                // Too close → move away
                return toPlayer.getNormalized().multiply(-ISCATMOTHER.maxVelocity);
            } else if (dist > ideal + IscatMotherSettings.DISTANZA_TOLLERANZA_LONTANO) {
                // Too far → move closer
                return toPlayer.getNormalized().multiply(ISCATMOTHER.maxVelocity);
            }
            // In ideal range → hold position (zero velocity)
            return new Vector2();
        }
    }

    // ── Attack: spawn minions when damaged ────────────────────────────────

    private class SpawnMinionsAttack implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (!(entity instanceof IscatMotherModel mother)) return 0.0;
            return mother.shouldSpawnMinions() ? 100.0 : 0.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            if (!(entity instanceof IscatMotherModel mother)) return;
            spawnMinions(mother);
            mother.markMinionsSpawned();   // reset flag so it fires only once
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            // nothing
        }
    }

    // ── Attack: spread burst ──────────────────────────────────────────────

    private class SpreadBurstAttack implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (!(entity instanceof IscatMotherModel mother)) return 0.0;
            if (!mother.isFireReady()) return 0.0;
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;
            double dist = entity.getTransform().getTranslation()
                    .distance(player.getTransform().getTranslation());
            if (dist < IscatMotherSettings.COMBAT_RANGE_MIN || dist > IscatMotherSettings.COMBAT_RANGE_MAX) return 0.0;
            return 60.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            if (!(entity instanceof IscatMotherModel mother)) return;
            shootBurst(mother);
            mother.startFireCooldown();
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            // nothing
        }
    }

    // ── Helper methods (unchanged logic) ──────────────────────────────────

    private void shootBurst(IscatMotherModel mother) {
        double baseAngle = mother.getTransform().getRotationAngle();
        double spreadRad = Math.toRadians(IscatMotherSettings.SPREAD_ANGLE_DEG);
        for (int i = -1; i <= 1; i++) {
            double angle = baseAngle + (i * spreadRad);
            Vector2 bulletPos = mother.getTransform().getTranslation().copy()
                    .add(Vector2.create(1.0, angle));
            shooter.shoot(bulletTemplate, bulletPos, angle);
        }
    }

    private void spawnMinions(IscatMotherModel mother) {
        Vector2 myPos = mother.getTransform().getTranslation();
        double radius = IscatMotherSettings.MINION_SPAWN_RADIUS;
        int total = IscatMotherSettings.MINION_ISCAT_COUNT + IscatMotherSettings.MINION_EATER_COUNT;
        for (int i = 0; i < total; i++) {
            double angle = Math.toRadians(i * (360.0 / total));
            double px = UU.mToPx(myPos.x + Math.cos(angle) * radius);
            double py = UU.mToPx(myPos.y + Math.sin(angle) * radius);
            UniverseSpawner.getInstance().spawn(
                    (i < IscatMotherSettings.MINION_ISCAT_COUNT
                            ? UniverseSpawnable.ISCAT_MOB
                            : UniverseSpawnable.EATER).name(), px, py);
        }
    }

    private void spawnHorde() {
        Vector2 center = entity.getTransform().getTranslation();
        for (int i = 0; i < IscatMotherSettings.HORDE_SIZE; i++) {
            double angle = Math.toRadians(Math.random() * 360);
            double dist = IscatMotherSettings.HORDE_RADIUS
                    + Math.random() * IscatMotherSettings.HORDE_RADIUS_VARIANCE;
            double px = UU.mToPx(center.x + Math.cos(angle) * dist);
            double py = UU.mToPx(center.y + Math.sin(angle) * dist);
            UniverseSpawner.getInstance().spawn(
                    (i % 2 == 0 ? UniverseSpawnable.HEART : UniverseSpawnable.EATER).name(), px, py);
        }
    }
}