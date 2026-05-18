package uni.gaben.iscat.gamenex.universe.iscat_mob;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class IscatMobController extends AiBehaviours<IscatMobModel> {
    Vector2 target = null;
    Random rand = new Random();
    Vector2 dirvec;
    double maxMagnitude = 2;
    double minMagnitude = 1;
    public IscatMobController(IscatMobModel iscat) {
        super(iscat);

        // ROTAZIONE: Sempre attiva tramite la lista automatica
       /*
        addBehavior(new LookAtBehavior(
                IscatMobSettings.ROTATION_STIFFNESS,
                IscatMobSettings.ROTATION_DAMPING,
                IscatMobSettings.AI_ACCURACY
        ));
        */
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (target == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            target = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5*Math.PI + rand.nextDouble(1.5*Math.PI));
        } else {
            aiEntity.getTransform().setRotation(
                    Interpolator.smootherStep(aiEntity.getTransform().getRotationAngle(),target.getDirection(),
                            1-(1/target.getMagnitude())
                    ));
            aiEntity.applyForce(target.getNormalized().multiply(IscatMobSettings.FORCE));

            if(aiEntity.contains(target)) {
                target = null;
            }
        }
    }
}