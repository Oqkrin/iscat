package uni.gaben.iscat.gamenex.universe.black_hole;

import javafx.beans.property.DoubleProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;

public class BlackHoleModel extends AbstractEntityModel {

    protected BlackHoleModel(double x, double y) {
        super(x, y);
        BodyFixture body = addFixture(Geometry.createCircle(0));
    }
}
