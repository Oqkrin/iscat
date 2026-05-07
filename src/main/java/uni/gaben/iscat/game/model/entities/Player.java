package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.physics.Vec2;
import uni.gaben.iscat.game.model.GameSettings;
import uni.gaben.iscat.game.model.settings.PlayerSettings;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;

/**
 * Nave del giocatore.
 * La spinta viene applicata dal controller ogni tick via {@link #applyForce}.
 * Lo scatto applica un impulso istantaneo e per {@code DURATA_SCATTO_TICK} tick
 * usa attrito ridotto e cap velocità più alto — così la distanza percorsa è reale.
 */
public class Player extends LivingEntity implements Collidable {

    /** Cooldown per lo scatto. */
    private final Cooldown cooldownScatto = new Cooldown();

    /** Timer per la fase scatto attiva (drag ridotto). */
    private final Cooldown faseScatto = new Cooldown();

    /** true se lo scatto è stato richiesto questo tick. */
    private boolean scattoRichiesto = false;
    private Random rand = new Random();

    public Player(double startX, double startY) {
        this.x     = startX;
        this.y     = startY;
        this.hp    = PlayerSettings.HP_INIZIALE;
        this.maxHp = PlayerSettings.HP_MASSIMO;
        this.mass  = PlayerSettings.MASSA;
        this.name  = "Player";
        this.spriteSize = PlayerSettings.DIMENSIONE_SPRITE;
        this.drag     = PlayerSettings.ATTRITO;
        this.maxSpeed = PlayerSettings.VELOCITA_MAX;
        this.deadZone = PlayerSettings.ZONA_MORTA;
    }

    // --- fisica ---

    @Override
    public void integrate(double dt) {
        // Modifica parametri fisici durante lo scatto
        boolean inScatto = faseScatto.isActive();
        
        if (inScatto) {
            this.drag = PlayerSettings.ATTRITO_SCATTO;
            this.maxSpeed = PlayerSettings.VELOCITA_MAX_SCATTO;
        } else {
            this.drag = PlayerSettings.ATTRITO;
            this.maxSpeed = PlayerSettings.VELOCITA_MAX;
        }
        
        // Usa l'integrazione base di PhysicalEntity (con drag, cap, dead-zone)
        super.integrate(dt);

        // Decrementa timer
        faseScatto.tick();
        cooldownScatto.tick();
    }

    // --- scatto ---

    /** Segnala che il giocatore vuole scattare questo tick. */
    public void richiestaScatto() { scattoRichiesto = true; }

    /** Callback opzionale chiamato quando lo scatto viene eseguito (es. per riprodurre audio). */
    private Runnable onScatto;

    /** Imposta il callback da chiamare quando lo scatto viene eseguito. */
    public void setOnScatto(Runnable callback) { this.onScatto = callback; }

    /**
     * Esegue lo scatto se richiesto e il cooldown è scaduto.
     * Chiamato dal controller dopo aver aggiornato {@link #directionAngle}.
     */
    public void elaboraScatto() {
        if (!scattoRichiesto) return;
        scattoRichiesto = false;
        if (!cooldownScatto.isReady()) return;

        if (onScatto != null) onScatto.run();

        double rad = Math.toRadians(directionAngle);
        velocity = velocity.add(
                Math.cos(rad) * PlayerSettings.IMPULSO_SCATTO,
                Math.sin(rad) * PlayerSettings.IMPULSO_SCATTO
        );

        faseScatto.set(PlayerSettings.DURATA_SCATTO_TICK);
        cooldownScatto.set(PlayerSettings.COOLDOWN_SCATTO_TICK);
    }

    /** {@code true} se lo scatto è disponibile. */
    public boolean isScattoDisponibile() { return cooldownScatto.isReady(); }

    /** {@code true} se la fase scatto è attiva (drag ridotto). */
    public boolean isInScatto() { return faseScatto.isActive(); }

    // --- Collidable ---

    @Override public double getCollisionRadius() { return PlayerSettings.RAGGIO_COLLISIONE; }
    
    @Override 
    public Vec2 getColliderCenter() {
        // Usa l'implementazione di LivingEntity
        return super.getColliderCenter();
    }
    
    @Override public void   onCollision(Collidable other) {
        // La fisica della collisione è gestita dall'altra entità
        // (per evitare di applicare la fisica due volte)
    }

    // --- Alive ---

    @Override public void die() { /* TODO: animazione morte */ }
}
