package uni.gaben.iscat.universe.entity.enemies.worm;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.entity.brain.goals.RotationGoal;

public class IscatWormSegmentBrain extends Brain<IscatWormSegment> {
    private final IscatWormSegment head;

    public IscatWormSegmentBrain(IscatWormSegment segment, IscatWormSegment head) {
        super(segment, MovementGoal.idle(),
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_FORCE : 0,
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_MAX_SPEED : 0,
                segment.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_ROTATION_SPEED : 0);

        this.head = head;

        if(segment.getType() == IscatWormSegment.Type.HEAD) setRotationGoal(RotationGoal.target(Target.ofPlayer()));
        if(segment.getType() == IscatWormSegment.Type.HEAD) setMovementGoal(MovementGoal.chase(Target.ofPlayer(), IscatWormSettings.HEAD_FORCE));


    }


}