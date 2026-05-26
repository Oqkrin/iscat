package uni.gaben.iscat.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldModel;

import java.util.*;

public class UniverseModel extends World<Body> {

    private PlayerModel player;

    private final List<AbstractEntityModel> entities = new ArrayList<>();
    private final List<AbstractProjectileModel> projectiles = new ArrayList<>();

    private final StarfieldModel starfieldModel =
            new StarfieldModel(0, 0);

    private final Map<Class<?>, List<AbstractEntityModel>>
            entitiesByCategory = new HashMap<>();

    private final DoubleProperty width =
            new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);

    private final DoubleProperty height =
            new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public UniverseModel() {

        setGravity(PhysicsWorld.ZERO_GRAVITY);

        addContactListener(new ContactListenerAdapter<Body>() {

            @Override
            public void begin(
                    ContactCollisionData<Body> collision,
                    Contact contact
            ) {

                Body b1 = collision.getBody1();
                Body b2 = collision.getBody2();

                AbstractEntityModel entA = extractEntity(b1);
                AbstractEntityModel entB = extractEntity(b2);

                if (entA != null && entB != null) {
                    entA.triggerCollision(entB);
                    entB.triggerCollision(entA);
                }
            }
        });
    }

    private AbstractEntityModel extractEntity(Body body) {

        if (body instanceof AbstractEntityModel model) {
            return model;
        }

        if (body.getUserData() instanceof AbstractEntityModel model) {
            return model;
        }

        return null;
    }

    public static double getUniverseScaled(double value) {
        return UU.pxToM(value);
    }

    public void stepPhysics(double dt) {
        super.updatev(dt);
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    public PlayerModel getPlayer() {

        if (player != null && player.shouldRemove()) {
            return null;
        }

        return player;
    }

    public void addEntity(AbstractEntityModel entity) {

        entities.add(entity);

        addBody(entity);

        if (entity instanceof AbstractProjectileModel projectile) {
            projectiles.add(projectile);
        }

        registerEntityCategories(entity);
    }

    public void removeEntity(AbstractEntityModel entity) {

        if (entity == null) return;

        entities.remove(entity);

        if (entity instanceof AbstractProjectileModel projectile) {
            projectiles.remove(projectile);
        }

        unregisterEntityCategories(entity);

        entity.setEnabled(false);
        entity.setLinearVelocity(0, 0);
        entity.setAngularVelocity(0);
        entity.setMass(MassType.INFINITE);
        entity.removeAllFixtures();

        removeBody(entity);
    }

    private void registerEntityCategories(AbstractEntityModel entity) {

        Class<?> current = entity.getClass();

        while (current != null && current != Object.class) {

            entitiesByCategory
                    .computeIfAbsent(current, k -> new ArrayList<>())
                    .add(entity);

            for (Class<?> iface : current.getInterfaces()) {

                entitiesByCategory
                        .computeIfAbsent(iface, k -> new ArrayList<>())
                        .add(entity);
            }

            current = current.getSuperclass();
        }
    }

    private void unregisterEntityCategories(AbstractEntityModel entity) {

        Class<?> current = entity.getClass();

        while (current != null && current != Object.class) {

            List<AbstractEntityModel> list =
                    entitiesByCategory.get(current);

            if (list != null) {
                list.remove(entity);
            }

            for (Class<?> iface : current.getInterfaces()) {

                List<AbstractEntityModel> ifaceList =
                        entitiesByCategory.get(iface);

                if (ifaceList != null) {
                    ifaceList.remove(entity);
                }
            }

            current = current.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractEntityModel>
    List<T> getEntitiesOfType(Class<T> type) {

        List<AbstractEntityModel> list =
                entitiesByCategory.get(type);

        if (list == null) {
            return new ArrayList<>();
        }

        return (List<T>) new ArrayList<>(list);
    }

    public List<AbstractEntityModel> getEntities() {
        return entities;
    }

    public List<AbstractProjectileModel> getProjectiles() {
        return projectiles;
    }

    public StarfieldModel getStarfieldModel() {
        return starfieldModel;
    }

    public void setDimensions(double w, double h) {

        if (w > 0 && h > 0) {
            width.set(w);
            height.set(h);
        }
    }

    public double getWidth() {
        return width.get();
    }

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public DoubleProperty heightProperty() {
        return height;
    }
}