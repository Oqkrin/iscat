package uni.gaben.iscat.universe.entity.hardcoded.worm;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.target.Target;
import uni.gaben.iscat.universe.entity.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.rotation.RotationGoal;

public class IscatWormSegmentBrain extends Brain<IscatWormSegment> {
    private final IscatWormSegment head;

    public IscatWormSegmentBrain(IscatWormSegment segment, IscatWormSegment head) {
        super(segment);

        this.head = head;

        if(segment.getType() == IscatWormSegment.Type.HEAD) setRotationGoal(RotationGoal.target(Target.ofPlayer()));
        if(segment.getType() == IscatWormSegment.Type.HEAD) setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 2));


    }


}