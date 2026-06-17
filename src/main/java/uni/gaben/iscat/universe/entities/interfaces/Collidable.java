package uni.gaben.iscat.universe.entities.interfaces;

import uni.gaben.iscat.universe.entities.AbstractEntityModel;

import java.util.function.Consumer;

public interface Collidable {
    // ---- Collision callbacks ----
    void addOnCollision(String id, Consumer<AbstractEntityModel> onCollision);

    boolean hasAnyCollision();

    void triggerAllCollisions(AbstractEntityModel other);
    void triggerCollision(String id, AbstractEntityModel other);

    void clearOnCollisions();

    void removeOnCollision(String melee);
}
