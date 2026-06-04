package uni.gaben.iscat.universe.entity.special.worm;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.RotationGoal;

public class IscatWormSegmentBrain extends Brain<IscatWormSegment> {
    private final IscatWormSegment head;

    public IscatWormSegmentBrain(IscatWormSegment segment, IscatWormSegment head) {
        super(segment, SteeringGoal.idle(),
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_FORCE : 0,
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_MAX_SPEED : 0,
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_ROTATION_SPEED : 0, 30);

        this.head = head;

        if(segment.getType() == IscatWormSegment.Type.HEAD) setRotationGoal(RotationGoal.target(Target.ofPlayer()));
        if(segment.getType() == IscatWormSegment.Type.HEAD) setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 2));


    }


}