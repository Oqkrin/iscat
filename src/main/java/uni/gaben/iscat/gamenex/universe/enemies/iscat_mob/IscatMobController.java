package uni.gaben.iscat.gamenex.universe.enemies.iscat_mob;

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
        // Inizializza lo shooter associandolo a questo specifico mob
        shooter = new Shooter<>(iscat);

        // Configura il tipo di proiettile come proiettile nemico
        bulletTemplate = new Projectile();
        bulletTemplate.setType(ProjectileType.ENEMY_BULLET);
    }

    /**
     * Aggiornamento principale dell'IA eseguito ad ogni frame di gioco.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        // Controllo di sicurezza: se il mob non esiste o è stato rimosso, interrompi
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Aggiorna il timer del cooldown dello sparo
        fireCooldown.update(dt);

        // Recupera il giocatore principale dall'universo
        PlayerModel player = universeModel.getPlayer();

        // Calcola la distanza dal player (se il player non esiste, imposta distanza infinita)
        double distToPlayer = player == null ? Double.MAX_VALUE
                : aiEntity.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());

        // ── MACCHINA A STATI: TRANSIZIONI ─────────────────────────────────────
        if (player == null || distToPlayer > IscatMobSettings.DETECTION_RANGE) {
            state = State.WANDER; // Il giocatore è troppo lontano o morto -> Pattuglia
        } else if (distToPlayer <= IscatMobSettings.COMBAT_RANGE) {
            state = State.COMBAT; // Il giocatore è molto vicino -> Attacca
        } else {
            state = State.CHASE;  // Il giocatore è avvistato ma fuori tiro -> Insegui
        }

        // ── ESECUZIONE DELLO STATO CORRENTE ───────────────────────────────────
        switch (state) {
            case WANDER -> updateWander(dt);
            case CHASE  -> updateChase(player, dt);
            case COMBAT -> updateCombat(player, dt);
        }
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
        aiEntity.applyForce(wanderTarget.getNormalized().multiply(IscatMobSettings.FORCE));

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
        aiEntity.applyForce(toPlayer.getNormalized().multiply(IscatMobSettings.FORCE));
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
        if (dist < IscatMobSettings.PREFERRED_RANGE) {
            // Se il player è troppo vicino, applica una forza contraria per indietreggiare (retrofront)
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-IscatMobSettings.FORCE * 0.6));
        } else if (dist > IscatMobSettings.PREFERRED_RANGE * 1.2) {
            // Se si sta allontanando troppo, riavvicinati lentamente
            aiEntity.applyForce(toPlayer.getNormalized().multiply(IscatMobSettings.FORCE * 0.4));
        }

        // Resta sempre puntato verso il giocatore durante il combattimento
        rotateTo(toPlayer.getDirection(), dt);

        // LOGICA DI SPARO: Se il cooldown è terminato, fai fuoco
        if (!fireCooldown.isCoolingDown()) {
            shooter.shoot(bulletTemplate); // Spara il proiettile
            fireCooldown.start(IscatMobSettings.FIRE_COOLDOWN_S); // Avvia il timer di ricarica
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
        double next = Interpolator.lerp(current, current + diff, Math.min(IscatMobSettings.ROTATION_SPEED * dt, 1.0));

        // Applica la rotazione calcolata al corpo fisico
        aiEntity.getTransform().setRotation(next);
    }
}