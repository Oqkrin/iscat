package uni.gaben.iscat.universe.rendering.starfield;

import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarfieldVFXModel extends AbstractEntityModel {
    private final List<StarVFXModel> stars = new ArrayList<>();

    public StarfieldVFXModel(double x, double y) {
        super(x, y);
    }

    public List<StarVFXModel> getStars() {
        return stars;
    }

    public void clear() {
        stars.clear();
    }

    public void addStar(StarVFXModel star) {
        stars.add(star);
    }


    public void generate(double width, double height) {
        Random rand = new Random();
        clear();
        int starCount = (int) (width * height * UniverseSettings.STAR_DENSITY);

        for (int i = 0; i < starCount; i++) {
            double x = rand.nextDouble() * Math.max(1, width);
            double y = rand.nextDouble() * Math.max(1, height);
            double r = rand.nextDouble();
            double size = UniverseSettings.STAR_MIN_SIZE + r * r * UniverseSettings.STAR_MAX_SIZE_ADD;
            addStar(new StarVFXModel(x, y, size));
        }
    }

}
