package uni.gaben.iscat.universe.entity.interfaces;

import org.dyn4j.dynamics.PhysicsBody;
import org.dyn4j.dynamics.joint.Joint;

public interface HasJoint<S extends PhysicsBody, T extends Joint<S>> {
    T joint();
}
