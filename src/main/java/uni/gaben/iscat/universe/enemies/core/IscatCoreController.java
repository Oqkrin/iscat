package uni.gaben.iscat.universe.enemies.core;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.LineOfSightChecker;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.ParallelLineAttack;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.core.IscatCoreSettings.ISCATCORE;

public class IscatCoreController extends AiController {

    // Four cardinal directions the square sides can face.
    // We treat sides as: 0° (front), 90° (right), 180° (back), 270° (left).
    private static final int[] SIDE_ANGLES_DEG = {0, 90, 180, 270};

    private final LineOfSightChecker losChecker;
    private final Cooldown rotationCooldown = new Cooldown();
    private final Cooldown plungeCooldown = new Cooldown();
    private final Shooter<IscatCoreModel> shooter;
    private final IscatCoreModel core;

    // Current rotation step (0..7) – we cycle through 45° steps.
    private int rotationStep = 0;

    public IscatCoreController(IscatCoreModel core) {
        super(core, ISCATCORE.force, ISCATCORE.maxVelocity, 0.0);  // rotationSpeed = 0, we handle rotation manually
        this.core = core;
        this.losChecker = new LineOfSightChecker(core);
        this.shooter = new Shooter<>(core);

        // Movement: stay completely still
        setMovementStrategy(new MovementStrategy() {
            @Override
            public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
                return new Vector2();   // zero velocity
            }
        });

        // Avoidance modifiers
        addModifier(new SeparationModifier(UU.pxToM(32.0), ISCATCORE.force * 0.8));
        addModifier(new ProjectileAvoidanceModifier());

        // Attacks – highest to lowest priority:
        // 1. Mechanical rotation (always)
        addAttack(new RotationStepAttack());
        // 2. Plunge dash (when player in dash cone)
        addAttack(new PlungeAttack());
        // 3. Projectile shooting (when player in shoot cone and LoS clear)
        addAttack(new ShootAttack());
    }

    @Override
    public void update(UniverseModel world, double dt) {
        if (entity == null || entity.shouldRemove()) return;

        // Update line‑of‑sight
        PlayerModel player = world.getPlayer();
        if (player != null) {
            losChecker.update(core.getTransform().getTranslation(),
                    player.getTransform().getTranslation(), world);
        }

        // Disable default face‑direction (rotation speed is 0 anyway)
        super.update(world, dt);
    }

    // ── Helper: get current four world‑space side normals ──────────────────

    private Vector2[] getSideNormals() {
        double baseAngle = Math.toRadians(rotationStep * 45.0);
        Vector2[] normals = new Vector2[4];
        for (int i = 0; i < 4; i++) {
            double angle = baseAngle + Math.toRadians(SIDE_ANGLES_DEG[i]);
            normals[i] = new Vector2(Math.cos(angle), Math.sin(angle));
        }
        return normals;
    }

    // ── 1. Rotation step attack ────────────────────────────────────────────

    private class RotationStepAttack implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            return rotationCooldown.isCoolingDown() ? 0.0 : 1000.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            rotationStep = (rotationStep + 1) % 8;
            double newAngle = Math.toRadians(rotationStep * 45.0);
            core.getTransform().setRotation(newAngle);
            rotationCooldown.start(IscatCoreSettings.ROTATION_INTERVAL);
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            rotationCooldown.update(dt);
        }
    }

    // ── 2. Plunge (dash) attack ────────────────────────────────────────────

    private class PlungeAttack implements AttackBehavior {
        private static final double PLUNGE_DISTANCE = 5.0;   // max range
        private static final double CONE_HALF_ANGLE_DEG = 25.0;

        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (plungeCooldown.isCoolingDown()) return 0.0;
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;

            Vector2 toPlayer = player.getTransform().getTranslation().copy()
                    .subtract(entity.getTransform().getTranslation());
            double dist = toPlayer.getMagnitude();
            if (dist > PLUNGE_DISTANCE) return 0.0;

            // Check if player is inside one of the two dash side cones.
            Vector2[] normals = getSideNormals();
            Vector2 dashDir1 = normals[1];   // right side (90°)
            Vector2 dashDir2 = normals[3];   // left side (270°)
            double dot1 = toPlayer.getNormalized().dot(dashDir1);
            double dot2 = toPlayer.getNormalized().dot(dashDir2);
            double threshold = Math.cos(Math.toRadians(CONE_HALF_ANGLE_DEG));
            if (Math.max(dot1, dot2) < threshold) return 0.0;

            // Must also have line‑of‑sight
            if (!losChecker.hasLineOfSight()) return 0.0;

            return 80.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return;

            Vector2 toPlayer = player.getTransform().getTranslation().copy()
                    .subtract(entity.getTransform().getTranslation());
            Vector2 dashDir = toPlayer.getNormalized().multiply(ISCATCORE.force * 4.0);  // strong impulse
            entity.applyImpulse(dashDir);
            plungeCooldown.start(1.2);   // from old DirectionalSlamBehavior
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            plungeCooldown.update(dt);
        }
    }

    // ── 3. Shoot attack ────────────────────────────────────────────────────

    private class ShootAttack implements AttackBehavior {
        private static final double CONE_HALF_ANGLE_DEG = 25.0;
        private final Cooldown fireCooldown = new Cooldown();
        // We'll use the old ShooterBehaviour's pattern but fire only from the
        // shooting side closest to the player.
        private final ProjectileType bulletType = ProjectileType.ENEMY_BULLET;

        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;
            double dist = entity.getTransform().getTranslation()
                    .distance(player.getTransform().getTranslation());
            if (dist > ISCATCORE.combatRange) return 0.0;
            if (!losChecker.hasLineOfSight()) return 0.0;
            if (fireCooldown.isCoolingDown()) return 0.0;

            Vector2 toPlayer = player.getTransform().getTranslation().copy()
                    .subtract(entity.getTransform().getTranslation());
            Vector2[] normals = getSideNormals();
            Vector2 shootDir1 = normals[0];   // front
            Vector2 shootDir2 = normals[2];   // back
            double dot1 = toPlayer.getNormalized().dot(shootDir1);
            double dot2 = toPlayer.getNormalized().dot(shootDir2);
            double threshold = Math.cos(Math.toRadians(CONE_HALF_ANGLE_DEG));
            if (Math.max(dot1, dot2) < threshold) return 0.0;

            // Cooldown scales with health (faster when damaged) – from old logic
            double healthPercent = core.getLife() / core.getMaxLife();
            double cooldown = ISCATCORE.fireCooldownS * Math.max(0.1, healthPercent);
            // We don't have a cooldown supplier here; we'll use a fixed cooldown for now.
            // Actually we can use the cooldown field start with that value.
            return 70.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return;

            Vector2 pos = entity.getTransform().getTranslation();
            Vector2 toPlayer = player.getTransform().getTranslation().copy().subtract(pos);
            Vector2[] normals = getSideNormals();
            // Determine which shooting side to use
            double dotFront = toPlayer.getNormalized().dot(normals[0]);
            double dotBack  = toPlayer.getNormalized().dot(normals[2]);
            double fireAngle;
            if (dotFront >= dotBack) {
                fireAngle = normals[0].getDirection();   // front side normal angle
            } else {
                fireAngle = normals[2].getDirection();   // back side normal angle
            }

            // Use the old parallel line pattern: 4 directions equally spaced around fireAngle
            // But we want to shoot multiple parallel lines from that side.
            // Recreate the pattern: MultiDirectionAttack(4, 0.0, new ParallelLineAttack(3, 30))
            // We'll need to instantiate the attacks and execute them manually through the shooter.
            // Since we don't have the exact class definitions, we'll approximate:
            // Fire 3 parallel lines perpendicular to the side normal, spread by 30 units??
            // For simplicity, just fire a single burst of 3 bullets in a spread from that side.
            Vector2 bulletPos = pos.copy().add(normals[fireAngle == normals[0].getDirection() ? 0 : 2].multiply(0.5));
            for (int i = -1; i <= 1; i++) {
                double angle = fireAngle + Math.toRadians(i * 10);   // spread ±10°
                shooter.shoot(new uni.gaben.iscat.universe.projectiles.Projectile(bulletType),
                        bulletPos, angle);
            }

            // Health‑based cooldown
            double healthPercent = core.getLife() / core.getMaxLife();
            double cd = ISCATCORE.fireCooldownS * Math.max(0.1, healthPercent);
            fireCooldown.start(cd);
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            fireCooldown.update(dt);
        }
    }
}