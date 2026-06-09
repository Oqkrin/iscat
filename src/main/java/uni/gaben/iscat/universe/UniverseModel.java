package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;

import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.AbstractProjectileModelAbstract;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UniverseModel extends World<Body> {

    private PlayerModel player;

    private final List<AbstractEntityModel> entities = new ArrayList<>();
    private final List<AbstractProjectileModelAbstract> projectiles = new ArrayList<>();
    private final Starfield starfield = new Starfield(0, 0);
    private final Map<Class<?>, List<AbstractEntityModel>> entitiesByCategory = new HashMap<>();
    private final Map<Class<?>, List<Class<?>>> classHierarchyCache = new ConcurrentHashMap<>();

    public static final double DEFAULT_SPAWN_WIDTHCENTER = UniverseSettings.DEFAULT_WIDTH / 2.0;
    public static final double DEFAULT_SPAWN_HEIGHTCENTER = UniverseSettings.DEFAULT_HEIGHT / 2.0;

    private double width = UniverseSettings.DEFAULT_WIDTH;
    private double height = UniverseSettings.DEFAULT_HEIGHT;
    private double physicsLifetime;

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public UniverseModel() {
        setGravity(PhysicsWorld.ZERO_GRAVITY);
        addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                AbstractEntityModel a = extractEntity(collision.getBody1());
                AbstractEntityModel b = extractEntity(collision.getBody2());
                if (a != null && b != null) {
                    a.triggerCollision(b);
                    b.triggerCollision(a);
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AbstractEntityModel extractEntity(Body body) {
        if (body instanceof AbstractEntityModel m) return m;
        if (body.getUserData() instanceof AbstractEntityModel m) return m;
        return null;
    }

    public static double getUniverseScaled(double value) {
        return UU.pxToM(value);
    }

    // -------------------------------------------------------------------------
    // Physics step
    // -------------------------------------------------------------------------

    public void stepPhysics(double dt) {
        super.updatev(dt);
        physicsLifetime += dt;
    }

    // -------------------------------------------------------------------------
    // Dimensions
    // -------------------------------------------------------------------------

    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width = w;
            this.height = h;
        }
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }

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

    public void addEntity(AbstractEntityModel entity) {
        entities.add(entity);
        addBody(entity);
        if (entity instanceof AbstractProjectileModelAbstract p) projectiles.add(p);
        registerEntityCategories(entity);
    }

    public void removeEntity(AbstractEntityModel entity) {
        if (entity == null) return;

        entities.remove(entity);
        if (entity instanceof AbstractProjectileModelAbstract p) projectiles.remove(p);
        unregisterEntityCategories(entity);

        entity.setEnabled(false);
        entity.setLinearVelocity(0, 0);
        entity.setAngularVelocity(0);
        entity.setMass(MassType.INFINITE);
        entity.removeAllFixtures();
        removeBody(entity);
    }

    /** Returns an unmodifiable view of the master entity list. */
    public List<AbstractEntityModel> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /** Returns an unmodifiable list of projectiles. */
    public List<AbstractProjectileModelAbstract> getProjectiles() {
        return Collections.unmodifiableList(projectiles);
    }

    public Starfield getStarfieldModel() { return starfield; }
    public double getPhysicsLifetime() { return physicsLifetime; }

    // -------------------------------------------------------------------------
    // Optimised category queries – no more defensive copies
    // -------------------------------------------------------------------------

    /**
     * Returns an unmodifiable list of all entities that are instances of the given class
     * or implement the given interface.
     * <p>
     * This method is O(1) and does not allocate a new list per call.
     * The returned list reflects live changes; do not modify it directly.
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractEntityModel> list = entitiesByCategory.get(type);
        if (list == null) return Collections.emptyList();
        // Return unmodifiable view – safe and allocation‑free
        return (List<T>) Collections.unmodifiableList(list);
    }

    // -------------------------------------------------------------------------
    // Internal helpers – cached hierarchy for faster registration
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

    private void registerEntityCategories(AbstractEntityModel entity) {
        for (Class<?> type : getClassHierarchy(entity.getClass())) {
            entitiesByCategory.computeIfAbsent(type, k -> new ArrayList<>()).add(entity);
        }
    }

    private void unregisterEntityCategories(AbstractEntityModel entity) {
        for (Class<?> type : getClassHierarchy(entity.getClass())) {
            List<AbstractEntityModel> list = entitiesByCategory.get(type);
            if (list != null) list.remove(entity);
        }
    }
}