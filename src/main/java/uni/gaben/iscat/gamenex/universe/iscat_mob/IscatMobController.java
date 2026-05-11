package uni.gaben.iscat.gamenex.universe.iscat_mob;

import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.LookAtBehavior;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.ObstacleAvoidanceBehavior;
import uni.gaben.iscat.gamenex.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public class IscatMobController extends AiBehaviours<IscatMobModel> {
    public IscatMobController(IscatMobModel iscat) {
        super(iscat);

        // 1. ROTAZIONE: Sempre attiva tramite la lista automatica
        addBehavior(new LookAtBehavior(
                IscatMobSettings.ROTATION_STIFFNESS,
                IscatMobSettings.ROTATION_DAMPING,
                IscatMobSettings.AI_ACCURACY
        ));

    }
    
}