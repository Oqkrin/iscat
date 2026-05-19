package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormHeadController extends AiBehaviours<IscatWormHeadModel> {

    private final IscatWormHeadModel head;
    private Vector2 target = null;
    private final uni.gaben.iscat.utils.Cooldown attackCooldown = new uni.gaben.iscat.utils.Cooldown();

    public IscatWormHeadController(IscatWormHeadModel head) {
        super(head);
        this.head = head;

        // --- COMPOSIZIONE BEHAVIORS ---

        // 1. CHASE (Inseguimento aggressivo)
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                // Sempre priorità base (10.0), a meno che non sia in range d'attacco
                return 10.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return;

                Vector2 headPos = head.getTransform().getTranslation();
                Vector2 playerPos = player.getTransform().getTranslation();

                if (target == null || headPos.distanceSquared(target) < 12) {
                    target = playerPos.copy();
                }

                Vector2 direction = target.copy().subtract(headPos);
                double distance = direction.getMagnitude();

                if (distance > 0.5) {
                    double targetAngle = direction.getDirection();
                    double currentAngle = head.getTransform().getRotationAngle();

                    // Shortest arc calculation to prevent weird wobbly wiggling and 360 spins at boundary crossing
                    double diff = targetAngle - currentAngle;
                    while (diff < -Math.PI) diff += Math.PI * 2;
                    while (diff > Math.PI) diff -= Math.PI * 2;

                    double newAngle = currentAngle + diff * 0.17;
                    head.getTransform().setRotation(newAngle);

                    if (head.getLinearVelocity().getMagnitude() <= IscatWormHeadSettings.MAX_VELOCITY_MS) {
                        Vector2 force = direction.getNormalized().multiply(IscatWormHeadSettings.FORCE);
                        head.applyForce(force);
                    } else {
                        Vector2 vel = head.getLinearVelocity();
                        head.setLinearVelocity(vel.getNormalized().multiply(IscatWormHeadSettings.MAX_VELOCITY_MS));
                    }
                }
            }
        });

        // 2. ATTACK
        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                Vector2 headPos = head.getTransform().getTranslation();
                Vector2 playerPos = player.getTransform().getTranslation();
                double distance = playerPos.copy().subtract(headPos).getMagnitude();

                double attackRadius = UU.pxToM(IscatWormHeadSettings.RAGGIO_COLLISIONE_PX)
                        * IscatWormHeadSettings.ATTACK_RADIUS_MULTIPLIER;

                return distance < attackRadius ? 80.0 : 0.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player != null) {
                    performAttack(player, universe);
                }
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (head == null || head.shouldRemove()) return;
        attackCooldown.update(dt);
        super.aiUpdate(universeModel, dt);
    }

    private void performAttack(PlayerModel player, UniverseModel universe) {
        if (attackCooldown.isReady()) {
            System.out.println("[SnakeHead] ATTACCO al player!");
            player.deltaToLife(-IscatWormHeadSettings.ATTACK_POWER);
            attackCooldown.start(IscatWormHeadSettings.ATTACK_COOLDOWN_S);
        }
    }
}