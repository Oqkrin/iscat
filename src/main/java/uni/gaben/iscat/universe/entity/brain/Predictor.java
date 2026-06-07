package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import java.util.List;

public final class Predictor {
    private static final double ONEOVERSQRT2 = 1.0 / Math.sqrt(2.0);

    private Predictor() {
        // Utility class
    }

    /**
     * Extrapolates a target's position based on a given look-ahead time.
     * Works uniformly for both physical entities and static/dynamic points without allocations.
     */
    public static Vector2 extrapolate(Target target, UniverseModel universe, double lookAheadTime, Vector2 out) {
        List<AbstractEntityModel> entities = target.getEntities(universe);
        if (entities == null || entities.isEmpty()) {
            Vector2 pos = target.getPosition(universe);
            return pos != null ? out.set(pos) : out.set(0, 0);
        }

        AbstractEntityModel entity = entities.getFirst();
        if (entity == null || entity.shouldRemove()) {
            Vector2 pos = target.getPosition(universe);
            return pos != null ? out.set(pos) : out.set(0, 0);
        }

        Vector2 currentPos = entity.getTransform().getTranslation();
        Vector2 velocity = entity.getLinearVelocity();
        return out.set(
                currentPos.x + (velocity.x * lookAheadTime),
                currentPos.y + (velocity.y * lookAheadTime)
        );
    }

    /**
     * Calculates baseline direct travel time based on constant projectile/actor velocity.
     */
    public static double calculateDirectTime(Vector2 selfPos, Vector2 targetPos, double speedBaseline) {
        double distance = selfPos.distance(targetPos);
        return speedBaseline > 0.001 ? distance / speedBaseline : 0.0;
    }

    /**
     * Empirical OpenSteer 9-Case Matrix for calculating refined pursuit look-ahead weights.
     */
    public static double calculatePursuitTime(Vector2 selfPos, double selfAngle,
                                              Vector2 targetPos, double targetAngle,
                                              double currentSpeed, double maxVelocity) {
        double distance = selfPos.distance(targetPos);
        if (distance < 0.001) return 0.0;

        // Deriving forward orientations
        double selfForwardX = Math.cos(selfAngle);
        double selfForwardY = Math.sin(selfAngle);
        double targetForwardX = Math.cos(targetAngle);
        double targetForwardY = Math.sin(targetAngle);

        // Normalized direction vector heading towards target
        double offsetX = targetPos.x - selfPos.x;
        double offsetY = targetPos.y - selfPos.y;
        double unitOffsetX = offsetX / distance;
        double unitOffsetY = offsetY / distance;

        // Geometric alignment products
        double parallelness = (selfForwardX * targetForwardX) + (selfForwardY * targetForwardY);
        double forwardness = (selfForwardX * unitOffsetX) + (selfForwardY * unitOffsetY);

        double speedBaseline = currentSpeed > 0.1 ? currentSpeed : maxVelocity;
        double directTravelTime = distance / speedBaseline;

        int f = intervalComparison(forwardness, -ONEOVERSQRT2, ONEOVERSQRT2);
        int p = intervalComparison(parallelness, -ONEOVERSQRT2, ONEOVERSQRT2);

        double timeFactor = switch (f) {
            case 1 -> switch (p) {
                case 1 -> 4.0;   // Ahead, Parallel
                case 0 -> 1.8;   // Ahead, Perpendicular
                case -1 -> 0.85; // Ahead, Anti-Parallel
                default -> 0.0;
            };
            case 0 -> switch (p) {
                case 1 -> 1.0;   // Aside, Parallel
                case 0 -> 0.8;   // Aside, Perpendicular
                case -1 -> 4.0;  // Aside, Anti-Parallel
                default -> 0.0;
            };
            case -1 -> switch (p) {
                case 1 -> 0.5;   // Behind, Parallel
                case 0 -> 2.0;   // Behind, Perpendicular
                case -1 -> 2.0;  // Behind, Anti-Parallel
                default -> 0.0;
            };
            default -> 0.0;
        };

        return directTravelTime * timeFactor;
    }

    /**
     * Calculates capabilities-proportional look-ahead time for evasion behaviors.
     */
    public static double calculateEvadeTime(Vector2 selfPos, Vector2 targetPos, double selfMaxVelocity, double threatSpeed) {
        double distance = targetPos.distance(selfPos);
        double closingSpeed = selfMaxVelocity + threatSpeed;
        return closingSpeed > 0.001 ? distance / closingSpeed : 0.0;
    }

    private static int intervalComparison(double value, double lower, double upper) {
        if (value < lower) return -1;
        if (value > upper) return 1;
        return 0;
    }
}