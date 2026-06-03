package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;

import uni.gaben.iscat.universe.entity.enemies.generic.GenericEntityModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.projectiles.AbstractProjectileModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The physics world and entity registry for one game session.
 *
 * <p>Width and height are plain doubles (no JavaFX properties) because
 * the universe dimensions are set once at creation time from the canvas
 * size and never need to drive UI bindings. This avoids the fragile
 * binding/unbinding dance that caused the black-screen restart bug.</p>
 */
public class UniverseModel extends World<Body> {

    private PlayerModel player;

    private final List<AbstractEntityModel>   entities    = new ArrayList<>();
    private final List<AbstractProjectileModel> projectiles = new ArrayList<>();
    private final Starfield starfield = new Starfield(0, 0);
    private final Map<Class<?>, List<AbstractEntityModel>> entitiesByCategory = new HashMap<>();

    /** Default spawn centre when canvas dimensions are not yet available. */
    public static final double DEFAULT_SPAWN_CENTER = UniverseSettings.DEFAULT_WIDTH / 2.0;

    private double width  = UniverseSettings.DEFAULT_WIDTH;
    private double height = UniverseSettings.DEFAULT_HEIGHT;
    private double lifetime;
    private int debugTickCount = 0;

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

    /** Converts a pixel value to physics-world metres using the universe scale. */
    public static double getUniverseScaled(double value) {
        return UU.pxToM(value);
    }

    // -------------------------------------------------------------------------
    // Physics step
    // -------------------------------------------------------------------------

    public void stepPhysics(double dt) {
        // Check for NaN/Infinite values in all bodies BEFORE physics step
        for (Body body : getBodies()) {
            double tx = body.getTransform().getTranslationX();
            double ty = body.getTransform().getTranslationY();
            double vx = body.getLinearVelocity().x;
            double vy = body.getLinearVelocity().y;

            if (body instanceof GenericEntityModel entity) {
                System.out.println("UniverseModel");
                System.out.println("Entity key: " + entity.getEntityKey());
            System.out.println("Body: " + body.getClass().getSimpleName());
            System.out.println("Position: " + tx + ", " + ty);
            System.out.println("Velocity: " + vx + ", " + vy);
            System.out.println("mass " + body.getMass().getMass());
            System.out.println("Tick: " + debugTickCount + " Lifetime: " + lifetime);
            }
        }
        super.updatev(dt);
        lifetime += dt;
    }

    // -------------------------------------------------------------------------
    // Dimensions  (set once after the canvas is known; no binding required)
    // -------------------------------------------------------------------------

    /** Must be called once after the canvas dimensions are available. */
    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            if (this.width != w || this.height != h) {
                System.out.println(">>> Universe dimensions changed: " + this.width + "x" + this.height 
                    + " -> " + w + "x" + h + " (tick: " + debugTickCount + ")");
            }
            this.width  = w;
            this.height = h;
        }
    }

    public double getWidth()  { return width; }
    public double getHeight() { return height; }

    // -------------------------------------------------------------------------
    // Player
    // -------------------------------------------------------------------------

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    /** Returns the player, or {@code null} if it has been marked for removal. */
    public PlayerModel getPlayer() {
        return (player != null && player.shouldRemove()) ? null : player;
    }

    // -------------------------------------------------------------------------
    // Entity registry
    // -------------------------------------------------------------------------

    public void addEntity(AbstractEntityModel entity) {
        entities.add(entity);
        addBody(entity);
        if (entity instanceof AbstractProjectileModel p) projectiles.add(p);
        registerEntityCategories(entity);
    }

    public void removeEntity(AbstractEntityModel entity) {
        if (entity == null) return;

        entities.remove(entity);
        if (entity instanceof AbstractProjectileModel p) projectiles.remove(p);
        unregisterEntityCategories(entity);

        entity.setEnabled(false);
        entity.setLinearVelocity(0, 0);
        entity.setAngularVelocity(0);
        entity.setMass(MassType.INFINITE);
        entity.removeAllFixtures();
        removeBody(entity);
    }

    private void registerEntityCategories(AbstractEntityModel entity) {
        Class<?> cls = entity.getClass();
        while (cls != null && cls != Object.class) {
            entitiesByCategory.computeIfAbsent(cls, k -> new ArrayList<>()).add(entity);
            for (Class<?> iface : cls.getInterfaces())
                entitiesByCategory.computeIfAbsent(iface, k -> new ArrayList<>()).add(entity);
            cls = cls.getSuperclass();
        }
    }

    private void unregisterEntityCategories(AbstractEntityModel entity) {
        Class<?> cls = entity.getClass();
        while (cls != null && cls != Object.class) {
            List<AbstractEntityModel> list = entitiesByCategory.get(cls);
            if (list != null) list.remove(entity);
            for (Class<?> iface : cls.getInterfaces()) {
                List<AbstractEntityModel> il = entitiesByCategory.get(iface);
                if (il != null) il.remove(entity);
            }
            cls = cls.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractEntityModel> list = entitiesByCategory.get(type);
        return list == null ? new ArrayList<>() : (List<T>) new ArrayList<>(list);
    }

    public List<AbstractEntityModel>    getEntities()    { return entities; }
    public List<AbstractProjectileModel> getProjectiles() { return projectiles; }
    public Starfield getStarfieldModel() { return starfield; }
    public double                       getLifetime()    { return lifetime; }
}
