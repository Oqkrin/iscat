package uni.gaben.iscat.gamenex.universe.player;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Alive;
import uni.gaben.iscat.gamenex.universe.GamenexCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public class PlayerModel extends LivingEntityModel {

    private double dashCooldownRemaining = 0;
    private double dashPhaseRemaining = 0;
    private double fireCooldownRemaining = 0;

    public PlayerModel(double x, double y) {
        super(x, y, PlayerSettings.HP_INIZIALE, PlayerSettings.HP_MASSIMO);
        // Scala il raggio di collisione in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(PlayerSettings.RAGGIO_COLLISIONE / UniverseSettings.SCALE));
        fixture.setFilter(GamenexCollisionLayers.PLAYER_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(PlayerSettings.LINEAR_DAMPING);
    }

    public void update(double dt) {
        if (dashCooldownRemaining > 0) dashCooldownRemaining -= dt;
        if (dashPhaseRemaining > 0) dashPhaseRemaining -= dt;
        if (fireCooldownRemaining > 0) fireCooldownRemaining -= dt;

        // Gestione Attrito/Damping dinamico durante lo scatto
        if (isInScatto()) {
            setLinearDamping(PlayerSettings.LINEAR_DAMPING_SCATTO);
        } else {
            setLinearDamping(PlayerSettings.LINEAR_DAMPING);
            // Cap velocità normale
            Vector2 vel = getLinearVelocity();
            if (vel.getMagnitude() > PlayerSettings.VELOCITA_MAX) {
                vel.normalize();
                vel.multiply(PlayerSettings.VELOCITA_MAX);
                setLinearVelocity(vel);
            }
        }
    }

    public boolean isScattoDisponibile() {
        return dashCooldownRemaining <= 0;
    }

    public boolean isInScatto() {
        return dashPhaseRemaining > 0;
    }

    public void executeScatto(double directionAngleRad) {
        double mass = getMass().getMass();
        Vector2 impulse = new Vector2(
            Math.cos(directionAngleRad) * PlayerSettings.IMPULSO_SCATTO * mass,
            Math.sin(directionAngleRad) * PlayerSettings.IMPULSO_SCATTO * mass
        );
        applyImpulse(impulse);
        dashPhaseRemaining = PlayerSettings.DURATA_SCATTO_SEC;
        dashCooldownRemaining = PlayerSettings.COOLDOWN_SCATTO_SEC;
    }

    public boolean isSparoDisponibile() {
        return fireCooldownRemaining <= 0;
    }

    public void startCooldownFuoco() {
        fireCooldownRemaining = PlayerSettings.COOLDOWN_FUOCO_SEC;
    }

    @Override
    public void onDeath() {
        System.out.println("Player died");
    }
}
