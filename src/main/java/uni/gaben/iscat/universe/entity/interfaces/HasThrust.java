package uni.gaben.iscat.universe.entity.interfaces;

import uni.gaben.iscat.universe.Thrust;

public interface HasThrust {
    void updateThrust();

    Thrust thrustState();
}
