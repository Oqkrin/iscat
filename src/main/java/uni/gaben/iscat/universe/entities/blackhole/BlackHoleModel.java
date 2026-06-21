package uni.gaben.iscat.universe.entities.blackhole;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.utils.Updatable;

/**
 * Modello fisico dell'entità Buco Nero (Black Hole Entity Model).
 * <p>
 * Simula un corpo singolare gravitazionale circolare capace di assorbire entità fisiche esterne
 * incrementando asintoticamente la propria densità e il proprio raggio.
 * </p>
 * <p>
 * <b>Architettura e Meccaniche Chiave:</b>
 * </p>
 * <ul>
 * <li><b>Deferred Mutex Update:</b> Le modifiche a raggio e densità innescate dalle collisioni vengono memorizzate
 * in flag differiti per essere applicate in sicurezza all'interno del metodo {@link #update(double)},
 * prevenendo violazioni di struttura ({@code ConcurrentModificationException}) all'interno del risolutore di dyn4j.</li>
 * <li><b>Hawking Radiation Simulation:</b> Se il buco nero non assorbe materia per un intervallo superiore a
 * {@code RADIATION_IDLE_TIME}, subisce un decadimento asintotico di raggio e densità per simulare l'evaporazione termica.</li>
 * <li><b>Player Preservation Event:</b> Il giocatore non viene assorbito ma subisce un impulso vettoriale di repulsione
 * proporzionale alla densità corrente del buco nero.</li>
 * </ul>
 */
public class BlackHoleModel extends AbstractPhysicalEntityModel implements Updatable, HasShockwave {

    private UU radius;
    private BodyFixture fixture;
    private final Shockwave shockwave = new Shockwave();

    private final double maxRadiusM;
    private final double initialRadiusM;

    // --- Parametri di Bilanciamento e Accrescimento ---
    private static final double MAX_DENSITY = 40.0;
    private static final double GROWTH_FACTOR = 0.3;
    private static final double RADIUS_GROWTH_BASE = 0.005;

    // --- Parametri della Radiazione di Hawking ---
    private static final double RADIATION_RADIUS_DECAY = 0.02;
    private static final double RADIATION_DENSITY_DECAY = 0.01;
    private static final double RADIATION_IDLE_TIME = 2.0;
    private double timeSinceLastAbsorption = 0.0;

    // --- Stati di Sincronizzazione Differita ---
    private boolean needsFixtureUpdate = false;
    private double pendingRadiusM = -1;
    private double pendingDensity = -1;

    /**
     * Costruttore completo. Inizializza il buco nero posizionandolo nel mondo e agganciando
     * il callback di collisione dedicato per l'assorbimento.
     *
     * @param initialRadiusM Il raggio iniziale (in metri del mondo fisico) dell'orizzonte degli eventi.
     */
    public BlackHoleModel(double x, double y, double initialRadiusM) {
        super(x, y, new EntityRecordBuilder().build());
        this.initialRadiusM = initialRadiusM;
        this.radius = new UU(initialRadiusM, UU.units.METERS);
        this.maxRadiusM = initialRadiusM * 7.5;

        createFixture();
        setMass(MassType.NORMAL);
        addOnCollision("blackhole", this::absorbEntity);
    }

    /**
     * Costruttore casuale. Genera un buco nero con raggio iniziale randomizzato compreso tra $0.5$ e $2.5$ metri.
     */
    public BlackHoleModel(double x, double y) {
        this(x, y, Math.random() * 2.0 + 0.5);
    }

    /**
     * Rigenera la fixture circolare di dyn4j aggiornandola al raggio corrente ed emette
     * un effetto d'onda d'urto visivo radiale.
     */
    private void createFixture() {
        if (fixture != null) removeFixture(fixture);
        fixture = addFixture(Geometry.createCircle(radius.m().get()));
        setMass(MassType.NORMAL);
        shockwave.trigger(1, radius.px().get(), 15);
        shockwave.update(0.85);
    }

    /**
     * Gestisce la logica di collisione e assorbimento delle entità catturate dall'orizzonte degli eventi.
     * <p>
     * Calcola la crescita logaritmica del raggio rispetto al limite massimo ed estrae la massa dell'oggetto
     * convertendola in incremento di densità. Invia le richieste di aggiornamento ai flag differiti.
     * </p>
     *
     * @param other L'entità fisica entrata in collisione con il buco nero.
     */
    private void absorbEntity(AbstractPhysicalEntityModel other) {
        if (other == null || other.shouldRemove()) return;
        if (other instanceof AbstractPhysicalProjectileModel appm) {
            appm.extinguish();
            return;
        }

        // Eccezione Giocatore: respinge con un vettore impulsivo anziché consumarlo
        if (other instanceof PlayerModel p) {
            Vector2 pushDir = p.getTransform().getTranslation().subtract(this.getTransform().getTranslation());
            double dist = pushDir.getMagnitude();
            if (dist > 0.01) {
                pushDir.normalize();
                double impulseMag = 800.0 * Math.min(fixture.getDensity(), MAX_DENSITY);
                p.applyImpulse(pushDir.setMagnitude(impulseMag));
            }
            return;
        }

        double absorbedMass = other.getMass().getMass();

        // 1. Calcolo asintotico del nuovo raggio basato sul progresso verso il limite massimo
        double newRadius = radius.m().get();
        if (newRadius < maxRadiusM) {
            double progress = (newRadius - initialRadiusM) / (maxRadiusM - initialRadiusM);
            double growth = absorbedMass * RADIUS_GROWTH_BASE * (1.0 - progress);
            newRadius = Math.min(newRadius + growth, maxRadiusM);
        }

        // 2. Calcolo della nuova densità limitata dal soft-cap
        double currentDensity = fixture.getDensity();
        double newDensity = Math.min(currentDensity + absorbedMass * GROWTH_FACTOR, MAX_DENSITY);

        // 3. Differimento dei parametri per la successiva fase di sincronizzazione sicura
        pendingRadiusM = newRadius;
        pendingDensity = newDensity;
        needsFixtureUpdate = true;

        if (other instanceof AbstractLivingEntityModel l) l.extinguish();
        else other.setShouldRemove(true);

        timeSinceLastAbsorption = 0.0;
    }

    /**
     * Esegue l'aggiornamento temporale periodico dell'entità.
     * <p>
     * Nella prima fase consuma in sicurezza i parametri di crescita differiti se richiesto.
     * Nella seconda fase gestisce l'orologio della Radiazione di Hawking, riducendo progressivamente
     * le dimensioni e la densità se non viene consumata materia per un periodo prolungato.
     * </p>
     */
    @Override
    public void update(double dt) {
        super.update(dt);

        // ---- Consumazione e applicazione delle modifiche differite (Safe Context) ----
        if (needsFixtureUpdate) {
            if (pendingRadiusM > 0) {
                this.radius = new UU(pendingRadiusM, UU.units.METERS);
                createFixture();
            }
            if (pendingDensity > 0) {
                fixture.setDensity(pendingDensity);
                this.setMass(MassType.NORMAL);
            }
            needsFixtureUpdate = false;
            pendingRadiusM = -1;
            pendingDensity = -1;
        }

        // ---- Simulazione dell'evaporazione (Radiazione di Hawking) ----
        timeSinceLastAbsorption += dt;
        if (timeSinceLastAbsorption > RADIATION_IDLE_TIME) {
            if (radius.m().get() > initialRadiusM) {
                double decay = RADIATION_RADIUS_DECAY * radius.m().get() * dt;
                this.radius = new UU(Math.max(initialRadiusM, radius.m().get() - decay), UU.units.METERS);
                createFixture();
            }
            double currentDensity = fixture.getDensity();
            if (currentDensity > 1.0) {
                double newDensity = Math.max(1.0, currentDensity - RADIATION_DENSITY_DECAY * dt);
                fixture.setDensity(newDensity);
                this.setMass(MassType.NORMAL);
            }
        }
    }

    public UU getRadius() {
        return radius;
    }

    @Override
    public Shockwave shockwave() {
        return shockwave;
    }
}