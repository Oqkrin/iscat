package uni.gaben.iscat.gamenex.universe.hearth;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

import java.util.Random;

public class HearthController extends AiBehaviours<HearthModel> {
    Vector2 target = null;
    Random rand = new Random();
    Vector2 dirvec;
    double maxMagnitude = 40 / UniverseSettings.SCALE;
    double minMagnitude = 20 / UniverseSettings.SCALE;
    public HearthController(HearthModel hearth) {
        super(hearth);
    }

    // Hearth non si muove e non fa nulla, aspetta la collisione, pero potremmo fare che se il player si avvicina troppo, il hearth si avvicina per far avvenire la collisione
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);
    }
}

//TODO: Quando player collide con Hearth, Hearth scompare e da al player 50 HP come scritto su settings
//TODO: Quando un nemico muore, oppure un asteroide muore, hearth può apparire come drop