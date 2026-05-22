package uni.gaben.iscat.game.universe.enemies.fake_iscat;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER;

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<FakeIscatModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    // ── GESTIONE DELLARAFFICA (BURST) DEL FAKE ISCAT ──────────────────────────
    private int currentAttackType = 0;       // 0: 5 Singoli, 1: 3x3 Arco, 2: 15 Radiali
    private int burstShotsRemaining = 0;     // Quanti colpi mancano alla raffica corrente
    private double burstTargetAngle = 0;     // Angolo memorizzato a inizio raffica
    private final Cooldown burstDelay = new Cooldown(); // Timer tra un colpo della raffica e il successivo

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat);
        shooter = new Shooter<>(iscat);

        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Aggiorna sia il cooldown principale che quello di raffica
        fireCooldown.update(dt);
        burstDelay.update(dt);

        PlayerModel player = universeModel.getPlayer();

        double distToPlayer = player == null
                ? Double.MAX_VALUE
                : aiEntity.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());

        if (player == null || distToPlayer > FakeIscatSettings.DETECTION_RANGE) {
            state = State.WANDER;
        } else if (distToPlayer <= FakeIscatSettings.COMBAT_RANGE) {
            state = State.COMBAT;
        } else {
            state = State.CHASE;
        }

        switch (state) {
            case WANDER -> updateWander(dt);
            case CHASE -> updateChase(player, dt);
            case COMBAT -> updateCombat(player, dt);
        }
    }

    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }
        aiEntity.applyForce(wanderTarget.getNormalized().multiply(FakeIscatSettings.FORCE));
        if (aiEntity.contains(wanderTarget)) wanderTarget = null;
    }

    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        aiEntity.applyForce(toPlayer.getNormalized().multiply(FakeIscatSettings.FORCE));
    }

    // ── COMBAT CON MULTI-PATTERN CASUALI ──────────────────────────────────────
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        if (dist < FakeIscatSettings.PREFERRED_RANGE) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-FakeIscatSettings.FORCE * 0.6));
        } else if (dist > FakeIscatSettings.PREFERRED_RANGE * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(FakeIscatSettings.FORCE * 0.4));
        }

        double angleToPlayer = toPlayer.getDirection();

        // 1. GESTIONE DELLE RAFFICE IN CORSO (Se ci sono colpi residui da sparare)
        if (burstShotsRemaining > 0 && !burstDelay.isCoolingDown()) {
            if (currentAttackType == 0) {
                // Pattern 1: Spara un singolo proiettile della scia di 5
                executeSingleShot(burstTargetAngle);
                burstShotsRemaining--;
                burstDelay.start(0.12); // Aspetta 120 millisecondi prima del prossimo
            }
            else if (currentAttackType == 1) {
                // Pattern 2: Spara una tripletta ad arco delle 3 totali
                executeArcShot(burstTargetAngle);
                burstShotsRemaining--;
                burstDelay.start(0.18); // Aspetta 180 millisecondi prima della prossima sventagliata
            }
        }

        // 2. ATTIVAZIONE NUOVO ATTACCO (Solo se il cooldown globale è scaduto e non ci sono raffiche attive)
        if (!fireCooldown.isCoolingDown() && burstShotsRemaining == 0) {

            // Sceglie a caso un attacco tra i 3 disponibili (0, 1 o 2)
            currentAttackType = rand.nextInt(3);

            switch (currentAttackType) {
                case 0 -> {
                    // Configura la raffica da 5 colpi in linea retta verso il player
                    burstShotsRemaining = 5;
                    burstTargetAngle = angleToPlayer;
                    burstDelay.start(0.0); // Parte istantaneamente il primo colpo
                }
                case 1 -> {
                    // Configura le 3 sventagliate ad arco consecutive verso il player
                    burstShotsRemaining = 3;
                    burstTargetAngle = angleToPlayer;
                    burstDelay.start(0.0); // Parte istantaneamente la prima
                }
                case 2 -> {
                    // Pattern 3: Esplosione radiale immediata a 15 direzioni (stile Fallen Star Golem)
                    executeRadialNova(15);
                }
            }

            // Fa ripartire il cooldown generale prima del prossimo attacco del boss
            fireCooldown.start(FakeIscatSettings.FIRE_COOLDOWN_S);
        }
    }

    // ── FUNZIONI COMPLEMENTARI DI SPARO (Senza alterare l'angolo grafico) ────

    /** Spara un singolo colpo mirato */
    private void executeSingleShot(double angle) {
        double originalAngle = aiEntity.getTransform().getRotationAngle();
        aiEntity.getTransform().setRotation(angle);
        shooter.shoot(bulletTemplate);
        aiEntity.getTransform().setRotation(originalAngle);
    }

    /** Spara 3 colpi ad arco (Ventaglio di circa 30 gradi totali) */
    private void executeArcShot(double centerAngle) {
        double originalAngle = aiEntity.getTransform().getRotationAngle();
        double spread = Math.toRadians(15); // Inclinazione dei colpi laterali

        for (int i = -1; i <= 1; i++) {
            aiEntity.getTransform().setRotation(centerAngle + (i * spread));
            shooter.shoot(bulletTemplate);
        }
        aiEntity.getTransform().setRotation(originalAngle);
    }

    /** Spara un'onda radiale simultanea divisa simmetricamente */
    private void executeRadialNova(int totalDirections) {
        double originalAngle = aiEntity.getTransform().getRotationAngle();
        double angleIncrement = (2 * Math.PI) / totalDirections;

        for (int i = 0; i < totalDirections; i++) {
            aiEntity.getTransform().setRotation(i * angleIncrement);
            shooter.shoot(bulletTemplate);
        }
        aiEntity.getTransform().setRotation(originalAngle);
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);
        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(FakeIscatSettings.ROTATION_SPEED * dt, 1.0)
        );
        aiEntity.getTransform().setRotation(next);
    }
}