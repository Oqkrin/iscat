package uni.gaben.iscat.gamenex.interfaces.model;

public interface Alive {
    int getHp();
    int getMaxHp();
    void setHp(int hp);

    default boolean isAlive() { return getHp() > 0; }

    default void takeDamage(double amount) {
        setHp(Math.max(0, (int) (getHp() - amount)));
        if (!isAlive()) die();
    }

    void die();
}
