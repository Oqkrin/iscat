package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Collidable;
import uni.gaben.iscat.game.model.physics.Vec2;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.model.GameSettings;

import java.util.Random;

/**
 * Nave del giocatore.
 * La spinta viene applicata dal controller ogni tick via {@link #applyForce}.
 * Lo scatto applica un impulso istantaneo e per {@code DURATA_SCATTO_TICK} tick
 * usa attrito ridotto e cap velocità più alto — così la distanza percorsa è reale.
 */
public class Player extends LivingEntity implements Collidable {

    /** Tick rimanenti al cooldown scatto. */
    private int cooldownScatto = 0;

    /** Tick rimanenti nella fase scatto attiva (drag ridotto). */
    private int faseScatto = 0;

    /** true se lo scatto è stato richiesto questo tick. */
    private boolean scattoRichiesto = false;
    private Random rand = new Random();

    public Player(double startX, double startY) {
        this.x     = startX;
        this.y     = startY;
        this.hp    = 100;
        this.maxHp = 100;
        this.mass  = GameSettings.MASSA_GIOCATORE;
        this.name  = "Player";
    }

    // --- fisica ---

    @Override
    public void integrate(double dt) {
        super.integrate(dt);

        boolean inScatto = faseScatto > 0;

        // attrito: ridotto durante lo scatto per mantenere la velocità più a lungo
        double attrito = inScatto ? GameSettings.ATTRITO_SCATTO : GameSettings.ATTRITO;
        velocity = velocity.scale(attrito);

        // cap velocità: più alto durante lo scatto
        double vMax = inScatto ? GameSettings.VELOCITA_MAX_SCATTO : GameSettings.VELOCITA_MAX;
        double speed = velocity.magnitude();
        if (speed > vMax) velocity = velocity.scale(vMax / speed);

        // dead-zone
        if (Math.abs(velocity.x) < 0.01 && Math.abs(velocity.y) < 0.01) velocity = Vec2.ZERO;

        // decrementa timer
        if (faseScatto   > 0) faseScatto--;
        if (cooldownScatto > 0) cooldownScatto--;
    }

    // --- scatto ---

    /** Segnala che il giocatore vuole scattare questo tick. */
    public void richiestaScatto() { scattoRichiesto = true; }

    /**
     * Esegue lo scatto se richiesto e il cooldown è scaduto.
     * Chiamato dal controller dopo aver aggiornato {@link #directionAngle}.
     */
    public void elaboraScatto() {
        if (!scattoRichiesto) return;
        scattoRichiesto = false;
        if (cooldownScatto > 0) return;

        // Questo va spostato all'inizio della game scene, cosi possiamo load tutti i suoni del player ecc
        // L'ho messo qui solo per il il farting
        IscatAudioManager am = IscatAudioManager.getInstance();
        am.loadSFX("fart_alt1", "/uni/gaben/iscat/audio/SFX/fart3.wav");
        am.loadSFX("fart_alt2", "/uni/gaben/iscat/audio/SFX/fart8.wav");
        am.loadSFX("fart_alt3", "/uni/gaben/iscat/audio/SFX/fart7.wav");

        int randomSfx = rand.nextInt(1,3+1); // Genera 1, 2 o 3
        IscatAudioManager.getInstance().playSFX("fart_alt" + randomSfx);


        double rad = Math.toRadians(directionAngle);
        velocity = velocity.add(
                Math.cos(rad) * GameSettings.IMPULSO_SCATTO,
                Math.sin(rad) * GameSettings.IMPULSO_SCATTO
        );

        faseScatto     = GameSettings.DURATA_SCATTO_TICK;
        cooldownScatto = GameSettings.COOLDOWN_SCATTO_TICK;
    }

    /** {@code true} se lo scatto è disponibile. */
    public boolean isScattoDisponibile() { return cooldownScatto == 0; }

    /** {@code true} se la fase scatto è attiva (drag ridotto). */
    public boolean isInScatto() { return faseScatto > 0; }

    // --- Collidable ---

    @Override public double getCollisionRadius() { return GameSettings.RAGGIO_COLLISIONE; }
    @Override public Vec2   getColliderCenter()  { return getPosition(); }
    @Override public void   onCollision(Collidable other) {}

    // --- Alive ---

    @Override public void die() { /* TODO: animazione morte */ }
}
