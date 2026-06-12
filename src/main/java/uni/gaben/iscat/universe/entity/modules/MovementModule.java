package uni.gaben.iscat.universe.entity.modules;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.Thrust;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.Dynamic;
import uni.gaben.iscat.universe.entity.interfaces.HasDash;
import uni.gaben.iscat.universe.entity.interfaces.HasThrust;
import uni.gaben.iscat.universe.entity.Data.DynamicsData;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.universe.rendering.RenderingSettings;

public class MovementModule implements EntityModule, Dynamic, HasThrust, HasDash {

    private GameEntity entity;
    private DynamicsData data;

    private final Cooldown dashCooldown = new Cooldown();
    private final Cooldown dashDuration = new Cooldown();
    private final Thrust thrust = new Thrust();

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().dynamics();
    }

    @Override
    public void update(double dt) {
        dashCooldown.update(dt);
        dashDuration.update(dt);
        updateThrust();

        if (entity.getLinearVelocity().getMagnitudeSquared() > getTerminalVelocity() * getTerminalVelocity()) {
           entity.getLinearVelocity().setMagnitude(getTerminalVelocity());
        }
    }

    @Override
    public void updateThrust() {
        Vector2 worldVel = entity.getLinearVelocity();
        double speed = worldVel.getMagnitude();
        double intensity = Math.min(speed / data.maxVelocity(), 1.0);

        double normVx = worldVel.x / data.maxVelocity();
        double normVy = worldVel.y / data.maxVelocity();

        double rotRad = entity.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET;
        double cos = Math.cos(rotRad);
        double sin = Math.sin(rotRad);

        double localDriftX = -normVx * cos - normVy * sin;
        double localDriftY =  normVx * sin - normVy * cos;

        // Approximating w/h
        double w = 32, h = 32;
        if (entity.hasModule(SpriteModule.class)) {
            w = entity.getModule(SpriteModule.class).getSpriteFrameWidth();
            h = entity.getModule(SpriteModule.class).getSpriteFrameHeight();
        }

        thrust.update(intensity, new Vector2(localDriftX, localDriftY), w, h);
    }

    @Override
    public void dashTowards(double angle) {
        if (!canDash() || entity.getRecord().player() == null) return;
        Vector2 dashDir = new Vector2(Math.cos(angle), Math.sin(angle));
        if (entity.getLinearVelocity().dot(dashDir) < 0) {
            entity.setLinearVelocity(new Vector2(0, 0));
        }
        
        double dashImpulse = entity.getRecord().player().dashImpulse();
        entity.applyImpulse(dashDir.multiply(dashImpulse * entity.getMass().getMass()));
        
        dashDuration.start(entity.getRecord().player().dashDurationSec());
        dashCooldown.start(entity.getRecord().player().dashCooldownSec());
    }

    @Override
    public boolean canDash() {
        return dashCooldown.isReady();
    }

    @Override
    public boolean isDashing() {
        return dashDuration.isCoolingDown();
    }

    @Override
    public Thrust thrustState() {
        return thrust;
    }

    @Override
    public double getTerminalVelocity() {
        return data.terminalVelocity() * (isDashing() ? 10 : 1);
    }

    @Override
    public double getAcceleration() {
        return data.maxForce();
    }

    @Override
    public double getMaxAngularVelocity() {
        return data.maxAngularVelocity();
    }
}
