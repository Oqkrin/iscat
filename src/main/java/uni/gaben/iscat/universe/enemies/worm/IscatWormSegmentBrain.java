package uni.gaben.iscat.universe.enemies.worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.player.PlayerModel;

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