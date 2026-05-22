package uni.gaben.iscat.game.universe.enemies.iscat_core;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.game.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

/**
 * Controller di Intelligenza Artificiale per l'IscatCore (Boss Quadrato).
 * Gestisce il movimento e l'attacco rotatorio tramite il sistema a comportamenti compositi (AiBehavior).
 */
public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    // ── PARAMETRI PER LO STATO WANDER ─────────────────────────────────────────
    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    // ── COMPONENTI DI SPARO ───────────────────────────────────────────────────
    private Shooter<IscatCoreModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    // ── ROTAZIONE AUTONOMA A SCATTI DI 45° ────────────────────────────────────
    private double targetRotationAngle = 0.0;
    private double rotationTimer = 0.0;
    private static double ROTATION_INTERVAL = 10.0; // Secondi per intervallo
    private int firedBulletsCount = 0;

    /**
     * Costruttore del Controller. Inizializza i componenti e registra i comportamenti.
     */
    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat);

        this.shooter = new Shooter<>(iscat);
        this.bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // ── REGISTRAZIONE COMPORTAMENTI COMPOSITI (Sostituisce la vecchia FSM manuale) ──

        // 1. COMPORTAMENTO: Wander (Pattugliamento Casuale - Default)
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0; // Base priority, sempre attivo se non ci sono bersagli
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateWander(dt);
            }
        });

        // 2. COMPORTAMENTO: Chase (Inseguimento)
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist > IscatCoreSettings.COMBAT_RANGE && dist <= IscatCoreSettings.DETECTION_RANGE) {
                    return 50.0;
                }
                return 0.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateChase(universe.getPlayer(), dt);
            }
        });

        // 3. COMPORTAMENTO: Combat (Mantenimento Distanza e Sparo)
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist <= IscatCoreSettings.COMBAT_RANGE) {
                    return 80.0; // Massima priorità quando il player è vicino
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
     * Aggiornamento logico principale ad ogni frame del motore di gioco.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Aggiorna il timer globale dei proiettili
        fireCooldown.update(dt);

        // Gestione rotazione autonoma a scatti di 45 gradi
        rotationTimer += dt;
        if (rotationTimer >= ROTATION_INTERVAL || firedBulletsCount == 10) {
            rotationTimer -= ROTATION_INTERVAL;
            targetRotationAngle += Math.PI / 4.0; // +45 gradi

            // Limitazione numerica entro il range [0, 2PI]
            if (targetRotationAngle >= Math.PI * 2.0) {
                targetRotationAngle -= Math.PI * 2.0;
            }
        }

        // Interpolazione continua e fluida verso l'angolo target calcolato
        rotateTo(targetRotationAngle, dt);

        // Esegue i comportamenti registrati nel costruttore in base alla priorità corrente
        super.aiUpdate(universeModel, dt);
    }

    /**
     * Logica di pattugliamento casuale.
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
     * Logica di inseguimento dinamico verso il giocatore.
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
     * Logica di combattimento: gestione delle distanze di sicurezza e sventagliate di colpi.
     */
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        // Algoritmo di posizionamento ottimale (Kiting)
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

        // Gestione dello sparo condizionato dal Cooldown Dinamico (Spara più velocemente se ferito)
        if (!fireCooldown.isCoolingDown()) {
            if (firedBulletsCount >= 10) firedBulletsCount = 0;
            shootCoreBurst();
            firedBulletsCount++;

            double healthPercent =  aiEntity.getLife() / aiEntity.getMaxLife();
            double dynamicCooldown = IscatCoreSettings.FIRE_COOLDOWN_S * healthPercent;

            fireCooldown.start(dynamicCooldown);
        }
    }

    /**
     * Genera l'attacco coreografico: 4 direzioni cardinali basate sulla rotazione attuale,
     * ognuna contenente 3 proiettili paralleli distanziati.
     */
    private void shootCoreBurst() {
        double offsetSpacingM = UU.pxToM(24.0);

        double rot = aiEntity.getTransform().getRotationAngle();
        double[] directions = { rot, rot + Math.PI / 2.0, rot + Math.PI, rot + 3.0 * Math.PI / 2.0 };

        for (double rad : directions) {
            Vector2 dir = new Vector2(Math.cos(rad), Math.sin(rad));
            Vector2 perp = new Vector2(-dir.y, dir.x); // Vettore perpendicolare per il distanziamento lineare

            for (int i = -1; i <= 1; i++) {
                Vector2 bulletTranslation = aiEntity.getTransform().getTranslation().copy().add(perp.copy().multiply(i * offsetSpacingM));
                shooter.shoot(aiEntity.getProjectile(), bulletTranslation, rad);
            }
        }
    }

    /**
     * Restituisce la distanza vettoriale dall'entità al Player.
     */
    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    /**
     * Esegue una rotazione fluida e smorzata verso un angolo specifico (Lerp).
     */
    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0); // Previene drifting fisici indesiderati da dyn4j

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        // Normalizzazione dell'angolo per evitare calcoli di rotazione su giri completi inutili
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI)  diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(IscatCoreSettings.ROTATION_SPEED * dt, 1.0)
        );

        aiEntity.getTransform().setRotation(next);
    }
}