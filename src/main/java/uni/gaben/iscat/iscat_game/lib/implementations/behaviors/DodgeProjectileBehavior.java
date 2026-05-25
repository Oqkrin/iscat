package uni.gaben.iscat.iscat_game.lib.implementations.behaviors;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

public class DodgeProjectileBehavior implements AiBehavior {

    private final double dodgeForce;
    private final double detectionRadius = 15.0; // Distanza a cui vede il proiettile
    private final Cooldown dodgeCooldown = new Cooldown();
    
    public DodgeProjectileBehavior(double dodgeForce, double cooldownSeconds) {
        this.dodgeForce = dodgeForce;
        this.dodgeCooldown.start(cooldownSeconds);
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        if (!dodgeCooldown.isCoolingDown()) {
            return 80.0; // Alta priorità se può schivare
        }
        return -1.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        dodgeCooldown.update(dt);
        if (dodgeCooldown.isCoolingDown()) return;

        Vector2 npcPos = npc.getTransform().getTranslation();
        Projectile incoming = null;
        double closestDist = Double.MAX_VALUE;

        for (AbstractProjectileModel p : universe.getProjectiles()) {
            if (p instanceof Projectile proj && proj.getType() == ProjectileType.PLAYER_BULLET) {
                Vector2 projPos = proj.getTransform().getTranslation();
                Vector2 projVel = proj.getLinearVelocity();
                
                // Se il proiettile è fermo ignoralo
                if (projVel.getMagnitudeSquared() < 0.1) continue;

                double dist = projPos.distance(npcPos);
                if (dist < detectionRadius && dist < closestDist) {
                    // Controlla se il proiettile sta andando verso l'NPC
                    Vector2 toNpc = npcPos.copy().subtract(projPos).getNormalized();
                    Vector2 projDir = projVel.copy().getNormalized();
                    
                    if (toNpc.dot(projDir) > 0.8) { // Proiettile sta puntando (angolo < ~36 gradi) verso npc
                        closestDist = dist;
                        incoming = proj;
                    }
                }
            }
        }

        if (incoming != null) {
            // Calcola la direzione ortogonale per la schivata
            Vector2 projDir = incoming.getLinearVelocity().getNormalized();
            // Orto: (-y, x) o (y, -x)
            Vector2 dodgeDir1 = new Vector2(-projDir.y, projDir.x);
            Vector2 dodgeDir2 = new Vector2(projDir.y, -projDir.x);
            
            // Sceglie la direzione più libera o a caso
            Vector2 dodgeDir = Math.random() > 0.5 ? dodgeDir1 : dodgeDir2;
            
            npc.applyImpulse(dodgeDir.multiply(dodgeForce));
            dodgeCooldown.start(2.0); // Reset cooldown dopo aver schivato
        }
    }
}
