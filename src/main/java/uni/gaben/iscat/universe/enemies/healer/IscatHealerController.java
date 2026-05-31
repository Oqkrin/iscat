package uni.gaben.iscat.universe.enemies.healer;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.enemies.generic.GenericEntitySettings;
import uni.gaben.iscat.utils.Cooldown;

public class IscatHealerController extends AiController {

    private final Cooldown healCooldown = new Cooldown();
    private final GenericEntitySettings s;

    public IscatHealerController(IscatHealerModel healer) {
        super(healer, healer.getSettings().force,
                healer.getSettings().maxVelocity,
                healer.getSettings().rotationSpeed);

        this.s = healer.getSettings();

        setMovementStrategy(new HealerHideStrategy());

        addModifier(new SeparationModifier(UU.pxToM(32.0), s.force * 0.8));
        addModifier(new ProjectileAvoidanceModifier());

        addAttack(new HealAlliesBehavior());
    }

    // ── Hiding movement strategy ──────────────────────────────────────────────

    private class HealerHideStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            IscatHealerModel healer = (IscatHealerModel) entity;
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 myPos = healer.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();

            AbstractEntityModel bestAlly = null;
            double bestDist = Double.MAX_VALUE;
            for (AbstractEntityModel e : world.getEntitiesOfType(AbstractEntityModel.class)) {
                if (e == healer || e == player) continue;
                if (e instanceof IscatHealerModel) continue;
                double d = myPos.distance(e.getTransform().getTranslation());
                if (d < bestDist) { bestDist = d; bestAlly = e; }
            }

            if (bestAlly == null) {
                Vector2 away = myPos.copy().subtract(playerPos);
                if (away.getMagnitudeSquared() < 0.01) return new Vector2();
                return away.getNormalized().multiply(s.maxVelocity);
            }

            Vector2 allyPos = bestAlly.getTransform().getTranslation();
            Vector2 dirFromPlayerToAlly = allyPos.copy().subtract(playerPos).getNormalized();
            Vector2 hidePos = allyPos.copy().add(dirFromPlayerToAlly.multiply(3.0));
            Vector2 toHide = hidePos.copy().subtract(myPos);
            if (toHide.getMagnitudeSquared() < 0.1) return new Vector2();
            return toHide.getNormalized().multiply(s.maxVelocity);
        }
    }

    // ── Healing behaviour ─────────────────────────────────────────────────────

    private class HealAlliesBehavior implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (healCooldown.isCoolingDown()) return 0.0;
            double range = s.customParam1 > 0 ? s.customParam1 : IscatHealerSettings.HEAL_RADIUS_M;
            for (LivingEntityModel l : world.getEntitiesOfType(LivingEntityModel.class)) {
                if (l == entity || l instanceof PlayerModel) continue;
                if (l.getLife() < l.getMaxLife() &&
                        entity.getTransform().getTranslation()
                                .distance(l.getTransform().getTranslation()) <= range) {
                    return 100.0;
                }
            }
            return 0.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            double range  = s.customParam1 > 0 ? s.customParam1 : IscatHealerSettings.HEAL_RADIUS_M;
            double amount = s.customParam2 > 0 ? s.customParam2 : IscatHealerSettings.HEAL_AMOUNT;
            for (LivingEntityModel l : world.getEntitiesOfType(LivingEntityModel.class)) {
                if (l == entity || l instanceof PlayerModel) continue;
                if (entity.getTransform().getTranslation()
                        .distance(l.getTransform().getTranslation()) <= range) {
                    l.setLife(Math.min(l.getLife() + amount, l.getMaxLife()));
                }
            }
            double cooldown = s.fireCooldownS > 0 ? s.fireCooldownS : IscatHealerSettings.HEAL_COOLDOWN_S;
            healCooldown.start(cooldown);
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            healCooldown.update(dt);
        }
    }
}