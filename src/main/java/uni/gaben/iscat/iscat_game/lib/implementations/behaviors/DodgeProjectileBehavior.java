package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;
import java.util.List;

public class DodgeProjectileBehavior implements AiBehavior {

    private final double dodgeForce;
    private final double postDodgeCooldownSeconds;
    private final double detectionRadius = 15.0; // Distanza a cui vede il proiettile
    private final Cooldown dodgeCooldown = new Cooldown();
    
    public DodgeProjectileBehavior(double dodgeForce, double cooldownSeconds, double postDodgeCooldownSeconds) {
        this.dodgeForce = dodgeForce;
        this.postDodgeCooldownSeconds = postDodgeCooldownSeconds;
        this.dodgeCooldown.start(cooldownSeconds);
    }

    public DodgeProjectileBehavior(double dodgeForce, double cooldownSeconds) {
        this(dodgeForce, cooldownSeconds, 2.0); // Default 2.0s
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        // Return -1.0 to execute as a parallel behavior, avoiding starvation of main states like Chase
        return -1.0; 
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        dodgeCooldown.update(dt);

        Vector2 npcPos = npc.getTransform().getTranslation();
        Vector2 npcForward = new Vector2(npc.getTransform().getRotationAngle());

        // Lanciamo un raggio in avanti per vedere se c'è un proiettile nella nostra traiettoria
        Ray ray = new org.dyn4j.geometry.Ray(npcPos, npcForward);
        List<RaycastResult<Body, BodyFixture>> results = universe.raycast(ray, detectionRadius, null);

        Projectile closestIncoming = null;
        double closestDist = Double.MAX_VALUE;

        if (results != null) {
            for (RaycastResult<Body, BodyFixture> result : results) {
                Body b = result.getBody();
                if (b instanceof Projectile proj && proj.getType() == ProjectileType.PLAYER_BULLET) {
                    double dist = proj.getTransform().getTranslation().distance(npcPos);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestIncoming = proj;
                    }
                }
            }
        }

        if (closestIncoming != null) {
            // Calcola la direzione ortogonale per la schivata
            Vector2 dodgeDir1 = new Vector2(-npcForward.y, npcForward.x);
            Vector2 dodgeDir2 = new Vector2(npcForward.y, -npcForward.x);
            
            // Ally awareness: calcola il centro di massa degli alleati vicini per evitare di schivare addosso a loro
            Vector2 crowdCenter = new Vector2();
            int crowdCount = 0;
            List<? extends AbstractEntityModel> allies = universe.getEntitiesOfType(npc.getClass());
            for (AbstractEntityModel ally : allies) {
                if (ally == npc) continue;
                Vector2 allyPos = ally.getTransform().getTranslation();
                if (allyPos.distance(npcPos) < 10.0) {
                    crowdCenter.add(allyPos);
                    crowdCount++;
                }
            }
            
            Vector2 dodgeDir;
            if (crowdCount > 0) {
                crowdCenter.divide(crowdCount);
                Vector2 toCrowd = crowdCenter.subtract(npcPos).getNormalized();
                // Scegliamo la direzione che si allontana maggiormente dal centro della folla
                if (dodgeDir1.dot(toCrowd) < dodgeDir2.dot(toCrowd)) {
                    dodgeDir = dodgeDir1;
                } else {
                    dodgeDir = dodgeDir2;
                }
            } else {
                // Sceglie la direzione a caso se non c'è folla
                dodgeDir = Math.random() > 0.5 ? dodgeDir1 : dodgeDir2;
            }
            
            double DASH_TRIGGER_DIST = 6.0; // Distanza a cui il dash scatta "all'ultimo momento"
            
            if (!dodgeCooldown.isCoolingDown() && closestDist < DASH_TRIGGER_DIST) {
                // Dash esplosivo all'ultimo momento
                npc.applyImpulse(dodgeDir.multiply(dodgeForce));
                dodgeCooldown.start(postDodgeCooldownSeconds);
            } else {
                // Evasione continua: cammina via (applica una forza laterale)
                // Usiamo una forza continua per "allontanarci" mentre aspettiamo il cooldown o il proiettile
                npc.setAtRest(false);
                npc.applyForce(dodgeDir.multiply(dodgeForce * 3.0)); 
            }
        }
    }
}
