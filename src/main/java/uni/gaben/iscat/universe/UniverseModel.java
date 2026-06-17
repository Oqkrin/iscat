package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import uni.gaben.iscat.universe.effects.Starfield;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UniverseModel extends World<Body> {

    private PlayerModel player;

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
                if (a != null && b != null) {
                    double Abefore = 0;
                    double Bbefore = 0;

                    if(a instanceof Alterable aa) {
                        Abefore = aa.getEndurance();
                    }

                    if(b instanceof Alterable ab) {
                        Bbefore = ab.getEndurance();
                    }

                    handlePlayerMeleeDamage(a, b);

                    a.triggerAllCollisions(b);
                    b.triggerAllCollisions(a);

                    double Aafter = 0;
                    double Bafter = 0;

                    if(a instanceof Alterable aa) {
                        Aafter = aa.getEndurance();
                    }

                    if(b instanceof Alterable ab) {
                        Bafter = ab.getEndurance();
                    }

                    if(Aafter != Abefore && !(a instanceof AbstractPhysicalProjectileModel)) {
                        alteredEndurance.put(a.getTransform().getTranslation(), Aafter-Abefore);
                    }

                    if(Bafter != Bbefore &&  !(b instanceof AbstractPhysicalProjectileModel)) {
                        alteredEndurance.put(b.getTransform().getTranslation(), Bafter-Bbefore);
                    }

                }
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
    public <T extends AbstractPhysicalEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractPhysicalEntityModel> list = entitiesByCategory.get(type);
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

    // Metodo per gestire il danno da mischia del player
    private void handlePlayerMeleeDamage(AbstractPhysicalEntityModel a, AbstractPhysicalEntityModel b) {
        PlayerModel player = null;
        AbstractLivingEntityModel enemy = null;

        if (a instanceof PlayerModel && b instanceof AbstractLivingEntityModel && !(b instanceof PlayerModel)) {
            player = (PlayerModel) a;
            enemy = (AbstractLivingEntityModel) b;
        } else if (b instanceof PlayerModel && a instanceof AbstractLivingEntityModel && !(a instanceof PlayerModel)) {
            player = (PlayerModel) b;
            enemy = (AbstractLivingEntityModel) a;
        }

        // Verifica che il nemico non sia un proiettile
        if (player != null && enemy != null && !(enemy instanceof AbstractPhysicalProjectileModel)) {

            if (player.canDealMeleeDamage()) {
                // Danno base
                double damage = player.getMeleeDamage();

                // Danno doppio durante il dash
                if (player.isDashing()) {
                    damage *= 2;
                    System.out.println("[MELEE] DASH! Danno doppio: " + damage);

                    // Knockback durante il dash
                    Vector2 knockbackDir = enemy.getTransform().getTranslation()
                            .copy()
                            .subtract(player.getTransform().getTranslation());

                    double distance = knockbackDir.getMagnitude();
                    if (distance > 0.001) {
                        knockbackDir.multiply(500.0 / distance);
                        enemy.applyImpulse(knockbackDir);
                        System.out.println("Knockback applicato durante il dash!");
                    }
                } else {
                    System.out.println("[MELEE] Danno normale: " + damage);
                }

                enemy.alter(-damage);
                player.startMeleeCooldown();
            }
        }
    }

}