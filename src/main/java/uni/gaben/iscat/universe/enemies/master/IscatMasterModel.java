package uni.gaben.iscat.universe.enemies.master;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.database.sqlite.EnemyDAO;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UniverseWaveController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

/**
 * Modello logico e fisico per il Boss finale (IscatMaster).
 * Estende {@link LivingEntityModel} e implementa {@link HasShockwave} per gestire l'emissione
 * di impulsi d'attacco globali. Governa una macchina a stati interna per le animazioni e
 * controlla la rimozione fisica ritardata, utile a consentire il completamento dei vfx visivi di morte
 * prima di notificare la fine dell'ondata e aggiornare il database.
 */
public class IscatMasterModel extends LivingEntityModel implements HasShockwave, Updatable {

    /** Chiave primaria identificativa del Boss all'interno del database. */
    private static final String ENTITY_KEY = "iscat_master";

    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    /** Stati utilizzabili per pilotare la visualizzazione delle clip animate del Boss. */
    public enum AnimationState { IDLE, ATTACK1, ATTACK2, ATTACK3, ATTACK4, DEATH }
    private AnimationState animationState = AnimationState.IDLE;

    /** Flag di stato che traccia il completamento della cinematica di ingresso del Boss nella mappa. */
    private boolean entranceDone = false;

    /** Flag di sicurezza per evitare la doppia invocazione della routine di morte permanente. */
    private boolean completeKillCalled = false;

    private final UniverseWaveController waveController;
    private final GenericEntitySettings settings;

    /**
     * Costruisce il modello dell'IscatMaster interfacciandosi con il controllore delle ondate corrente.
     *
     * @param x              Coordinata X iniziale di spawn in pixel.
     * @param y              Coordinata Y iniziale di spawn in pixel.
     * @param waveController Riferimento al manager delle ondate per notificare la risoluzione del match.
     */
    public IscatMasterModel(double x, double y, UniverseWaveController waveController) {
        this(x, y, waveController, loadSettings());
    }

    /**
     * Costruttore privato adibito alla configurazione dei vincoli fisici Dyn4J del Boss.
     * Inizializza il corpo rigido a rotazione vincolata e lo imposta inizialmente come disabilitato
     * in attesa del termine della sequenza cinematica d'ingresso.
     */
    private IscatMasterModel(double x, double y, UniverseWaveController waveController,
                             GenericEntitySettings s) {
        super(x, y, s.initLife, s.initLife);
        this.waveController = waveController;
        this.settings = s;

        setEntityKey(ENTITY_KEY);
        setXpReward(s.xpReward);

        // Generazione del collider circolare di grandi dimensioni basato sui parametri DB
        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(s.dimSprite * s.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.FIXED_ANGULAR_VELOCITY); // Impedisce al Boss di ruotare su se stesso a seguito di urti
        setLinearDamping(s.dampingLineare);

        // Disabilitato di default fino al completamento dell'animazione di spawn intro
        setEnabled(false);
    }

    /**
     * Carica le configurazioni dal database. Se la riga del Boss è assente, inizializza
     * un DTO minimale di emergenza con valori unitari per prevenire fallimenti critici nell'engine.
     */
    private static GenericEntitySettings loadSettings() {
        return EnemyDAO.findByKey(ENTITY_KEY).orElseGet(() -> {
            GenericEntitySettings s = new GenericEntitySettings();
            s.initLife       = 1;
            s.dimSprite      = 1;
            s.scale          = 1;
            s.dampingLineare = 1;
            s.maxVelocity    = 1;
            s.xpReward       = 1;
            return s;
        });
    }

    public GenericEntitySettings getSettings() { return settings; }

    @Override
    public ShockwaveModel shockwave() { return shockwaveModel; }

    @Override
    public void update(double dt) { shockwaveModel.update(dt); }

    public AnimationState getAnimationState() { return animationState; }
    public void setAnimationState(AnimationState state) { this.animationState = state; }

    public boolean isEntranceDone() { return entranceDone; }
    public void setEntranceDone(boolean done) { this.entranceDone = done; }

    /**
     * Intercetta la richiesta standard di distruzione. Invece di rimuovere l'entità,
     * sposta lo stato su DEATH per avviare l'animazione di esplosione finale.
     */
    @Override
    public void kill() { this.kill(true); }

    @Override
    public void kill(boolean b) {
        if (animationState == AnimationState.DEATH) return;
        setAnimationState(AnimationState.DEATH);
    }

    /**
     * Sovrascrive la logica di rimozione. Fintanto che il Boss si trova nello stato DEATH,
     * la rimozione fisica dal motore viene bloccata finché i controller visivi non invocano {@link #completeKill()}.
     */
    @Override
    public boolean shouldRemove() {
        if (animationState == AnimationState.DEATH && !completeKillCalled) return false;
        return super.shouldRemove();
    }

    /**
     * Sancisce la distruzione definitiva del Boss a schermo.
     * Incrementa in modo persistente sul database il contatore di uccisioni per l'utente loggato,
     * notifica la vittoria formale al controller delle ondate e sblocca la rimozione della superclasse.
     */
    public void completeKill() {
        if (completeKillCalled) return;
        completeKillCalled = true;

        try {
            var user = uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                uni.gaben.iscat.database.sqlite.EnemyDAO.incrementKill(user.id(), ENTITY_KEY);
            }
        } catch (Exception e) {
            System.err.println("[ERRORE REGISTRAZIONE BOSS] Impossibile aggiornare i record di morte su DB");
            e.printStackTrace();
        }

        if (waveController != null) {
            waveController.notifyBossDead();
        }

        super.kill(true);
    }

    @Override
    public double getTerminalVelocity() { return settings.maxVelocity; }
}