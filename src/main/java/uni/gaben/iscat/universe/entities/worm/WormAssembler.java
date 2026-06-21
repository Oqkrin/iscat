package uni.gaben.iscat.universe.entities.worm;

import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityBrain;
import uni.gaben.iscat.universe.entities.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.target.Target;

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
            EntityBrain headBrain = chain.getLatestBrain();
            if (headBrain != null) {
                headBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 1.0));
            }
            previousModel = head;
        }

        // Parti del Corpo
        for (int i = 0; i < bodyCount; i++) {
            double offsetY = startY + ((i + 1) * 2.0);
            EntityModel body = spawnSegment(bodyKey, startX, offsetY, universe, controller, chain, false, false);

            if (body != null && previousModel != null) {
                EntityBrain bodyBrain = chain.getLatestBrain();
                if (bodyBrain != null) {
                    bodyBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofEntity(previousModel), .5));
                    bodyBrain.setRotationGoal(RotationGoal.target(Target.ofEntity(previousModel)));
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

        // Applica il filtro worm a tutti i segmenti
        model.setCollisionFilter(UniverseCollisionLayers.WORM_BODY_FILTER);

        universe.addEntity(model);
        controller.addEntityController(brain);
        chain.addSegment(model, brain, isHead, isTail);

        return model;
    }
}