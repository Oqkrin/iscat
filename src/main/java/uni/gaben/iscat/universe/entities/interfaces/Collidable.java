package uni.gaben.iscat.universe.entities.interfaces;

import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

import java.util.function.Consumer;

public interface Collidable {
    // ---- Collision callbacks ----
    void addOnCollision(String id, Consumer<AbstractPhysicalEntityModel> onCollision);

    boolean hasAnyCollision();

    void triggerAllCollisions(AbstractPhysicalEntityModel other);
    void triggerCollision(String id, AbstractPhysicalEntityModel other);

    void clearOnCollisions();

    void removeOnCollision(String melee);
}
