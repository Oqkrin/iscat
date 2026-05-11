package uni.gaben.iscat.gamenex.lib.abstracts;

import uni.gaben.iscat.gamenex.lib.interfaces.model.Mortal;

public abstract class AbstractProjectileModel extends AbstractEntityModel implements Mortal {
    protected AbstractProjectileModel() {
        setBullet(true);
    }
}
