package uni.gaben.iscat.gamenex.universe.iscat_mob;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class IscatMobController extends AiBehaviours<IscatMobModel> {
    Vector2 target = null;
    Random rand = new Random();
    Vector2 dirvec;
    double maxMagnitude = 40 / UniverseSettings.SCALE;
    double minMagnitude = 20 / UniverseSettings.SCALE;
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
            double currentDir = npc.getTransform().getRotationAngle();
            target = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5*Math.PI + rand.nextDouble(1.5*Math.PI));
        } else {
            if (npc.getLinearVelocity().getMagnitude() <= IscatMobSettings.MAX_VELOCITY_MS) {
                dirvec = npc.getTransform().getTranslation().to(target);
                npc.getTransform().setRotation(
                        Interpolator.smootherStep(npc.getTransform().getRotationAngle(),dirvec.getDirection(),
                                1-(1/dirvec.getMagnitude())
                        ));
                npc.applyForce(dirvec.getNormalized().multiply(IscatMobSettings.FORCE));
            } else {
                npc.setLinearVelocity(npc.getLinearVelocity().setMagnitude(IscatMobSettings.MAX_VELOCITY_MS));
            }
            if(npc.contains(target)) {
                target = null;
            }
        }
    }
}