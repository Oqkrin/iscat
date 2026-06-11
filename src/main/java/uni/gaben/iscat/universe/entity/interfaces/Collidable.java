package uni.gaben.iscat.universe.entity.interfaces;

import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.function.Consumer;

public interface Collidable {
    // ---- Collision callbacks ----
    void setOnCollision(Consumer<AbstractEntityModel> onCollision);

    boolean hasCollision();

    void triggerCollision(AbstractEntityModel other);
}
