package uni.gaben.iscat.universe.entities.hardcoded.asteroid;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.UniverseVelocitySettings;
import java.util.*;

/**
 * Modello fisico e geometrico di un asteroide (Asteroid Entity Model).
 * <p>
 * Rappresenta un ostacolo distruttibile di forma poligonale simulato tramite il motore dyn4j.
 * Include sistemi per l'ottimizzazione del ciclo grafico tramite caching delle coordinate di rendering
 * e logiche di frammentazione ricorsiva controllata (Cinematic Breakup).
 * </p>
 */
public class AsteroidModel extends AbstractPhysicalEntityModel {
    private final double size;
    private final Vector2[] displayVertices;

    // --- Cache di Trasformazione Locale-Mondo-Pixel ---
    private final double[] cachedXPoints;
    private final double[] cachedYPoints;
    private double lastCachedX = Double.NaN;
    private double lastCachedY = Double.NaN;
    private double lastCachedAngle = Double.NaN;

    // --- Meccaniche di Durabilità e Frantumazione ---
    private final double maxDurability;
    private double currentDurability;
    private final double splitAngle;

    /**
     * Costruttore base. Inizializza l'asteroide alle coordinate fornite delegando al costruttore radiale.
     */
    public AsteroidModel(double x, double y) {
        this(x, y, 0);
    }

    /**
     * Costruttore intermedio. Configura un asteroide con raggio predefinito e parametri strutturali standard.
     */
    public AsteroidModel(double x, double y, double radius) {
        this(x, y, radius, AsteroidSettings.MIN_VERTICES, AsteroidSettings.VERTICE_VARIATION,
                AsteroidSettings.RADIUS_VARIATION_MIN, AsteroidSettings.RADIUS_VARIATION_RANGE);
    }

    /**
     * Costruttore completo. Genera la geometria poligonale dell'asteroide, ne calcola la densità di massa
     * in scala quadratica e aggancia i listener per la gestione del danno da proiettile.
     *
     * @param radiusPixel Raggio dell'asteroide espresso in pixel (se 0, viene randomizzato).
     */
    public AsteroidModel(double x, double y, double radiusPixel,
                         int minVertices, int variation,
                         double radiusMin, double radiusRange) {
        super(x, y, new EntityRecordBuilder().build());
        Random rand = new Random();
        this.splitAngle = rand.nextDouble() * Math.PI * 2;
        this.size = radiusPixel != 0 ? radiusPixel * 2 : (16 + rand.nextInt(AsteroidSettings.MAXPXSIZE));
        double radiusMeters = UU.pxToM(size / 2.0);

        // Estrazione della mesh poligonale e configurazione del corpo rigido dyn4j
        this.displayVertices = AsteroidShapeFactory.getScaledShape(radiusMeters);
        Polygon polygon = new Polygon(this.displayVertices);

        BodyFixture fixture = addFixture(polygon);
        fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);

        // La densità scala quadraticamente rispetto alla dimensione per simulare la massa volumetrica
        double density = 1.0 + Math.pow(size / 45.0, 2.0);
        fixture.setDensity(density);
        setMass(MassType.NORMAL);

        // Smorzamento inerziale proporzionale alla dimensione dell'oggetto
        double damping = 0.5 + (size / 100.0);
        setLinearDamping(damping);
        setAngularDamping(damping);

        this.maxDurability = AsteroidSettings.BASE_DURABILITY * density;
        this.currentDurability = this.maxDurability;

        // Allocazione immediata delle matrici della cache dei punti per prevenire micro-allocazioni nel loop
        this.cachedXPoints = new double[displayVertices.length];
        this.cachedYPoints = new double[displayVertices.length];

        // Pipeline di collisione reattiva con i proiettili
        this.addOnCollision("projectile", other -> {
            if (this.shouldRemove()) return;
            if (other instanceof AbstractPhysicalProjectileModel projectile) {
                takeDamage(AsteroidSettings.PROJECTILE_DAMAGE_FACTOR);
                projectile.setShouldRemove(true);
            }
        });
    }

    /**
     * Restituisce i vertici della mesh trasformati nell'asse spaziale dei pixel (Coordinata X).
     * Sfrutta un meccanismo latente di aggiornamento della cache basato sul controllo dei delta di stato.
     */
    public double[] getCachedXPoints() {
        refreshCacheIfNeeded();
        return cachedXPoints;
    }

    /**
     * Restituisce i vertici della mesh trasformati nell'asse spaziale dei pixel (Coordinata Y).
     */
    public double[] getCachedYPoints() {
        refreshCacheIfNeeded();
        return cachedYPoints;
    }

    /**
     * Sincronizza la cache geometrica locale convertendo i vettori d'origine metrici in pixel.
     * Salta l'intera pipeline di trasformazione della matrice se il corpo rigido non ha subito spostamenti o rotazioni.
     */
    private void refreshCacheIfNeeded() {
        double tx    = getTransform().getTranslationX();
        double ty    = getTransform().getTranslationY();
        double angle = getTransform().getRotationAngle();

        // Controllo di stabilità: se i parametri della Trasformazione coincidono, la cache è valida
        if (tx == lastCachedX && ty == lastCachedY && angle == lastCachedAngle) return;

        for (int i = 0; i < displayVertices.length; i++) {
            Vector2 w = getTransform().getTransformed(displayVertices[i]);
            cachedXPoints[i] = UU.mToPx(w.x);
            cachedYPoints[i] = UU.mToPx(w.y);
        }

        lastCachedX     = tx;
        lastCachedY     = ty;
        lastCachedAngle = angle;
    }

    /**
     * Riduce la durabilità strutturale dell'asteroide. Se la resistenza si azzera, avvia la frammentazione.
     */
    public void takeDamage(double amount) {
        this.currentDurability = Math.max(0, this.currentDurability - amount);
        if (this.currentDurability <= 0) handleBreakup();
    }

    /**
     * Esegue la frammentazione fisica dell'asteroide (Mitosis Breakup).
     * Se l'asteroide supera le dimensioni minime di split, genera 4 frammenti più piccoli, calcola le loro posizioni
     * cartesiane e applica vettori di spinta cinetica ortogonali ($\pm \vec{v}$ e vettori destrorsi) prima di eliminare il corpo padre.
     */
    private void handleBreakup() {
        this.setShouldRemove(true);

        if (this.size >= AsteroidSettings.MIN_SPLIT_SIZE) {
            double asteroidXMeters = this.getTransform().getTranslationX();
            double asteroidYMeters = this.getTransform().getTranslationY();

            // Calcolo del raggio scalato dei 4 sotto-frammenti con deviazione casuale
            double smallerAsteroidARadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidBRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidCRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidDRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);

            double angle = this.splitAngle;
            double offsetMeters = UU.pxToM(this.size / 4.0);

            // Mappatura geometrica a croce dei punti di spawn per evitare sovrapposizioni dei corpi rigidi
            double mX1 = asteroidXMeters + Math.cos(angle) * offsetMeters;
            double mY1 = asteroidYMeters + Math.sin(angle) * offsetMeters;
            double mX2 = asteroidXMeters - Math.cos(angle) * offsetMeters;
            double mY2 = asteroidYMeters - Math.sin(angle) * offsetMeters;
            double mX3 = mX1;
            double mY3 = mY2;
            double mX4 = mX2;
            double mY4 = mY1;

            AsteroidModel a = new AsteroidModel(UU.mToPx(mX1), UU.mToPx(mY1), smallerAsteroidARadius);
            AsteroidModel b = new AsteroidModel(UU.mToPx(mX2), UU.mToPx(mY2), smallerAsteroidBRadius);
            AsteroidModel c = new AsteroidModel(UU.mToPx(mX3), UU.mToPx(mY3), smallerAsteroidCRadius);
            AsteroidModel d = new AsteroidModel(UU.mToPx(mX4), UU.mToPx(mY4), smallerAsteroidDRadius);

            Vector2 vel = this.getLinearVelocity();
            double pushForce = 2.0;

            // Ripartizione cinetica: i primi due seguono l'asse primario, gli altri due deviano sull'ortogonale destro
            a.setLinearVelocity(vel.copy().add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)));
            b.setLinearVelocity(vel.copy().subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)));
            c.setLinearVelocity(vel.copy().add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector());
            d.setLinearVelocity(vel.copy().subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector());

            UniverseSpawner.getInstance().spawnEntity(a);
            UniverseSpawner.getInstance().spawnEntity(b);
            UniverseSpawner.getInstance().spawnEntity(c);
            UniverseSpawner.getInstance().spawnEntity(d);
        }
    }

    public double getSize()                  { return size; }
    public Vector2[] getDisplayVertices()    { return displayVertices; }
    public double getDurabilityHealthRatio() { return currentDurability / maxDurability; }
    public double getSplitAngle()            { return splitAngle; }

    /**
     * Calcola la velocità terminale asintotica dell'asteroide interrogando i profili globali di simulazione.
     */
    @Override
    public double getTerminalVelocity() {
        return UniverseVelocitySettings.asteroidTerminalVelocity(size);
    }
}