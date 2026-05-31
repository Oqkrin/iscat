package uni.gaben.iscat.universe.enemies.healer;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

/**
 * Modello logico e fisico per l'entità nemica di tipo Healer (Curatore).
 * Estende {@link LivingEntityModel} per la gestione dei punti vita e implementa {@link HasShockwave}
 * per emettere un'onda d'urto ad area (Shockwave) dedicata all'applicazione di cure ai mob alleati.
 * Integra un sistema di caricamento da database accoppiato a costanti statiche di fallback rigido.
 */
public class IscatHealerModel extends LivingEntityModel implements HasShockwave, Updatable {

    /** Chiave primaria di censimento nel database SQLite per questa entità specifica. */
    private static final String ENTITY_KEY = "iscat_healer";

    // Costanti strutturali di sicurezza utilizzate in assenza del record nel database
    private static final double FALLBACK_INIT_LIFE  = 80.0;
    private static final double FALLBACK_DIM_SPRITE = 32.0;
    private static final double FALLBACK_SCALE      = 2.0;
    private static final double FALLBACK_DAMPING    = 3.0;
    private static final int    FALLBACK_XP_REWARD  = 100;

    /** Istanza dell'onda d'urto legata all'entità per l'erogazione delle auree o degli impulsi curativi. */
    private final ShockwaveModel healingWave = new ShockwaveModel();

    /** Contenitore dei parametri biologici e cinematici estratti. */
    private final GenericEntitySettings settings;

    /**
     * Costruisce il modello dell'Healer interrogando il database per ottenerne le impostazioni.
     *
     * @param x Coordinata X iniziale nel mondo (espressa in pixel).
     * @param y Coordinata Y iniziale nel mondo (espressa in pixel).
     */
    public IscatHealerModel(double x, double y) {
        this(x, y, loadSettings());
    }

    /**
     * Costruttore privato di aggancio per l'inizializzazione atomica dei corpi fisici Dyn4J
     * e la configurazione dei moltiplicatori e dei layer di collisione.
     */
    private IscatHealerModel(double x, double y, GenericEntitySettings s) {
        super(x, y, s.initLife, s.initLife);
        this.settings = s;

        setEntityKey(ENTITY_KEY);
        setXpReward(s.xpReward);

        // Definizione del collider circolare scalato e convertito in metri
        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(s.dimSprite * s.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(s.dampingLineare);
    }

    /**
     * Tenta il recupero selettivo delle proprietà dell'Healer tramite DAO.
     * Se il database non risponde o la riga è assente, genera un'istanza provvisoria
     * cablata sulle costanti statiche di emergenza per evitare il crash dell'applicazione.
     *
     * @return Un oggetto GenericEntitySettings pronto per l'uso.
     */
    private static GenericEntitySettings loadSettings() {
        return IscatDB.getInstance().getEnemyDAO().findByKey(ENTITY_KEY).orElseGet(() -> {
            System.err.println("[IscatHealerModel] Record DB non trovato. Applicazione dei valori di fallback.");
            GenericEntitySettings s = new GenericEntitySettings();
            s.initLife       = FALLBACK_INIT_LIFE;
            s.dimSprite      = FALLBACK_DIM_SPRITE;
            s.scale          = FALLBACK_SCALE;
            s.dampingLineare = FALLBACK_DAMPING;
            s.xpReward       = FALLBACK_XP_REWARD;
            return s;
        });
    }

    public GenericEntitySettings getSettings() { return settings; }

    /**
     * Ritorna il modello dell'onda d'urto d'area associato a questo curatore.
     */
    @Override public ShockwaveModel shockwave() { return healingWave; }

    /**
     * Aggiorna lo stato di espansione temporale e il ciclo vitale dell'onda d'urto curativa.
     *
     * @param dt Tempo trascorso dall'ultimo tick di gioco.
     */
    @Override
    public void update(double dt) {
        healingWave.update(dt);
    }

    /**
     * Ritorna la velocità limite oltre la quale interviene il freno del loop di controllo cinematico.
     */
    @Override
    public double getTerminalVelocity() { return settings.maxVelocity; }
}