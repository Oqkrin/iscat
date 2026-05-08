package uni.gaben.iscat.game.components.entities.npcs.iscat_mother;

import uni.gaben.iscat.game.components.entities.npcs.NpcModel;
import uni.gaben.iscat.game.components.entities.player.PlayerModel;
import uni.gaben.iscat.game.utils.interfaces.Collidable;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.interfaces.HasRenderer;
import uni.gaben.iscat.game.utils.interfaces.Spawnable;
import uni.gaben.iscat.game.utils.physics.Vec2;
import uni.gaben.iscat.utils.Cooldown;

/**
 * IscatMotherModel — pure model.
 *
 * Owns: physics config, collision response, death, stun state.
 * Does NOT own: AI logic, trail, movement decisions → see IscatMotherController.
 */
public class IscatMotherModel extends NpcModel implements Collidable, HasRenderer, Spawnable {

    private static final IscatMotherView VIEW = new IscatMotherView();

    private final Cooldown stunTimer         = new Cooldown();
    private final Cooldown collisionCooldown = new Cooldown();

    public IscatMotherModel(double startX, double startY) {
        super(startX, startY);
        this.hp         = IscatMotherSettings.HP;
        this.maxHp      = IscatMotherSettings.HP;
        this.mass       = IscatMotherSettings.MASSA;
        this.name       = "IscatMother";
        this.spriteSize = IscatMotherSettings.DIMENSIONE_SPRITE;
        this.drag       = IscatMotherSettings.ATTRITO;
        this.maxSpeed   = IscatMotherSettings.VELOCITA_MAX;
        this.deadZone   = 0.01;
    }

    // -------------------------------------------------------------------------
    // Collidable
    // -------------------------------------------------------------------------

    @Override
    public double getCollisionRadius() { return IscatMotherSettings.RAGGIO_COLLISIONE; }

    @Override
    public Vec2 getColliderCenter() { return super.getColliderCenter(); }

    @Override
    public void onCollision(Collidable other) {
        // Physics handled by CollisionPhysics in GameModel.
        // Only game logic here: stun + cooldown.
        if (collisionCooldown.isActive()) return;
        if (other instanceof PlayerModel) {
            stunTimer.set(IscatMotherSettings.DURATA_STORDIMENTO);
            collisionCooldown.set(IscatMotherSettings.COOLDOWN_COLLISIONE);
        }
    }

    @Override
    public void die() {
        System.out.println("CHE TU SIA DANNATO!!!!! AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        // TODO: death animation, explosion, loot
    }

    // -------------------------------------------------------------------------
    // HasRenderer
    // -------------------------------------------------------------------------

    @Override
    @SuppressWarnings("unchecked")
    public EntityRenderer<IscatMotherModel> getRenderer() { return VIEW; }

    // -------------------------------------------------------------------------
    // Collision layers
    // -------------------------------------------------------------------------

    @Override public int getCollisionLayer() { return LAYER_ENEMY; }
    @Override public int getCollisionMask()  { return LAYER_PLAYER | LAYER_PROJECTILE | LAYER_ENEMY; }

    // -------------------------------------------------------------------------
    // Stun state — read by IscatMotherController
    // -------------------------------------------------------------------------

    public boolean isStunned() { return stunTimer.isActive(); }

    /** Tick stun and collision cooldown — called by IscatMotherController each AI update. */
    public void tickCooldowns() {
        stunTimer.tick();
        collisionCooldown.tick();
    }
}