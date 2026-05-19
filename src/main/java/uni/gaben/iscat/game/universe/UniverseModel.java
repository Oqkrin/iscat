package uni.gaben.iscat.game.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasTerminalVelocity;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.enemies.iscat_worm.IscatWormSegment;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.starfield.StarfieldModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UniverseModel extends World<Body> {

    private PlayerModel player;
    private final List<AbstractEntityModel> entities = new ArrayList<>();
    private final List<AbstractProjectileModel> projectiles = new ArrayList<>();
    private final StarfieldModel starfieldModel = new StarfieldModel(0, 0);
    private final Map<Class<?>, List<AbstractEntityModel>> entitiesByCategory = new HashMap<>();

    private final DoubleProperty width = new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
    private final DoubleProperty height = new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public UniverseModel() {
        setGravity(PhysicsWorld.ZERO_GRAVITY);

        addContactListener(new ContactListenerAdapter<Body>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();

                AbstractEntityModel entA = null;
                if (b1 instanceof AbstractEntityModel model) entA = model;
                else if (b1.getUserData() instanceof AbstractEntityModel model) entA = model;

                AbstractEntityModel entB = null;
                if (b2 instanceof AbstractEntityModel model) entB = model;
                else if (b2.getUserData() instanceof AbstractEntityModel model) entB = model;

                if (entA != null && entB != null) {
                    // Innesca in sicurezza i callback logici nei rispettivi controller
                    entA.triggerCollision(entB);
                    entB.triggerCollision(entA);
                }
            }
        });
    }

    public static double getUniverseScaled(double value) {
        return UU.pxToM(value);
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    private void registerEntityCategories(AbstractEntityModel entity) {
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            entitiesByCategory.computeIfAbsent(current, k -> new ArrayList<>()).add(entity);
            for (Class<?> iface : current.getInterfaces()) {
                entitiesByCategory.computeIfAbsent(iface, k -> new ArrayList<>()).add(entity);
            }
            current = current.getSuperclass();
        }
    }

    private void unregisterEntityCategories(AbstractEntityModel entity) {
        Class<?> current = entity.getClass();
        while (current != null && current != Object.class) {
            List<AbstractEntityModel> list = entitiesByCategory.get(current);
            if (list != null) {
                list.remove(entity);
            }
            for (Class<?> iface : current.getInterfaces()) {
                List<AbstractEntityModel> ifaceList = entitiesByCategory.get(iface);
                if (ifaceList != null) {
                    ifaceList.remove(entity);
                }
            }
            current = current.getSuperclass();
        }
    }

    public void addEntity(AbstractEntityModel entity) {
        this.entities.add(entity);
        this.addBody(entity);
        if (entity instanceof AbstractProjectileModel projectile) {
            this.projectiles.add(projectile);
        }
        registerEntityCategories(entity);
    }

    public void removeEntity(AbstractEntityModel entity) {
        if (entity == null) return;

        // Rimozione immediata dalla lista logica usata dalla View per il rendering
        this.entities.remove(entity);
        if (entity instanceof AbstractProjectileModel projectile) {
            this.projectiles.remove(projectile);
        }
        unregisterEntityCategories(entity);

        // Disabilitazione totale del corpo fisico per prevenire contatti fantasma residui
        entity.setEnabled(false);
        entity.setLinearVelocity(0, 0);
        entity.setAngularVelocity(0);
        entity.setMass(MassType.INFINITE);
        entity.removeAllFixtures();

        boolean removed = this.removeBody(entity);
    }

    public List<AbstractEntityModel> getEntities() { return entities; }
    public List<AbstractProjectileModel> getProjectiles() { return projectiles; }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        List<AbstractEntityModel> list = entitiesByCategory.get(type);
        if (list == null) {
            return new ArrayList<>();
        }
        return (List<T>) new ArrayList<>(list);
    }

    /**
     * Esegue l'aggiornamento dello stato dei corpi interni.
     * Nota: Questo metodo viene invocato esplicitamente dall'update del Controller.
     */
    @Override
    public void updatev(double dt) {
        if (player != null) {
            player.update(dt);
        }
        clampTerminalVelocities();

        // Allinea i segmenti del corpo e della coda del verme alla direzione del loro movimento fisico
        for (Body b : getBodies()) {
            if (b instanceof IscatWormSegment segment && segment.getType() != IscatWormSegment.Type.HEAD) {
                Vector2 vel = b.getLinearVelocity();
                if (vel.getMagnitudeSquared() > 0.01) {
                    b.getTransform().setRotation(vel.getDirection());
                }
            }
        }

        super.updatev(dt);
    }

    /**
     * Ispeziona lo stato vitale delle entità e rimuove i corpi distrutti.
     * Esposto pubblicamente per permettere all'UniverseController di sincronizzarlo nel loop.
     */
    public void processEntityCleanup() {
        List<AbstractEntityModel> toRemove = new ArrayList<>();
        List<AbstractEntityModel> currentEntities = new ArrayList<>(this.entities);

        for (AbstractEntityModel e : currentEntities) {
            if (e == null) continue;

            boolean shouldDeadClean = false;

            // 1. Verifica flag di rimozione esplicito dell'entità
            if (e.shouldRemove()) {
                shouldDeadClean = true;
            }
            // 2. Fallback per modelli viventi con punti vita azzerati
            else if (e instanceof LivingEntityModel living) {
                if (living.getLife() <= 0) {
                    if (!living.shouldRemove()) {
                        living.kill();
                    }
                    shouldDeadClean = true;
                }
            }

            if (shouldDeadClean) {
                toRemove.add(e);
            }
        }

        for (AbstractEntityModel entity : toRemove) {
            removeEntity(entity);
        }
    }

    private void clampTerminalVelocities() {
        for (Body b : getBodies()) {
            if (b instanceof HasTerminalVelocity entity) {
                double maxAllowedSpeed = entity.getTerminalVelocity();
                Vector2 velocity = b.getLinearVelocity();

                if (velocity.getMagnitude() > maxAllowedSpeed) {
                    b.setLinearVelocity(velocity.getNormalized().setMagnitude(maxAllowedSpeed));
                }
            }
        }
    }

    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width.set(w);
            this.height.set(h);
        }
    }

    public double getWidth() { return width.get(); }
    public double getHeight() { return height.get(); }
    public DoubleProperty widthProperty() { return width; }
    public DoubleProperty heightProperty() { return height; }
    public PlayerModel getPlayer() { return player; }
    public StarfieldModel getStarfieldModel() { return starfieldModel; }
}