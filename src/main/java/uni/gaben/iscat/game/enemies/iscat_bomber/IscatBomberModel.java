package uni.gaben.iscat.game.enemies.iscat_bomber;

import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.GameSettings;
import uni.gaben.iscat.game.enemies.EnemyModel;
import uni.gaben.iscat.game.player.PlayerModel;
import uni.gaben.iscat.game.interfaces.AI;
import uni.gaben.iscat.game.interfaces.Collidable;
import uni.gaben.iscat.game.physics.Vec2;
import uni.gaben.iscat.game.player.PlayerSettings;
import uni.gaben.iscat.utils.Cooldown;

import java.util.ArrayList;
import java.util.List;

/**
 * IscatBomberModel: nemico che segue le vecchie posizioni del giocatore con interpolazione smooth.
 * Quando collide con il giocatore, entrambi vengono respinti e il bomber entra in stun.
 */
public class IscatBomberModel extends EnemyModel implements Collidable, AI {
    
    private final List<Vec2> playerTrail = new ArrayList<>();
    private final Cooldown stunTimer = new Cooldown(); // timer di stun dopo collisione
    private final Cooldown collisionCooldown = new Cooldown(); // cooldown per evitare collisioni ripetute

    public IscatBomberModel(double startX, double startY) {
        super(startX, startY);
        this.hp    = IscatBomberSettings.HP;
        this.maxHp = IscatBomberSettings.HP;
        this.mass  = PlayerSettings.MASSA * IscatBomberSettings.FATTORE_MASSA;
        this.name  = "IscatBomber";
        this.spriteSize = PlayerSettings.DIMENSIONE_SPRITE;
        this.drag     = IscatBomberSettings.ATTRITO;
        this.maxSpeed = PlayerSettings.VELOCITA_MAX * IscatBomberSettings.FATTORE_VELOCITA_MAX;
        this.deadZone = 0.01;
    }

    // --- AI interface ---

    @Override
    public void updateAI(GameModel model, double dt) {
        // Decrementa cooldowns
        stunTimer.tick();
        collisionCooldown.tick();
        
        if (stunTimer.isActive()) {
            return; // non seguire durante lo stun
        }
        
        // Aggiorna trail del player
        Vec2 playerPos = model.getPlayer().getPosition();
        playerTrail.add(playerPos);
        
        // Mantieni solo le ultime N posizioni
        if (playerTrail.size() > IscatBomberSettings.LUNGHEZZA_TRAIL) {
            playerTrail.remove(0);
        }
        
        // Segui il player
        followPlayer();
    }
    
    @Override
    public void resetAI() {
        playerTrail.clear();
        stunTimer.reset();
        collisionCooldown.reset();
    }

    // --- AI logic ---

    /**
     * Logica di inseguimento: segue punti nella trail del player.
     */
    private void followPlayer() {
        if (playerTrail.size() <= IscatBomberSettings.RITARDO_TRAIL) {
            return;
        }
        
        int targetIndex = playerTrail.size() - IscatBomberSettings.RITARDO_TRAIL;
        targetIndex = Math.max(0, Math.min(targetIndex, playerTrail.size() - 1));
        
        Vec2 target = playerTrail.get(targetIndex);
        Vec2 currentPos = getPosition();
        double dx = target.x - currentPos.x;
        double dy = target.y - currentPos.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > IscatBomberSettings.DISTANZA_MIN_INSEGUIMENTO) {
            double nx = dx / distance;
            double ny = dy / distance;
            applyForce(new Vec2(
                nx * IscatBomberSettings.VELOCITA_INSEGUIMENTO,
                ny * IscatBomberSettings.VELOCITA_INSEGUIMENTO
            ));
            updateDirectionSmooth(dx, dy, IscatBomberSettings.SMOOTHING_ROTAZIONE);
        }
    }

    // --- fisica ---

    @Override
    public void integrate(double dt) {
        // Usa l'integrazione base di PhysicalEntityModel (con drag, cap, dead-zone)
        super.integrate(dt);
    }

    // --- Collidable ---

    @Override 
    public double getCollisionRadius() {
        return IscatBomberSettings.RAGGIO_COLLISIONE;
    }
    
    @Override 
    public Vec2 getColliderCenter() {
        // Usa l'implementazione di LivingEntityModel
        return super.getColliderCenter();
    }
    
    @Override 
    public void onCollision(Collidable other) {
        // La fisica del rimbalzo è già gestita da CollisionPhysics in GameModel.
        // Qui gestiamo solo la logica di gioco: stun e cooldown.
        if (collisionCooldown.isActive()) return;

        if (other instanceof PlayerModel) {
            stunTimer.set(IscatBomberSettings.DURATA_STORDIMENTO);
            collisionCooldown.set(IscatBomberSettings.COOLDOWN_COLLISIONE);
        }
    }

    // --- Alive ---

    @Override 
    public void die() {
        System.out.println("CHE TU SIA DANNATO!!!!! AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        /* TODO: animazione morte, esplosione, loot */ 
    }
    
    // --- Accessors ---
    
    public boolean isStunned() {
        return stunTimer.isActive();
    }
}
