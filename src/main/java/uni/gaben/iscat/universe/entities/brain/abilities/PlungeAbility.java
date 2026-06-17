package uni.gaben.iscat.universe.entities.brain.abilities;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractEntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.entities.brain.target.Predictor;
import uni.gaben.iscat.universe.entities.brain.target.Target;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Collections;
import java.util.List;

public class PlungeAbility extends Ability {

    private final Cooldown plungeCooldown;
    private final Cooldown plungeDuration;
    private final Target plungeTarget;
    private final double maxPredictionTime;
    private final double plungeImpulse;

    private Vector2 plungeDirection = new Vector2();

    /**
     * @param cooldownSec        seconds before plunge can be used again
     * @param durationSec        seconds the plunge movement lasts
     * @param maxPredictionTime  seconds to look ahead for target prediction (used by Predictor)
     * @param impulse            impulse strength applied to the entity
     * @param target             target to plunge toward (usually the player)
     */
    public PlungeAbility(AbstractEntityModel entity,
                         double cooldownSec,
                         double durationSec,
                         double maxPredictionTime,
                         double impulse,
                         Target target) {
        super("plunge", AbilityCategory.MOVEMENT, Collections.emptySet());
        this.plungeCooldown = new Cooldown(durationSec + cooldownSec);
        this.plungeDuration = new Cooldown(durationSec);
        this.maxPredictionTime = maxPredictionTime;
        this.plungeImpulse = impulse;
        this.plungeTarget = target;
    }

    @Override
    public boolean canActivate(AbstractEntityModel self, UniverseModel world, double dt) {
        if (plungeCooldown.isCoolingDown()) return false;
        if (plungeDuration.isCoolingDown()) return false;
        return true;
    }

    @Override
    public void onActivate(Brain<?> brain, UniverseModel world) {

        AbstractEntityModel self = brain.getEntity();
        self.setTemporaryTerminalVelocity(self.getTerminalVelocity()*3);
        self.setDashLinearDamping(0);

        List<? extends AbstractEntityModel> targets = plungeTarget.getEntities(world);
        if (targets == null || targets.isEmpty()) return;

        AbstractEntityModel targetEntity = targets.getFirst();
        Vector2 selfPos = self.getTransform().getTranslation();
        Vector2 targetPos = targetEntity.getTransform().getTranslation();

        // Calculate pursuit time using Predictor's matrix method
        double currentSpeed = self.getLinearVelocity().getMagnitude();
        double maxVel = self.getTerminalVelocity();
        double pursuitTime = Predictor.calculatePursuitTime(
                selfPos,
                self.getTransform().getRotationAngle(),
                targetPos,
                targetEntity.getTransform().getRotationAngle(),
                currentSpeed,
                maxVel
        );
        // Clamp to max prediction time
        double lookAhead = Math.min(pursuitTime, maxPredictionTime);

        // Predict target position at lookAhead time
        Vector2 predictedPos = UU.vector2zero();
        Predictor.extrapolate(plungeTarget, world, lookAhead, predictedPos);

        // Compute direction and apply impulse
        plungeDirection.set(predictedPos).subtract(selfPos).normalize();
        double mass = self.getMass().getMass();
        self.applyImpulse(plungeDirection.multiply(plungeImpulse * mass));

        // Start cooldowns
        plungeDuration.start();
        plungeCooldown.start();
    }

    @Override
    public boolean update(Brain<?> brain, UniverseModel world, double dt) {
        plungeCooldown.update(dt);
        plungeDuration.update(dt);
        if (plungeDuration.isReady()) {
            brain.getEntity().restoreLinearDamping();
            brain.getEntity().setTemporaryTerminalVelocity(-1);
        }
        return plungeDuration.isCoolingDown();
    }
}