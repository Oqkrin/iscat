package uni.gaben.iscat.universe.effects;

import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.EntityRecordBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Starfield extends AbstractEntityModel {
    private final List<Star> stars = new ArrayList<>();

    public Starfield(double x, double y) {
        super(x, y, new EntityRecordBuilder().build());
    }

    public List<Star> getStars() {
        return stars;
    }

    public void clear() {
        stars.clear();
    }

    public void addStar(Star star) {
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
            addStar(new Star(x, y, size));
        }
    }

}
