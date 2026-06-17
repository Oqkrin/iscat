package uni.gaben.iscat.universe.entity.worm;

import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.EntityModel;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.EntityBrain;
import uni.gaben.iscat.universe.entity.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.target.Target;

public class WormAssembler {

    public static EntityModel assemble(
            String headKey, String bodyKey, String tailKey,
            int bodyCount, double startX, double startY,
            UniverseModel universe, UniverseController controller) {

        WormChainController chain = new WormChainController(controller, headKey, bodyKey, tailKey);
        EntityModel previousModel = null;

        // Testa
        EntityModel head = spawnSegment(headKey, startX, startY, universe, controller, chain, true, false);
        if (head != null) {
            previousModel = head;
        }

        // Parti del Corpo
        for (int i = 0; i < bodyCount; i++) {
            double offsetY = startY + ((i + 1) * 2.0);
            EntityModel body = spawnSegment(bodyKey, startX, offsetY, universe, controller, chain, false, false);

            if (body != null && previousModel != null) {
                EntityBrain bodyBrain = chain.getLatestBrain();
                if (bodyBrain != null) {
                    bodyBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofEntity(previousModel), 0.0));
                }
                previousModel = body;
            }
        }

        // Coda
        double tailOffsetY = startY + ((bodyCount + 1) * 2.0);
        EntityModel tail = spawnSegment(tailKey, startX, tailOffsetY, universe, controller, chain, false, true);
        if (tail != null && previousModel != null) {
            EntityBrain tailBrain = chain.getLatestBrain();
            if (tailBrain != null) {
                tailBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofEntity(previousModel), 0.0));
            }
        }

        // Aggiungiamo il gestore di catena al loop principale
        controller.addEntityController(chain);

        return head;
    }

    private static EntityModel spawnSegment(String key, double x, double y,
                                            UniverseModel universe, UniverseController controller,
                                            WormChainController chain, boolean isHead, boolean isTail) {

        EntityRecord record = EntityFactory.getCache().get(key.toLowerCase().trim());
        if (record == null) return null;

        EntityModel model = new EntityModel(x, y, record);
        EntityBrain brain = EntityBrain.fromRecord(model);

        universe.addEntity(model);
        controller.addEntityController(brain);

        // Aggiungi il segmento con i flag isHead e isTail
        chain.addSegment(model, brain, isHead, isTail);

        return model;
    }
}