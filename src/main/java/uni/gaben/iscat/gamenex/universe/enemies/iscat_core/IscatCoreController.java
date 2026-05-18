package uni.gaben.iscat.gamenex.universe.enemies.iscat_core;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.gamenex.universe.projectiles.ProjectileType;
import uni.gaben.iscat.gamenex.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

/**
 * Controller di Intelligenza Artificiale per l'IscatCore (Boss Quadrato).
 * Gestisce il movimento e l'attacco rotatorio a 4 lati con 3 proiettili per lato.
 */
public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    // ── STATI DELLA MACCHINA A STATI (FSM) ────────────────────────────────────
    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER;

    // ── PARAMETRI PER LO STATO WANDER ─────────────────────────────────────────
    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    // ── COMPONENTI PER LO STATO COMBAT ────────────────────────────────────────
    private final Shooter<IscatCoreModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    /**
     * Costruttore del Controller. Inizializza i componenti di sparo.
     */
    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat);

        shooter = new Shooter<>(iscat);

        bulletTemplate = new Projectile();
        bulletTemplate.setType(ProjectileType.ENEMY_BULLET);
    }

    /**
     * Aggiornamento principale dell'IA eseguito ad ogni frame di gioco.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (aiEntity == null || aiEntity.shouldRemove()) return;

        fireCooldown.update(dt);

        PlayerModel player = universeModel.getPlayer();

        double distToPlayer = player == null
                ? Double.MAX_VALUE
                : aiEntity.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());

        // ── MACCHINA A STATI (Calcolo transizioni in base alla distanza) ───────
        if (player == null || distToPlayer > IscatCoreSettings.DETECTION_RANGE) {
            state = State.WANDER;
        } else if (distToPlayer <= IscatCoreSettings.COMBAT_RANGE) {
            state = State.COMBAT;
        } else {
            state = State.CHASE;
        }

        // ── ESECUZIONE DELLO STATO CORRENTE ───────────────────────────────────
        switch (state) {
            case WANDER -> updateWander(dt);
            case CHASE  -> updateChase(player, dt);
            case COMBAT -> updateCombat(player, dt);
        }
    }

    /**
     * Stato WANDER: movimento casuale di pattugliamento.
     */
    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();

            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }

        aiEntity.applyForce(
                wanderTarget.getNormalized()
                        .multiply(IscatCoreSettings.FORCE)
        );

        if (aiEntity.contains(wanderTarget)) {
            wanderTarget = null;
        }
    }

    /**
     * Stato CHASE: Insegue il giocatore se si trova nel raggio di ingaggio.
     */
    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);

        aiEntity.applyForce(
                toPlayer.getNormalized()
                        .multiply(IscatCoreSettings.FORCE)
        );
    }

    /**
     * Stato COMBAT: Gestisce il balletto della distanza e la logica di sparo rotatoria.
     */
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        // Mantiene il posizionamento ottimale rispetto al player
        if (dist < IscatCoreSettings.PREFERRED_RANGE) {
            aiEntity.applyForce(
                    toPlayer.getNormalized()
                            .multiply(-IscatCoreSettings.FORCE * 0.6)
            );
        } else if (dist > IscatCoreSettings.PREFERRED_RANGE * 1.2) {
            aiEntity.applyForce(
                    toPlayer.getNormalized()
                            .multiply(IscatCoreSettings.FORCE * 0.4)
            );
        }

        // ── LOGICA DI SPARO CON ROTAZIONE DI 45° ──────────────────────────────
        if (!fireCooldown.isCoolingDown()) {

            // 1. Calcoliamo la nuova rotazione fisica del quadrato sommandoci 45 gradi (PI / 4 radianti)
            double newCoreRotation = aiEntity.getTransform().getRotationAngle() + (Math.PI / 4.0);

            // 2. Applichiamo immediatamente la nuova rotazione al modello
            aiEntity.getTransform().setRotation(newCoreRotation);
            aiEntity.setAngularVelocity(0.0); // Azzera l'inerzia della fisica di dyn4j

            // 3. Ciclo per i 4 lati del quadrato (ogni lato è sfasato di 90° ovvero PI / 2)
            for (int side = 0; side < 4; side++) {
                double sideAngle = newCoreRotation + (side * (Math.PI / 2.0));

                // 4. Per ogni lato, spara 3 proiettili (b varia tra -1, 0, 1) creando la sventagliata
                for (int b = -1; b <= 1; b++) {
                    // Distanza angolare di sventagliata tra i 3 colpi dello stesso lato (es. 10 gradi)
                    double spreadOffset = Math.toRadians(10);
                    double finalBulletAngle = sideAngle + (b * spreadOffset);

                    // Forziamo momentaneamente la rotazione dell'entità sull'angolo del singolo proiettile
                    // in modo che la classe Shooter lo legga e generi la traiettoria corretta
                    aiEntity.getTransform().setRotation(finalBulletAngle);

                    // Esegue il comando di sparo nativo della tua libreria
                    shooter.shoot(bulletTemplate);
                }
            }

            // 5. Ripristiniamo la rotazione strutturale corretta a 45 gradi impostata all'inizio.
            // Così i colpi sono partiti inclinati e il quadrato rimane fermo a 45° fino al prossimo sparo!
            aiEntity.getTransform().setRotation(newCoreRotation);

            // Avvia il timer di ricarica dell'arma
            fireCooldown.start(IscatCoreSettings.FIRE_COOLDOWN_S);
        }
    }

    /**
     * Calcola il vettore direzione verso il player.
     */
    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    /**
     * Rotazione fluida verso un angolo obiettivo (Attualmente non usata dagli stati).
     */
    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(IscatCoreSettings.ROTATION_SPEED * dt, 1.0)
        );

        aiEntity.getTransform().setRotation(next);
    }
}