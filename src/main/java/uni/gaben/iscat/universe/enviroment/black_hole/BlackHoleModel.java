package uni.gaben.iscat.universe.enviroment.black_hole;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class BlackHoleModel extends AbstractEntityModel {

    protected BlackHoleModel(double x, double y) {
        super(x, y);
        BodyFixture body = addFixture(Geometry.createCircle(0));
    }
}
