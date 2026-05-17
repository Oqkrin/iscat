package uni.gaben.iscat.gamenex.universe.starfield;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;

import java.util.ArrayList;
import java.util.List;

public class StarfieldModel extends AbstractEntityModel {
    private final List<StarModel> stars = new ArrayList<>();

    public StarfieldModel(double x, double y) {
        super(x, y);
    }

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
