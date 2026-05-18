package uni.gaben.iscat.gamenex.universe.enemies.iscat_mother;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSpawnable;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileType;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Controller AI per IscatMother (boss).
 *
 * Comportamento:
 * - Mantiene una distanza ideale dal player, avvicinandosi o allontanandosi
 * - Spara a ventaglio di 3 proiettili quando il player è nel range di combattimento
 * - Al 50% HP spawna minioni (Iscat + Eater) intorno a sé
 * - Alla morte spawna un'orda finale di nemici
 */
public class IscatMotherController extends AiBehaviours<IscatMotherModel> {

    private final Shooter<IscatMotherModel> shooter;
    private final Projectile bulletTemplate;

    public IscatMotherController(IscatMotherModel mother) {
        super(mother);

        // Configura lo shooter per proiettili nemici
        shooter = new Shooter<>(mother);
        bulletTemplate = new Projectile();
        bulletTemplate.setType(ProjectileType.ENEMY_BULLET);

        // Alla morte, spawna l'orda finale
        mother.setOnDeath(this::spawnHorde);

        // --- COMPOSIZIONE BEHAVIORS ---

        // 1. SPAWN MINIONS (Priorità altissima)
        addBehavior(new AiBehavior() {
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

        // 2. COMBAT STATE (Priorità base, sempre attiva se il player è in vita)
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return universe.getPlayer() != null ? 50.0 : 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return;

                Vector2 myPos = aiEntity.getTransform().getTranslation();
                Vector2 playerPos = player.getTransform().getTranslation();
                Vector2 toPlayer = playerPos.copy().subtract(myPos);
                double dist = toPlayer.getMagnitude();

                maintainDistance(toPlayer, dist);
                rotateTo(toPlayer.getDirection(), dt);

                if (dist >= IscatMotherSettings.COMBAT_RANGE_MIN
                        && dist <= IscatMotherSettings.COMBAT_RANGE_MAX
                        && aiEntity.isFireReady()) {
                    shootBurst();
                }
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Tick cooldown interni
        aiEntity.update(dt);

        // Delega al Priority Queue (esegue il behavior col priority maggiore)
        super.aiUpdate(universeModel, dt);
    }

    // ─── Movimento ──────────────────────────────────────────────────────────

    private void maintainDistance(Vector2 toPlayer, double dist) {
        double idealDist = IscatMotherSettings.DISTANZA_IDEALE_M;

        if (dist < idealDist - IscatMotherSettings.DISTANZA_TOLLERANZA_VICINO) {
            // Troppo vicino → indietreggia
            Vector2 away = toPlayer.copy().getNormalized().multiply(-IscatMotherSettings.FORCE);
            aiEntity.applyForce(away);
        } else if (dist > idealDist + IscatMotherSettings.DISTANZA_TOLLERANZA_LONTANO) {
            // Troppo lontano → avvicinati
            Vector2 toward = toPlayer.copy().getNormalized().multiply(IscatMotherSettings.FORCE);
            aiEntity.applyForce(toward);
        }
    }

    // ─── Sparo ──────────────────────────────────────────────────────────────

    private void shootBurst() {
        double baseAngle = aiEntity.getTransform().getRotationAngle();
        double spreadRad = Math.toRadians(IscatMotherSettings.SPREAD_ANGLE_DEG);

        for (int i = -1; i <= 1; i++) {
            double angle = baseAngle + (i * spreadRad);
            Vector2 velocity = Vector2.create(
                    bulletTemplate.getTerminalVelocity(), angle);

            Projectile p = (Projectile) bulletTemplate.blueprint();
            p.setTransform(aiEntity.getTransform().copy());
            p.translate(Vector2.create(1, angle));
            p.setLinearVelocity(velocity);

            // Callback di collisione del proiettile
            p.setOnCollision(other -> {
                if (p.shouldRemove()) return;
                if (other instanceof uni.gaben.iscat.gamenex.lib.interfaces.model.Lifecycle target) {
                    target.deltaToLife(-p.getDamage());
                }
                p.kill();
                p.setShouldRemove(true);
            });

            UniverseSpawner.getInstance().spawnEntity(p);
        }

        IscatAudioManager.getInstance().playSFX("shoot");
        aiEntity.startFireCooldown();
    }

    // ─── Spawn minioni (al 50% HP) ──────────────────────────────────────────

    private void spawnMinions() {
        Vector2 myPos = aiEntity.getTransform().getTranslation();
        double radius = IscatMotherSettings.MINION_SPAWN_RADIUS;

        int total = IscatMotherSettings.MINION_ISCAT_COUNT + IscatMotherSettings.MINION_EATER_COUNT;
        for (int i = 0; i < total; i++) {
            double angle = Math.toRadians(i * (360.0 / total));
            double x = myPos.x + Math.cos(angle) * radius;
            double y = myPos.y + Math.sin(angle) * radius;

            double px = uni.gaben.iscat.gamenex.lib.utils.UU.mToPx(x);
            double py = uni.gaben.iscat.gamenex.lib.utils.UU.mToPx(y);

            if (i < IscatMotherSettings.MINION_ISCAT_COUNT) {
                UniverseSpawner.getInstance().spawn(UniverseSpawnable.ISCAT_MOB.name(), px, py);
            } else {
                UniverseSpawner.getInstance().spawn(UniverseSpawnable.EATER.name(), px, py);
            }
        }
        System.out.println("IscatMother ha chiamato rinforzi!");
    }

    // ─── Orda finale (alla morte) ───────────────────────────────────────────

    private void spawnHorde() {
        Vector2 center = aiEntity.getTransform().getTranslation();

        for (int i = 0; i < IscatMotherSettings.HORDE_SIZE; i++) {
            double angle = Math.toRadians(Math.random() * 360);
            double dist = IscatMotherSettings.HORDE_RADIUS
                    + Math.random() * IscatMotherSettings.HORDE_RADIUS_VARIANCE;

            double x = center.x + Math.cos(angle) * dist;
            double y = center.y + Math.sin(angle) * dist;

            double px = uni.gaben.iscat.gamenex.lib.utils.UU.mToPx(x);
            double py = uni.gaben.iscat.gamenex.lib.utils.UU.mToPx(y);

            if (i % 2 == 0) {
                UniverseSpawner.getInstance().spawn(UniverseSpawnable.HEARTH.name(), px, py);
            } else {
                UniverseSpawner.getInstance().spawn(UniverseSpawnable.EATER.name(), px, py);
            }
        }
        System.out.println("IscatMother ha dato inizio ad un'orda!");
    }

    // ─── Rotazione fluida ───────────────────────────────────────────────────

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current, current + diff,
                Math.min(IscatMotherSettings.ROTATION_SPEED * dt, 1.0));
        aiEntity.getTransform().setRotation(next);
    }
}