package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.effects.Starfield;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.EntityAudioManager;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modello principale dell'universo di gioco (estende il {@link World} di dyn4j).
 * Gestisce l'anagrafica delle entità, le query tipizzate, i vincoli dell'arena circolare,
 * i flussi di particelle d'impatto e il tracciamento delle variazioni di endurance (danni/cure).
 */
public class UniverseModel extends World<Body> {

    private PlayerModel player;
    private CameraModel camera;

    private final List<AbstractPhysicalEntityModel> entities = new ArrayList<>();
    private final List<AbstractPhysicalProjectileModel> projectiles = new ArrayList<>();
    private final Starfield starfield = new Starfield(0, 0);
    private final List<HitSpark> hitSparks = new ArrayList<>();

    // Indici e cache ottimizzati per evitare allocazioni a runtime
    private final Map<Class<?>, List<AbstractPhysicalEntityModel>> entitiesByCategory = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> classHierarchyCache = new ConcurrentHashMap<>();

    private double width = UniverseSettings.DEFAULT_WIDTH;
    private double height = UniverseSettings.DEFAULT_HEIGHT;
    private double physicsLifetime;
    private final Map<Vector2, Double> alteredEndurance = new ConcurrentHashMap<>();

    /**
     * Inizializza il mondo a gravità zero e configura i listener nativi dyn4j
     * per gestire impatti balistici e variazioni di endurance.
     */
    public UniverseModel() {
        setGravity(PhysicsWorld.ZERO_GRAVITY);
        addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                AbstractPhysicalEntityModel a = extractEntity(collision.getBody1());
                AbstractPhysicalEntityModel b = extractEntity(collision.getBody2());
                if (a == null || b == null) return;

                // Gestione e instanziazione particellare dei proiettili (HitSpark)
                AbstractPhysicalProjectileModel proj = null;
                AbstractPhysicalEntityModel target = null;
                if (a instanceof AbstractPhysicalProjectileModel app) {
                    proj = app; target = b;
                } else if (b instanceof AbstractPhysicalProjectileModel bpp) {
                    proj = bpp; target = a;
                }

                if (proj != null && target != null) {
                    Vector2 impactWorld = contact.getPoint();
                    Vector2 vel = proj.getLinearVelocity();
                    addHitSpark(HitSpark.create(impactWorld, camera, vel));
                }

                // Tracciamento delta endurance pre/post collisione
                double aBefore = getAbstractPhysicalEntityEndurance(a);
                double bBefore = getAbstractPhysicalEntityEndurance(b);

                a.triggerAllCollisions(b);
                b.triggerAllCollisions(a);

                double aAfter = getAbstractPhysicalEntityEndurance(a);
                double bAfter = getAbstractPhysicalEntityEndurance(b);

                handleEnduranceAlteration(a, aBefore, aAfter);
                handleEnduranceAlteration(b, bBefore, bAfter);
            }
        });
    }

    private AbstractPhysicalEntityModel extractEntity(Body body) {
        if (body instanceof AbstractPhysicalEntityModel m) return m;
        if (body.getUserData() instanceof AbstractPhysicalEntityModel m) return m;
        return null;
    }

    /**
     * Avanza la simulazione fisica dell'universo e applica i confini geometrici.
     *
     * @param dt Passo temporale (Delta Time).
     */
    public void stepPhysics(double dt) {
        super.updatev(dt);
        enforceCircularBoundaries();
        physicsLifetime += dt;
    }

    /**
     * Forza il confinamento radiale delle entità dinamiche entro il perimetro dell'arena,
     * annullando i vettori di velocità diretti verso l'esterno.
     */
    private void enforceCircularBoundaries() {
        double radius = getUniverseRadius();

        for (Body body : this.getBodies()) {
            if (body instanceof uni.gaben.iscat.universe.entities.interfaces.Dynamic) {
                Vector2 pos = body.getTransform().getTranslation();
                double distanceFromCenter = pos.getMagnitude();

                if (distanceFromCenter > radius) {
                    Vector2 normal = pos.getNormalized();
                    pos.x = normal.x * radius;
                    pos.y = normal.y * radius;

                    Vector2 vel = body.getLinearVelocity();
                    double dotProduct = vel.dot(normal);
                    if (dotProduct > 0) {
                        vel.subtract(normal.product(dotProduct));
                    }
                }
            }
        }
    }

    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width = w; this.height = h;
        }
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getUniverseRadius() { return width / 2.0; }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    public PlayerModel getPlayer() {
        return (player != null && player.shouldRemove()) ? null : player;
    }

    /**
     * Registra un'entità all'interno del mondo fisico e delle strutture dati categorizzate.
     */
    public void addEntity(AbstractPhysicalEntityModel entity) {
        entities.add(entity);
        addBody(entity);
        if (entity instanceof AbstractPhysicalProjectileModel p) projectiles.add(p);
        registerEntityCategories(entity);
    }

    /**
     * Rimuove in modo sicuro un'entità disattivando i corpi rigidi (fixtures) e i vettori di movimento.
     */
    public void removeEntity(AbstractPhysicalEntityModel entity) {
        if (entity == null) return;

        entities.remove(entity);
        if (entity instanceof AbstractPhysicalProjectileModel p) projectiles.remove(p);
        unregisterEntityCategories(entity);

        entity.setEnabled(false);
        entity.setLinearVelocity(0, 0);
        entity.setAngularVelocity(0);
        entity.setMass(MassType.INFINITE);
        entity.removeAllFixtures();
        removeBody(entity);
    }

    public List<AbstractPhysicalEntityModel> getEntities() { return Collections.unmodifiableList(entities); }
    public List<AbstractPhysicalProjectileModel> getProjectiles() { return Collections.unmodifiableList(projectiles); }
    public Starfield getStarfieldModel() { return starfield; }
    public double getPhysicsLifetime() { return physicsLifetime; }

    /**
     * Restituisce la lista di entità filtrate per classe/interfaccia tramite cache $O(1)$.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractPhysicalEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractPhysicalEntityModel> list = entitiesByCategory.get(type);
        if (list == null) return Collections.emptyList();
        return (List<T>) Collections.unmodifiableList(list);
    }

    /**
     * Genera e memorizza la gerarchia completa di classi e interfacce di un'entità per l'indicizzazione.
     */
    private List<Class<?>> getClassHierarchy(Class<?> clazz) {
        return classHierarchyCache.computeIfAbsent(clazz, c -> {
            List<Class<?>> hierarchy = new ArrayList<>();
            Class<?> current = c;
            while (current != null && current != Object.class) {
                hierarchy.add(current);
                for (Class<?> iface : current.getInterfaces()) {
                    hierarchy.add(iface);
                }
                current = current.getSuperclass();
            }
            return hierarchy;
        });
    }

    private void registerEntityCategories(AbstractPhysicalEntityModel entity) {
        for (Class<?> type : getClassHierarchy(entity.getClass())) {
            entitiesByCategory.computeIfAbsent(type, k -> new ArrayList<>()).add(entity);
        }
    }

    private void unregisterEntityCategories(AbstractPhysicalEntityModel entity) {
        for (Class<?> type : getClassHierarchy(entity.getClass())) {
            List<AbstractPhysicalEntityModel> list = entitiesByCategory.get(type);
            if (list != null) list.remove(entity);
        }
    }

    public Map<Vector2, Double> getAlteredEndurances() { return alteredEndurance; }

    private double getAbstractPhysicalEntityEndurance(AbstractPhysicalEntityModel entity) {
        return (entity instanceof Alterable alterable) ? alterable.getEndurance() : 0;
    }

    /**
     * Sincronizza i cambiamenti di salute e riproduce gli effetti audio associati (hurt/heal).
     */
    private void handleEnduranceAlteration(AbstractPhysicalEntityModel entity, double before, double after) {
        if (before == after || entity instanceof AbstractPhysicalProjectileModel) return;

        double delta = after - before;
        alteredEndurance.put(entity.getTransform().getTranslation(), delta);

        if (entity instanceof EntityModel entityModel) {
            if (delta < 0) EntityAudioManager.playEventAudio(entityModel, "hurt");
            else AudioManager.getInstance().playSFX("heal");
        }
    }

    public void setCamera(CameraModel camera) { this.camera = camera; }
    public CameraModel getCamera() { return camera; }
    public void addHitSpark(HitSpark spark) { hitSparks.add(spark); }
    public List<HitSpark> getHitSparks() { return Collections.unmodifiableList(hitSparks); }

    /**
     * Aggiorna lo stato temporale delle scintille d'impatto (HitSparks) rimuovendo quelle scadute.
     */
    public void updateSparks(double dt) {
        Iterator<HitSpark> it = hitSparks.iterator();
        while (it.hasNext()) {
            HitSpark spark = it.next();
            spark.update(dt);
            if (spark.shouldRemove()) it.remove();
        }
    }
}