package uni.gaben.iscat.universe.entity.interfaces;

import uni.gaben.iscat.universe.entity.GameEntity;

import java.util.function.Consumer;

public interface Collidable {
    // ---- Collision callbacks ----
    void setOnCollision(Consumer<GameEntity> onCollision);

    boolean hasCollision();

    void triggerCollision(GameEntity other);
}
