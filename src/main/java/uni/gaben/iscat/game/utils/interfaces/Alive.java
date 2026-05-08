package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità con salute: può subire danni e morire.
 *
 * takeDamage usa double per supportare danni frazionari (veleno, fuoco, ecc.)
 * senza perdere precisione. Il cast a int avviene solo al momento dell'applicazione.
 */
public interface Alive {

    int  getHp();
    int  getMaxHp();
    void setHp(int hp);

    /** {@code true} finché hp > 0. */
    default boolean isAlive() { return getHp() > 0; }

    /**
     * Riduce hp di {@code amount}; chiama {@link #die()} se raggiunge zero.
     * Usa double per supportare danni frazionari (veleno, DPS, ecc.).
     */
    default void takeDamage(double amount) {
        setHp(Math.max(0, (int) (getHp() - amount)));
        if (!isAlive()) die();
    }

    /** Chiamato quando hp arriva a 0. Sovrascrivere per effetti morte. */
    void die();
}
