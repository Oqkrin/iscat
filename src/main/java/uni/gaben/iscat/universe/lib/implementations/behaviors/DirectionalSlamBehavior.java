package uni.gaben.iscat.universe.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

public class DirectionalSlamBehavior implements AiBehavior {

    private final double maxDist;
    private final double slamForce;
    private final double slamDurationSeconds;
    private final double coneTolerance = 0.95; // ~18 gradi
    private final Cooldown slamCooldown = new Cooldown();
    
    private boolean isSlamming = false;
    private double slamTimer = 0.0;

    public DirectionalSlamBehavior(double maxDist, double slamForce, double cooldownSeconds, double slamDurationSeconds) {
        this.maxDist = maxDist;
        this.slamForce = slamForce;
        this.slamDurationSeconds = slamDurationSeconds;
        this.slamCooldown.start(cooldownSeconds);
    }

    public DirectionalSlamBehavior(double maxDist, double slamForce, double cooldownSeconds) {
        this(maxDist, slamForce, cooldownSeconds, 0.6); // Default 0.6s
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        if (isSlamming) return 95.0;
        
        if (!slamCooldown.isCoolingDown()) {
            PlayerModel player = universe.getPlayer();
            if (player != null) {
                Vector2 npcPos = npc.getTransform().getTranslation();
                Vector2 playerPos = player.getTransform().getTranslation();
                double dist = playerPos.distance(npcPos);
                
                if (dist <= maxDist) {
                    Vector2 dir = playerPos.copy().subtract(npcPos).getNormalized();
                    double rot = npc.getTransform().getRotationAngle();
                    
                    // Controlla allineamento con i 4 assi (front, back, left, right)
                    Vector2[] axes = {
                        new Vector2(Math.cos(rot), Math.sin(rot)), // front
                        new Vector2(-Math.cos(rot), -Math.sin(rot)), // back
                        new Vector2(-Math.sin(rot), Math.cos(rot)), // left
                        new Vector2(Math.sin(rot), -Math.cos(rot)) // right
                    };
                    
                    for (Vector2 axis : axes) {
                        if (dir.dot(axis) > coneTolerance) {
                            return 95.0;
                        }
                    }
                }
            }
        }
        return -1.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        slamCooldown.update(dt);
        PlayerModel player = universe.getPlayer();
        if (player == null) return;
        
        if (!isSlamming && !slamCooldown.isCoolingDown()) {
            isSlamming = true;
            slamTimer = slamDurationSeconds;
            
            Vector2 npcPos = npc.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            Vector2 dir = playerPos.copy().subtract(npcPos).getNormalized();
            
            // Fissa la direzione ad uno dei 4 assi
            double rot = npc.getTransform().getRotationAngle();
            Vector2[] axes = {
                new Vector2(Math.cos(rot), Math.sin(rot)),
                new Vector2(-Math.cos(rot), -Math.sin(rot)),
                new Vector2(-Math.sin(rot), Math.cos(rot)),
                new Vector2(Math.sin(rot), -Math.cos(rot))
            };
            
            Vector2 bestAxis = axes[0];
            double maxDot = -2.0;
            for (Vector2 axis : axes) {
                double dot = dir.dot(axis);
                if (dot > maxDot) {
                    maxDot = dot;
                    bestAxis = axis;
                }
            }
            
            npc.applyImpulse(bestAxis.multiply(slamForce));
            slamCooldown.start(4.0);
        } else if (isSlamming) {
            slamTimer -= dt;
            if (slamTimer <= 0) {
                isSlamming = false;
            }
        }
    }
}
