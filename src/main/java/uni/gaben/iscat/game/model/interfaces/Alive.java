package uni.gaben.iscat.game.model.interfaces;

/** Entità con salute: può subire danni e morire. */
public interface Alive {

    int  getHp();
    int  getMaxHp();
    void setHp(int hp);

    /** {@code true} finché hp > 0. */
    default boolean isAlive() { return getHp() > 0; }

    /** Riduce hp di {@code amount}; chiama {@link #die()} se raggiunge zero. */
    default void takeDamage(int amount) {
        setHp(Math.max(0, getHp() - amount));
        if (!isAlive()) die();
    }

    /** Chiamato quando hp arriva a 0. Sovrascrivere per effetti morte. */
    void die();
}
