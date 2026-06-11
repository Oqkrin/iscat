package uni.gaben.iscat.universe.entity.interfaces;

public interface Progression {
    int getLevel();

    void incrementExperience(double amount);

    default void levelUp() {
        setLevel(getLevel() + 1);
    };

    void setLevel(int level);
    double getNeededXpFor(int level);

    default double xpForLevelUp() {
        return getNeededXpFor(getLevel()) - getExperience();
    }
    double getExperience();
    void setExperience(double experience);
}
