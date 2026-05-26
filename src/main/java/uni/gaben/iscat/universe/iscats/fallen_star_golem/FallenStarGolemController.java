package uni.gaben.iscat.universe.iscats.fallen_star_golem;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

import static uni.gaben.iscat.universe.iscats.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

/**
 * Controller per il FallenStarGolem.
 * Utilizza un sistema a comportamenti compositi (AiBehavior) con priorità dinamiche.
 */
public class FallenStarGolemController extends AiBehaviours<FallenStarGolemModel> {

    // ── PARAMETRI PER LO STATO WANDER ─────────────────────────────────────────
    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    // ── COMPONENTI DI SPARO BASE ──────────────────────────────────────────────
    private final Shooter<FallenStarGolemModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    // ── PARAMETRI PER L'ATTACCO A SPIRALE TEMPORIZZATO ────────────────────────
    private int spiralBulletsLeft = 0;
    private double spiralTimer = 0.0;
    private double currentSpiralAngle = 0.0;
    private static final double SPIRAL_DELAY = 0.05; // 50ms di ritardo tra ogni proiettile
    private static final int NUM_SPIRAL_BULLETS = 12;
    private static final double SPIRAL_ANGLE_STEP = (2.0 * Math.PI) / NUM_SPIRAL_BULLETS;

    /**
     * Costruttore: Inizializza i componenti e registra i comportamenti dell'IA.
     */
    public FallenStarGolemController(FallenStarGolemModel golem) {
        super(golem);

        this.shooter = new Shooter<>(golem);
        this.bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // ── REGISTRAZIONE DEI BEHAVIORS COMPOSITI ──

        // 1. PATTUGLIAMENTO (Wander) - Comportamento di fallback
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateWander(dt);
            }
        });

        // 2. INSEGUIMENTO (Chase) - Si attiva quando il player è visibile ma fuori portata di tiro
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist > FALLENSTARGOLEM.combatRange && dist <= FALLENSTARGOLEM.detectionRange) {
                    return 50.0;
                }
                return 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateChase(universe.getPlayer(), dt);
            }
        });

        // 3. COMBATTIMENTO (Combat) - Massima priorità quando il player è vicino
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist <= FALLENSTARGOLEM.combatRange) {
                    return 80.0;
                }
                return 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateCombat(universe.getPlayer(), dt);
            }
        });
    }

    /**
     * Aggiornamento logico eseguito ad ogni frame.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Aggiorna il cooldown globale di fuoco
        fireCooldown.update(dt);

        // Gestione dello spawning sequenziale della spirale di proiettili
        if (spiralBulletsLeft > 0) {
            spiralTimer += dt;
            if (spiralTimer >= SPIRAL_DELAY) {
                spiralTimer -= SPIRAL_DELAY;
                spawnSpiralBullet();
                spiralBulletsLeft--;
            }
        }

        // Passa il controllo al motore dei comportamenti (esegue il behavior a priorità più alta)
        super.aiUpdate(universeModel, dt);
    }

    /**
     * Movimento casuale di perlustrazione.
     */
    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }

        // Rimuovi il commento sotto se desideri che il Golem ruoti fisicamente mentre vaga
        // rotateTo(wanderTarget.getDirection(), dt);

        aiEntity.applyForce(wanderTarget.getNormalized().multiply(FALLENSTARGOLEM.force));

        if (aiEntity.contains(wanderTarget)) {
            wanderTarget = null;
        }
    }

    /**
     * Avvicinamento dritto verso la posizione del Player.
     */
    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);

        // rotateTo(toPlayer.getDirection(), dt);

        aiEntity.applyForce(toPlayer.getNormalized().multiply(FALLENSTARGOLEM.force));
    }

    /**
     * Posizionamento tattico di kiting e attivazione della raffica a spirale.
     */
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        // Mantenimento della distanza di sicurezza dal bersaglio
        if (dist < FALLENSTARGOLEM.preferredRange) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-FALLENSTARGOLEM.force * 0.6));
        } else if (dist > FALLENSTARGOLEM.preferredRange * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(FALLENSTARGOLEM.force * 0.4));
        }

        // Ruota costantemente il Golem verso il giocatore durante il combattimento
        rotateTo(toPlayer.getDirection(), dt);

        // Innesco dell'attacco speciale se pronto
        if (!fireCooldown.isCoolingDown()) {
            startSpiralBurst();
            fireCooldown.start(FALLENSTARGOLEM.fireCooldownS);
        }
    }

    /**
     * Inizializza i parametri per la sequenza di colpi a spirale.
     */
    private void startSpiralBurst() {
        spiralBulletsLeft = NUM_SPIRAL_BULLETS;
        spiralTimer = 0.0;
        currentSpiralAngle = rand.nextDouble() * Math.PI * 2.0; // Angolo iniziale randomico per imprevedibilità
    }

    /**
     * Istanzia, configura e lancia un singolo proiettile della traiettoria curva.
     */
    private void spawnSpiralBullet() {
        shooter.shoot(bulletTemplate, currentSpiralAngle);
        currentSpiralAngle += SPIRAL_ANGLE_STEP;
    }

    /**
     * Calcola il vettore distanza tra l'entità corrente e il Player.
     */
    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    /**
     * Interpolazione sferica fluida (Lerp) per evitare rotazioni istantanee innaturali.
     */
    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0); // Resetta forze angolari parassite indotte dal motore fisico

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        // Normalizzazione dell'angolo nell'intervallo [-PI, PI] per evitare giri completi errati
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI)  diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(FALLENSTARGOLEM.rotationSpeed * dt, 1.0)
        );

        aiEntity.getTransform().setRotation(next);
    }
}