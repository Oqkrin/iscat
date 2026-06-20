package uni.gaben.iscat.universe.entities.worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.EntityBrain;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.brain.IEntityController;
import uni.gaben.iscat.universe.entities.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entities.brain.target.Target;

import java.util.ArrayList;
import java.util.List;

public class WormChainController implements IEntityController {

    public static class WormSegmentData {
        public EntityModel model;
        public EntityBrain brain;
        public boolean isHead;
        public boolean isTail;

        public WormSegmentData(EntityModel model, EntityBrain brain, boolean isHead, boolean isTail) {
            this.model = model;
            this.brain = brain;
            this.isHead = isHead;
            this.isTail = isTail;
        }
    }

    private final List<WormSegmentData> segments = new ArrayList<>();
    private final UniverseController universeController;
    private final String headKey;
    private final String bodyKey;
    private final String tailKey;

    private static final double SEGMENT_SPACING = 1.5;

    // Costruttore
    public WormChainController(UniverseController universeController, String headKey, String bodyKey, String tailKey) {
        this.universeController = universeController;
        this.headKey = headKey;
        this.bodyKey = bodyKey;
        this.tailKey = tailKey;
    }

    public void addSegment(EntityModel model, EntityBrain brain, boolean isHead, boolean isTail) {
        if (!isHead && brain != null) {
            brain.setEnabled(false);
        }
        segments.add(new WormSegmentData(model, brain, isHead, isTail));
    }

    public EntityBrain getLatestBrain() {
        if (segments.isEmpty()) return null;
        return segments.getLast().brain;
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (segments.isEmpty()) return;

        // Rilevamento morti
        int deadIndex = -1;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i).model.shouldRemove()) {
                deadIndex = i;
                break;
            }
        }

        // Gestione rottura o mutazione
        if (deadIndex != -1) {
            handleChainBreak(universe, deadIndex);
        }

        if (segments.isEmpty()) return;

        // Forza i segmenti corpo e coda a seguire come una corda
        for (int i = 1; i < segments.size(); i++) {
            WormSegmentData current = segments.get(i);
            EntityModel leader = segments.get(i - 1).model;

            Vector2 currentPos = current.model.getTransform().getTranslation();
            Vector2 leaderPos = leader.getTransform().getTranslation();

            Vector2 toLeader = leaderPos.copy().subtract(currentPos);
            double distance = toLeader.getMagnitude();

            if (distance > 0.001) {
                Vector2 direction = toLeader.getNormalized();
                Vector2 desiredPos = leaderPos.copy().subtract(direction.multiply(SEGMENT_SPACING));

                current.model.getTransform().setTranslation(desiredPos.x, desiredPos.y);
                current.model.getLinearVelocity().set(leader.getLinearVelocity());
            }
        }
    }

    private void handleChainBreak(UniverseModel universe, int deadIndex) {
        WormSegmentData deadSegment = segments.get(deadIndex);

        // CASO 1: Muore la testa (indice 0)
        if (deadIndex == 0 && deadSegment.isHead) {
            segments.removeFirst();

            if (!segments.isEmpty()) {
                WormSegmentData newHead = segments.getFirst();
                String newHeadKey = newHead.model.getEntityRecord().entityKey();

                if (newHeadKey.equals(bodyKey)) {
                    // solo il corpo può diventare testa
                    promoteToHead(universe, newHead);
                } else if (newHeadKey.equals(tailKey)) {
                    // La coda non può diventare una testa
                    // Attiva il suo brain originale e lasciala vivere da sola
                    activateTailBrain(newHead);
                    segments.clear();
                }
            }
            return;
        }

        // CASO 2: Muore un segmento centrale (taglio a metà)
        if (deadIndex > 0 && deadIndex < segments.size()-1) {
            List<WormSegmentData> backPart = new ArrayList<>(
                    segments.subList(deadIndex, segments.size())
            );

            backPart.removeFirst();
            segments.subList(deadIndex, segments.size()).clear();

            if (!backPart.isEmpty()) {
                handleBackPart(universe, backPart);
            }
        }
    }

    private void promoteToHead(UniverseModel universe, WormSegmentData segment) {
        if (!segment.model.getEntityRecord().entityKey().equals(bodyKey)) {
            return;
        }

        Vector2 currentPos = segment.model.getTransform().getTranslation().copy();
        double currentRotation = segment.model.getTransform().getRotationAngle();
        Vector2 currentVelocity = segment.model.getLinearVelocity().copy();

        segment.model.completeKill();
        universe.removeEntity(segment.model);

        EntityRecord headRecord = EntityFactory.getCache().get(headKey.toLowerCase().trim());
        if (headRecord != null) {
            EntityModel newHeadModel = new EntityModel(currentPos.x, currentPos.y, headRecord);
            EntityBrain newHeadBrain = EntityBrain.fromRecord(newHeadModel);

            newHeadModel.getTransform().setTranslation(currentPos.x, currentPos.y);
            newHeadModel.getTransform().setRotation(currentRotation);
            newHeadModel.getLinearVelocity().set(currentVelocity);

            newHeadBrain.setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 2.0));

            universe.addEntity(newHeadModel);
            universeController.addEntityController(newHeadBrain);

            segment.model = newHeadModel;
            segment.brain = newHeadBrain;
            segment.isHead = true;
            segment.isTail = false;
        }
    }

    private void activateTailBrain(WormSegmentData tailSegment) {
        EntityBrain newBrain = EntityBrain.fromRecord(tailSegment.model);
        tailSegment.brain.setSteeringGoal(newBrain.getSteeringGoal());
        tailSegment.brain.setRotationGoal(newBrain.getRotationGoal());
        tailSegment.brain.setEnabled(true);
        tailSegment.isHead = false;
        tailSegment.isTail = true;
    }

    private void handleBackPart(UniverseModel universe, List<WormSegmentData> backPart) {
        WormSegmentData firstSegment = backPart.getFirst();
        String firstKey = firstSegment.model.getEntityRecord().entityKey();

        if (firstKey.equals(bodyKey)) {
            createNewWormFromSegments(universe, backPart);
        } else if (firstKey.equals(tailKey)) {
            activateTailBrain(firstSegment);

            for (int i = 1; i < backPart.size(); i++) {
                WormSegmentData segment = backPart.get(i);
                segment.model.completeKill();
                universe.removeEntity(segment.model);
            }
        }
    }

    private void createNewWormFromSegments(UniverseModel universe, List<WormSegmentData> newSegments) {
        if (newSegments.isEmpty()) return;

        // Passa le stesse chiavi al nuovo worm
        WormChainController newWormChain = new WormChainController(universeController, headKey, bodyKey, tailKey);

        WormSegmentData firstSegment = newSegments.getFirst();
        String firstKey = firstSegment.model.getEntityRecord().entityKey();

        if (firstKey.equals(bodyKey)) {
            promoteToHead(universe, firstSegment);
        } else {
            return;
        }

        newWormChain.addSegment(firstSegment.model, firstSegment.brain, true, false);

        for (int i = 1; i < newSegments.size(); i++) {
            WormSegmentData segment = newSegments.get(i);
            boolean isTail = segment.model.getEntityRecord().entityKey().equals(tailKey);
            newWormChain.addSegment(segment.model, segment.brain, false, isTail);
        }

        universeController.addEntityController(newWormChain);
    }
}