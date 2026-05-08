package uni.gaben.iscat.game.player;

import uni.gaben.iscat.game.projectile.ProjectileModel;
import uni.gaben.iscat.game.interfaces.Collidable;
import uni.gaben.iscat.game.physics.Vec2;
import uni.gaben.iscat.game.GameSettings;
import uni.gaben.iscat.game.entities.LivingEntityModel;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;
import java.util.function.BiConsumer;

/**
 * Nave del giocatore.
 * La spinta viene applicata dal controller ogni tick via {@link #applyForce}.
 * Lo scatto applica un impulso istantaneo e per {@code DURATA_SCATTO_TICK} tick
 * usa attrito ridotto e cap velocità più alto — così la distanza percorsa è reale.
 */
public class PlayerModel extends LivingEntityModel implements Collidable {

    /** Cooldown per lo scatto. */
    private final Cooldown cooldownScatto = new Cooldown();
    /** Cooldown per lo sparo. */
    private final Cooldown cooldownFuoco = new Cooldown();

    private boolean fuocoRichiesto = false;
    // Callback per far sapere al mondo che abbiamo sparato
    // Il primo parametro è la posizione, il secondo è la direzione/velocità
    private BiConsumer<Vec2, Vec2> onSparo;

    /** Timer per la fase scatto attiva (drag ridotto). */
    private final Cooldown faseScatto = new Cooldown();

    /** true se lo scatto è stato richiesto questo tick. */
    private boolean scattoRichiesto = false;
    private Runnable onSparoSound;

    private Random rand = new Random();

    public PlayerModel(double startX, double startY) {
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
        
        // Usa l'integrazione base di PhysicalEntityModel (con drag, cap, dead-zone)
        super.integrate(dt);

        // Decrementa timer
        faseScatto.tick();
        cooldownScatto.tick();
        cooldownFuoco.tick(); // timer attacco
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
        // Usa l'implementazione di LivingEntityModel
        return super.getColliderCenter();
    }
    
    @Override public void   onCollision(Collidable other) {
        // La fisica della collisione è gestita dall'altra entità
        // (per evitare di applicare la fisica due volte)
    }

    // --- logica attacco ---
    public void setOnSparo(BiConsumer<Vec2, Vec2> callback) {
        this.onSparo = callback;
    }
    public void setOnSparoSound(Runnable callback) {
        this.onSparoSound = callback;
    }

    /** Segnala che il giocatore vuole sparare. */
    public void richiestaFuoco() {
        this.fuocoRichiesto = true;
    }

    /** Esegue l'attacco **/
    public void elaboraFuoco() {
        if (!fuocoRichiesto) return;
        fuocoRichiesto = false;
        if (!cooldownFuoco.isReady()) return;

        // Esegui la LOGICA FISICA (quella del GameModel)
        if (onSparo != null) {
            double rad = Math.toRadians(directionAngle);
            Vec2 spawnPos = new Vec2(x + spriteSize / 2, y + spriteSize / 2);
            Vec2 bulletVel = new Vec2(
                    Math.cos(rad) * PlayerSettings.VELOCITA_PROIETTILE,
                    Math.sin(rad) * PlayerSettings.VELOCITA_PROIETTILE
            );
            onSparo.accept(spawnPos, bulletVel);
        }

        // Esegui la LOGICA AUDIO (quella del GameController)
        if (onSparoSound != null) {
            onSparoSound.run();
        }

        cooldownFuoco.set(PlayerSettings.COOLDOWN_FUOCO_TICK);
    }


    // --- Alive ---

    @Override public void die() { /* TODO: animazione morte */ }
}
