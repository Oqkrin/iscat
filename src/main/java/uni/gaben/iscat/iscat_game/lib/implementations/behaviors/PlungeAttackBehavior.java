package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

public class PlungeAttackBehavior implements AiBehavior {

    private final double triggerRadius;
    private final double plungeForce;
    private final double plungeDurationSeconds;
    private final Cooldown plungeCooldown = new Cooldown();
    
    // Stato del plunge
    private boolean isPlunging = false;
    private Vector2 plungeTarget = null;
    private double plungeDuration = 0.0;
    
    public PlungeAttackBehavior(double triggerRadius, double plungeForce, double cooldownSeconds, double plungeDurationSeconds) {
        this.triggerRadius = triggerRadius;
        this.plungeForce = plungeForce;
        this.plungeDurationSeconds = plungeDurationSeconds;
        this.plungeCooldown.start(cooldownSeconds); // Starts empty
    }

    public PlungeAttackBehavior(double triggerRadius, double plungeForce, double cooldownSeconds) {
        this(triggerRadius, plungeForce, cooldownSeconds, 0.5); // Default 0.5s
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        if (isPlunging) return 90.0; // Altissima priorità se in corso
        
        if (!plungeCooldown.isCoolingDown()) {
            PlayerModel player = universe.getPlayer();
            if (player != null) {
                double dist = player.getTransform().getTranslation().distance(npc.getTransform().getTranslation());
                if (dist <= triggerRadius) {
                    return 90.0; // Trigger plunge
                }
            }
        }
        return -1.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        plungeCooldown.update(dt);
        PlayerModel player = universe.getPlayer();
        if (player == null) return;
        
        if (!isPlunging && !plungeCooldown.isCoolingDown()) {
            // Inizia il plunge
            isPlunging = true;
            plungeTarget = player.getTransform().getTranslation().copy();
            plungeDuration = plungeDurationSeconds;
            
            // Applica la forza
            Vector2 npcPos = npc.getTransform().getTranslation();
            Vector2 dir = plungeTarget.copy().subtract(npcPos).getNormalized();
            npc.applyImpulse(dir.multiply(plungeForce));
            plungeCooldown.start(3.0); // 3 secondi di cooldown dopo il plunge
            
            // Ruota verso il target
            double targetAngle = dir.getDirection();
            npc.getTransform().setRotation(targetAngle);
        } else if (isPlunging) {
            plungeDuration -= dt;
            if (plungeDuration <= 0) {
                isPlunging = false;
            } else {
                // Precision homing: course-correct slightly towards the player during the dash
                Vector2 npcPos = npc.getTransform().getTranslation();
                Vector2 targetPos = player.getTransform().getTranslation();
                Vector2 dir = targetPos.copy().subtract(npcPos).getNormalized();
                
                // Applica una forza di correzione per seguire il player
                npc.applyForce(dir.multiply(plungeForce * 0.5));
                
                // Aggiorna la rotazione visiva per guardare il player
                double targetRot = dir.getDirection();
                double cur = npc.getTransform().getRotationAngle();
                double diff = targetRot - cur;
                while(diff < -Math.PI) diff += Math.PI*2;
                while(diff > Math.PI) diff -= Math.PI*2;
                npc.getTransform().setRotation(cur + diff * dt * 10.0);
            }
        }
    }
}
