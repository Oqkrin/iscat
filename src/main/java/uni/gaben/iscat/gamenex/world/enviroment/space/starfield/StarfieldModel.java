package uni.gaben.iscat.gamenex.world.enviroment.space.starfield;

import uni.gaben.iscat.gamenex.interfaces.model.AbstractEntityModel;

import java.util.ArrayList;
import java.util.List;

public class StarfieldModel extends AbstractEntityModel {
    private final List<StarModel> stars = new ArrayList<>();

    public List<StarModel> getStars() {
        return stars;
    }

    public void clear() {
        stars.clear();
    }

    public void addStar(StarModel star) {
        stars.add(star);
    }
}
