package uni.gaben.iscat.iscat_game.universe.iscats.mob;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.utils.UU;

import java.util.Random;

import static uni.gaben.iscat.iscat_game.universe.iscats.mob.IscatMobSettings.ISCATMOB;

/**
 * Controller di Intelligenza Artificiale per l'IscatMob.
 * Gestisce la macchina a stati (Wander, Chase, Combat), il movimento e lo sparo.
 */
public class IscatMobController extends AiBehaviours<IscatMobModel> {

    // ── STATI DELLA MACCHINA A STATI (FSM) ────────────────────────────────────
    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER; // Stato iniziale predefinito

    // ── PARAMETRI PER LO STATO WANDER (PATTUGLIAMENTO) ────────────────────────
    private Vector2 wanderTarget = null; // Punto di destinazione casuale attuale
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1; // Distanze min/max del movimento casuale

    // ── COMPONENTI PER LO STATO COMBAT (COMBATTIMENTO) ────────────────────────
    private final Shooter<IscatMobModel> shooter;   // Gestore dello sparo (Controller)
    private final Projectile bulletTemplate;         // Il "blueprint" del proiettile da sparare
    private final Cooldown fireCooldown = new Cooldown(); // Gestore del tempo di ricarica dello sparo

    /**
     * Costruttore del Controller. Inizializza i componenti di sparo.
     */
    public IscatMobController(IscatMobModel iscat) {
        super(iscat);
        shooter = new Shooter<>(iscat);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // --- COMPOSIZIONE BEHAVIORS ---

        // Comportamento parallelo di separazione per evitare clumping (raggio 32px, forza bilanciata)
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), ISCATMOB.force * 0.8));

        this.addBehavior(new DodgeProjectileBehavior(ISCATMOB.force * 1.5, 2.0));

        // 1. WANDER: Priorità base (10.0). Se nient'altro si attiva, pattuglia.
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

        // 2. CHASE: Priorità media (50.0). Si attiva se il player è nel DETECTION_RANGE ma fuori dal COMBAT_RANGE.
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist > ISCATMOB.combatRange && dist <= ISCATMOB.detectionRange) return 50.0;
                return 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateChase(universe.getPlayer(), dt);
            }
        });

        // 3. COMBAT: Priorità alta (80.0). Si attiva se il player entra nel COMBAT_RANGE.
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist <= ISCATMOB.combatRange) return 80.0;
                return 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateCombat(universe.getPlayer(), dt);
            }
        });
    }

    /**
     * Aggiornamento principale dell'IA eseguito ad ogni frame di gioco.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;
        
        fireCooldown.update(dt);
        
        // Delega la scelta dello stato al sistema a priorità (PriorityQueue in AiBehaviours)
        super.aiUpdate(universeModel, dt);
    }

    /**
     * Stato WANDER: Il mob si muove a caso cambiando direzione periodicamente.
     */
    private void updateWander(double dt) {
        // Se non ha una destinazione, ne calcola una nuova basandosi sulla rotazione attuale
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI));
        }

        // Ruota verso il punto di pattuglia casuale
        rotateTo(wanderTarget.getDirection(), dt);

        // Applica una forza fisica per spingere il mob verso la direzione del target
        aiEntity.applyForce(wanderTarget.getNormalized().multiply(ISCATMOB.force));

        // Se il mob ha raggiunto il punto (ci si trova sopra), resetta il target per calcolarne uno nuovo
        if (aiEntity.contains(wanderTarget)) wanderTarget = null;
    }

    /**
     * Stato CHASE: Il mob punta dritto verso il giocatore per accorciare le distanze.
     */
    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null; // Annulla eventuali target di pattugliamento

        // Calcola il vettore direzione verso il giocatore
        Vector2 toPlayer = directionToPlayer(player);

        // Ruota il muso del mob verso il giocatore
        rotateTo(toPlayer.getDirection(), dt);

        // Spinge fisicamente il mob in avanti verso il giocatore
        aiEntity.applyForce(toPlayer.getNormalized().multiply(ISCATMOB.force));
    }

    /**
     * Stato COMBAT: Mantiene una distanza di sicurezza ottimale dal giocatore e spara.
     */
    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;

        // Calcola distanza e direzione verso il giocatore
        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        // GESTIONE DELLA DISTANZA DI SICUREZZA (Evita che il player si avvicini troppo)
        if (dist < ISCATMOB.preferredRange) {
            // Se il player è troppo vicino, applica una forza contraria per indietreggiare (retrofront)
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-ISCATMOB.force * 0.6));
        } else if (dist > ISCATMOB.preferredRange * 1.2) {
            // Se si sta allontanando troppo, riavvicinati lentamente
            aiEntity.applyForce(toPlayer.getNormalized().multiply(ISCATMOB.force * 0.4));
        }

        // Resta sempre puntato verso il giocatore durante il combattimento
        rotateTo(toPlayer.getDirection(), dt);

        // LOGICA DI SPARO: Se il cooldown è terminato, fai fuoco
        if (!fireCooldown.isCoolingDown()) {
            shooter.shoot(bulletTemplate); // Spara il proiettile
            fireCooldown.start(ISCATMOB.fireCooldownS); // Avvia il timer di ricarica
        }
    }

    /**
     * Calcola la distanza vettoriale tra il centro del Mob e il centro del Player.
     */
    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    /**
     * Gestisce la rotazione fluida (Lerp) verso un angolo obiettivo.
     * FIX: Blocca la velocità angolare fisica per evitare conflitti con la fisica di dyn4j.
     */
    private void rotateTo(double targetAngle, double dt) {
        // Forza l'azzeramento della rotazione fisica rotante indotta da urti/collisioni
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        // Normalizzazione dell'angolo tra -PI e +PI per evitare che faccia giri a 360° completi inutili
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        // Calcola l'angolo intermedio tramite interpolazione lineare (movimento fluido dell'asse)
        double next = Interpolator.lerp(current, current + diff, Math.min(ISCATMOB.rotationSpeed * dt, 1.0));

        // Applica la rotazione calcolata al corpo fisico
        aiEntity.getTransform().setRotation(next);
    }
}