package uni.gaben.iscat.universe.enemies.eater;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.eater.IscatEaterSettings.ISCATEATER;
import static uni.gaben.iscat.universe.enemies.eater.IscatEaterSettings.ATTACK_POWER;

public class IscatEaterController extends AiController {

    public IscatEaterController(IscatEaterModel eater) {
        super(eater, ISCATEATER.force, ISCATEATER.maxVelocity, ISCATEATER.rotationSpeed);

        // ── MOVEMENT STRATEGY: always chase the player ─────────────────────
        setMovementStrategy((entity, world, dt) -> {
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();
            Vector2 toPlayer = player.getTransform().getTranslation()
                    .copy()
                    .subtract(entity.getTransform().getTranslation());
            // Stop when extremely close (explosion handles the rest)
            if (toPlayer.getMagnitude() < 0.5) return new Vector2();
            return toPlayer.getNormalized().multiply(ISCATEATER.maxVelocity);
        });

        // ── AVOIDANCE MODIFIERS (applied in order) ─────────────────────────
        // Dodge incoming projectiles while chasing
        addModifier(new ProjectileAvoidanceModifier());
        // Keep some distance from other eaters / allies
        addModifier(new SeparationModifier(UU.pxToM(24.0), ISCATEATER.force * 0.8));

        // ── ATTACK: explode on touching the player ─────────────────────────
        addAttack(new ExplodeOnTouchAttack());

        // ── COLLISION: direct damage when physics bodies overlap ────────────
        eater.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !eater.shouldRemove()) {
                player.deltaToLife(-ATTACK_POWER);
                eater.setLife(0);
                eater.kill();
                eater.setShouldRemove(true);
            }
        });
    }

    // ── Internal AttackBehavior ──────────────────────────────────────────────
    private class ExplodeOnTouchAttack implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;
            double dist = entity.getTransform().getTranslation()
                    .distance(player.getTransform().getTranslation());
            // Trigger when extremely close (0.8 units)
            return dist < 0.8 ? 100.0 : 0.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            // The attack itself doesn't deal damage – the collision listener does.
            // But we can force removal here if the listener somehow missed it.
            if (!entity.shouldRemove() && entity instanceof IscatEaterModel eater) {
                eater.setLife(0);
                eater.kill();
                eater.setShouldRemove(true);
            }
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            // No cooldown needed – once exploded, the entity is removed.
        }
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null || entity.shouldRemove()) return;
        super.update(universe, dt);
    }
}