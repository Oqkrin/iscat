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
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.EntityAudioManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UniverseModel extends World<Body> {

    private PlayerModel player;
    private CameraModel camera;

    private final List<AbstractPhysicalEntityModel> entities = new ArrayList<>();
    private final List<AbstractPhysicalProjectileModel> projectiles = new ArrayList<>();
    private final Starfield starfield = new Starfield(0, 0);
    private final Map<Class<?>, List<AbstractPhysicalEntityModel>> entitiesByCategory = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> classHierarchyCache = new ConcurrentHashMap<>();

    public static final double DEFAULT_SPAWN_WIDTHCENTER = UniverseSettings.DEFAULT_WIDTH / 2.0;
    public static final double DEFAULT_SPAWN_HEIGHTCENTER = UniverseSettings.DEFAULT_HEIGHT / 2.0;

    private double width = UniverseSettings.DEFAULT_WIDTH;
    private double height = UniverseSettings.DEFAULT_HEIGHT;
    private double physicsLifetime;
    private final Map<Vector2, Double> alteredEndurance = new ConcurrentHashMap<>();

    // In UniverseModel.java

    private final List<HitSpark> hitSparks = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public UniverseModel() {
        setGravity(PhysicsWorld.ZERO_GRAVITY);
        addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                AbstractPhysicalEntityModel a = extractEntity(collision.getBody1());
                AbstractPhysicalEntityModel b = extractEntity(collision.getBody2());
                if (a == null || b == null) return;

                // If one of them is a projectile, spawn a hit spark
                AbstractPhysicalProjectileModel proj = null;
                AbstractPhysicalEntityModel target = null;
                if (a instanceof AbstractPhysicalProjectileModel app) {
                    proj = app;
                    target = b;
                } else if (b instanceof AbstractPhysicalProjectileModel bpp) {
                    proj = bpp;
                    target = a;
                }

                if (proj != null && target != null) {
                    // Get impact point (the contact point)
                    Vector2 impactWorld = contact.getPoint(); // contact point in world coords

                    // Get projectile velocity
                    Vector2 vel = proj.getLinearVelocity();

                    // Create spark with 20 confetti and 10 sequins (adjust as needed)
                    HitSpark spark = HitSpark.create(
                            impactWorld,
                            camera,
                            vel,
                            getCamera().getScreenWidth(),
                            getCamera().getScreenHeight(),
                            20, 10
                    );
                    addHitSpark(spark);
                }



                // 1. Record endurance before collision callbacks
                double aBefore = getAbstractPhysicalEntityEndurance(a);
                double bBefore = getAbstractPhysicalEntityEndurance(b);

                // 2. Trigger all collision callbacks (includes player melee)
                a.triggerAllCollisions(b);
                b.triggerAllCollisions(a);

                // 3. Record endurance after and track changes
                double aAfter = getAbstractPhysicalEntityEndurance(a);
                double bAfter = getAbstractPhysicalEntityEndurance(b);

                handleEnduranceAlteration(a, aBefore, aAfter);
                handleEnduranceAlteration(b, bBefore, bAfter);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AbstractPhysicalEntityModel extractEntity(Body body) {
        if (body instanceof AbstractPhysicalEntityModel m) return m;
        if (body.getUserData() instanceof AbstractPhysicalEntityModel m) return m;
        return null;
    }

    // -------------------------------------------------------------------------
    // Physics step con controllo confini circolari
    // -------------------------------------------------------------------------

    public void stepPhysics(double dt) {
        super.updatev(dt);
        enforceCircularBoundaries();
        physicsLifetime += dt;
    }

    private void enforceCircularBoundaries() {
        double radius = getUniverseRadius();

        for (Body body : this.getBodies()) {
            // Applichiamo il vincolo solo alle entità dinamiche che possono muoversi
            if (body instanceof uni.gaben.iscat.universe.entities.interfaces.Dynamic) {
                Vector2 pos = body.getTransform().getTranslation();
                double distanceFromCenter = pos.getMagnitude();

                // Se l'entità supera il raggio dell'arena circolare
                if (distanceFromCenter > radius) {
                    Vector2 normal = pos.getNormalized();

                    // Riposiziona l'entità esattamente lungo il perimetro interno
                    pos.x = normal.x * radius;
                    pos.y = normal.y * radius;

                    // Annulla la velocità residua diretta verso l'esterno per evitare rimbalzi innaturali
                    Vector2 vel = body.getLinearVelocity();
                    double dotProduct = vel.dot(normal);
                    if (dotProduct > 0) {
                        vel.subtract(normal.product(dotProduct));
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Dimensions & Geometry
    // -------------------------------------------------------------------------

    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width = w;
            this.height = h;
        }
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

    /**
     * Calcola il raggio dell'universo circolare in metri basandosi sulla dimensione della mappa.
     */
    public double getUniverseRadius() {
        return width / 2.0;
    }

    // -------------------------------------------------------------------------
    // Player
    // -------------------------------------------------------------------------

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    public PlayerModel getPlayer() {
        return (player != null && player.shouldRemove()) ? null : player;
    }

    // -------------------------------------------------------------------------
    // Entity registry
    // -------------------------------------------------------------------------

    public void addEntity(AbstractPhysicalEntityModel entity) {
        entities.add(entity);
        addBody(entity);
        if (entity instanceof AbstractPhysicalProjectileModel p) projectiles.add(p);
        registerEntityCategories(entity);
    }

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

    /** Returns an unmodifiable view of the master entity list. */
    public List<AbstractPhysicalEntityModel> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /** Returns an unmodifiable list of projectiles. */
    public List<AbstractPhysicalProjectileModel> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    public Starfield getStarfieldModel() { return starfield; }
    public double getPhysicsLifetime() { return physicsLifetime; }

    // -------------------------------------------------------------------------
    // Optimised category queries
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public <T extends AbstractPhysicalEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractPhysicalEntityModel> list = entitiesByCategory.get(type);
        if (list == null) return Collections.emptyList();
        return (List<T>) Collections.unmodifiableList(list);
    }

    // -------------------------------------------------------------------------
    // Internal hierarchy cache helpers
    // -------------------------------------------------------------------------

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

    public Map<Vector2, Double> getAlteredEndurances() {
        return alteredEndurance;
    }

    private double getAbstractPhysicalEntityEndurance(AbstractPhysicalEntityModel entity) {
        return (entity instanceof Alterable alterable) ? alterable.getEndurance() : 0;
    }

    private void handleEnduranceAlteration(AbstractPhysicalEntityModel entity, double before, double after) {
        if (before == after) return;
        if (entity instanceof AbstractPhysicalProjectileModel) return;

        double delta = after - before;
        alteredEndurance.put(entity.getTransform().getTranslation(), delta);

        if (entity instanceof EntityModel entityModel) {
            if(delta < 0) EntityAudioManager.playEventAudio(entityModel, "hurt");
            else if(delta > 0) AudioManager.getInstance().playSFX("heal");
        }
    }

    public void setCamera(CameraModel camera) {
        this.camera = camera;
    }

    public CameraModel getCamera() {
        return camera;
    }

    public void addHitSpark(HitSpark spark) {
        hitSparks.add(spark);
    }

    public List<HitSpark> getHitSparks() {
        return Collections.unmodifiableList(hitSparks);
    }

    // In your update loop (e.g., stepPhysics or a separate update method)
    public void updateSparks(double dt) {
        Iterator<HitSpark> it = hitSparks.iterator();
        while (it.hasNext()) {
            HitSpark spark = it.next();
            spark.update(dt);
            if (spark.shouldRemove()) it.remove();
        }
    }

}