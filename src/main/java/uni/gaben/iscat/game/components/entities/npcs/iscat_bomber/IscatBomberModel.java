package uni.gaben.iscat.game.components.entities.npcs.iscat_bomber;

import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.components.entities.player.PlayerSettings;
import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.utils.Cooldown;

/**
 * IscatBomberModel — pure model.
 *
 * Owns: physics config, collision response, death, stun state.
 * Does NOT own: AI logic, trail, movement decisions → see IscatBomberController.
 */
public class IscatBomberModel extends NpcModel implements Collidable {

    private final Cooldown stunTimer        = new Cooldown();
    private final Cooldown collisionCooldown = new Cooldown();

    public IscatBomberModel(double startX, double startY) {
        super(startX, startY);
        this.hp        = IscatBomberSettings.HP;
        this.maxHp     = IscatBomberSettings.HP;
        this.mass      = PlayerSettings.MASSA * IscatBomberSettings.FATTORE_MASSA;
        this.name      = "IscatBomber";
        this.spriteSize = PlayerSettings.DIMENSIONE_SPRITE;
        this.drag      = IscatBomberSettings.ATTRITO;
        this.maxSpeed  = PlayerSettings.VELOCITA_MAX * IscatBomberSettings.FATTORE_VELOCITA_MAX;
        this.deadZone  = 0.01;
    }

    // -------------------------------------------------------------------------
    // Collidable
    // -------------------------------------------------------------------------

    @Override
    public double getCollisionRadius() { return IscatBomberSettings.RAGGIO_COLLISIONE; }

    @Override
    public Vec2 getColliderCenter() { return super.getColliderCenter(); }

    @Override
    public void onCollision(Collidable other) {
        // Physics handled by CollisionPhysics in GameModel.
        // Only game logic here: stun + cooldown.
        if (collisionCooldown.isActive()) return;
        if (other instanceof PlayerModel) {
            stunTimer.set(IscatBomberSettings.DURATA_STORDIMENTO);
            collisionCooldown.set(IscatBomberSettings.COOLDOWN_COLLISIONE);
        }
    }

    // -------------------------------------------------------------------------
    // Alive
    // -------------------------------------------------------------------------

    @Override
    public void die() {
        System.out.println("CHE TU SIA DANNATO!!!!! AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        // TODO: death animation, explosion, loot
    }

    // -------------------------------------------------------------------------
    // Stun state — read by IscatBomberController
    // -------------------------------------------------------------------------

    public boolean isStunned()          { return stunTimer.isActive(); }

    /** Tick stun and collision cooldown — called by IscatBomberController each AI update. */
    public void tickCooldowns() {
        stunTimer.tick();
        collisionCooldown.tick();
    }
}
