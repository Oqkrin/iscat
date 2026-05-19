package uni.gaben.iscat.game.universe.starfield;

import uni.gaben.iscat.game.universe.UniverseSettings;

import java.util.Random;

public class StarfieldController {
    private final Random rand = new Random();

    public void regenerate(StarfieldModel model, double width, double height) {
        model.clear();
        int starCount = (int) (width * height * UniverseSettings.STAR_DENSITY);
        
        for (int i = 0; i < starCount; i++) {
            double x = rand.nextDouble() * Math.max(1, width);
            double y = rand.nextDouble() * Math.max(1, height);
            double r = rand.nextDouble();
            double size = UniverseSettings.STAR_MIN_SIZE + r * r * UniverseSettings.STAR_MAX_SIZE_ADD;
            model.addStar(new StarModel(x, y, size));
        }
    }
}
