package uni.gaben.iscat.universe.enemies.worm;

import org.dyn4j.dynamics.joint.DistanceJoint;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.ArrayList;
import java.util.List;

public class IscatWormModel {  // NON estende LivingEntityModel

    private final List<IscatWormSegment> segments = new ArrayList<>();

    public IscatWormModel(double x, double y) {
        createFullWorm(x, y);
    }

    private void createFullWorm(double x, double y) {
        double spacing = UU.pxToM(IscatWormSettings.SEGMENT_SPACING_PX);

        IscatWormSegment head = new IscatWormSegment(IscatWormSegment.Type.HEAD, x, y);
        segments.add(head);
        IscatWormSegment previous = head;

        for (int i = 0; i < IscatWormSettings.INITIAL_SEGMENTS - 2; i++) {
            IscatWormSegment body = new IscatWormSegment(
                    IscatWormSegment.Type.BODY, x - (i + 1) * spacing, y);
            body.setPreviousSegment(previous); // ← era mancante
            segments.add(body);
            previous = body;
        }

        IscatWormSegment tail = new IscatWormSegment(
                IscatWormSegment.Type.TAIL, x - (IscatWormSettings.INITIAL_SEGMENTS - 1) * spacing, y);
        tail.setPreviousSegment(previous); // ← era mancante
        segments.add(tail);
    }

    // In IscatWormModel
    public void connectSegments(UniverseModel universe) {
        for (int i = 0; i < segments.size() - 1; i++) {
            IscatWormSegment a = segments.get(i);
            IscatWormSegment b = segments.get(i + 1);

            DistanceJoint joint = new DistanceJoint(a, b,
                    a.getPosition(), b.getPosition());
            joint.setSpringFrequency(IscatWormSettings.JOINT_FREQUENCY);  // e.g., 5.0
            joint.setSpringDampingRatio(IscatWormSettings.JOINT_DAMPING); // e.g., 0.7
            joint.setRestDistance(IscatWormSettings.SEGMENT_SPACING_M);
            universe.addJoint(joint);
        }
    }



    public List<IscatWormSegment> getSegments() { return segments; }

    public IscatWormSegment getHead() {
        return segments.stream().filter(s -> !s.isConsumed()).findFirst().orElse(null);
    }
}