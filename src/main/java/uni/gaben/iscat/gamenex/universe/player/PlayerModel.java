package uni.gaben.iscat.gamenex.universe.player;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.GamenexCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

import javax.swing.event.ChangeListener;

public class PlayerModel extends LivingEntityModel {

    private double dashCooldownRemaining = 0;
    private double dashPhaseRemaining = 0;
    private double fireCooldownRemaining = 0;

    public PlayerModel(double x, double y) {
        super(x, y, PlayerSettings.HP_INIZIALE, PlayerSettings.HP_MASSIMO);
        BodyFixture fixture = addFixture(Geometry.createCircle(PlayerSettings.RAGGIO_COLLISIONE / UniverseSettings.SCALE));
        fixture.setFilter(GamenexCollisionLayers.PLAYER_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(PlayerSettings.LINEAR_DAMPING);
    }

    public void update(double dt) {
        if (dashCooldownRemaining > 0) dashCooldownRemaining -= dt;
        if (dashPhaseRemaining > 0) dashPhaseRemaining -= dt;
        if (fireCooldownRemaining > 0) fireCooldownRemaining -= dt;
        if (isInScatto()) {
            setLinearDamping(0.7); // Quasi zero attrito per mantenere il momentum
        } else {
            setLinearDamping(PlayerSettings.LINEAR_DAMPING);
        }
    }

    public void executeScatto(double angle) {
        Vector2 dashDir = new Vector2(Math.cos(angle), Math.sin(angle));

        // Directional Snap: se scatto controcorrente, resetto velocità per reattività istantanea
        if (getLinearVelocity().dot(dashDir) < 0) {
            setLinearVelocity(new Vector2(0,0));
        }

        applyImpulse(dashDir.multiply(PlayerSettings.IMPULSO_SCATTO * getMass().getMass()));

        dashPhaseRemaining = PlayerSettings.DURATA_SCATTO_SEC;
        dashCooldownRemaining = PlayerSettings.COOLDOWN_SCATTO_SEC;
    }

    public boolean isScattoDisponibile() { return dashCooldownRemaining <= 0; }
    public boolean isInScatto() { return dashPhaseRemaining > 0; }
    public boolean isSparoDisponibile() { return fireCooldownRemaining <= 0; }
    public void startCooldownFuoco() { fireCooldownRemaining = PlayerSettings.COOLDOWN_FUOCO_SEC; }

    /** Ritorna un valore [0, 1] per la barra dello scatto nella UI */
    public double getDashMeter() {
        if (isScattoDisponibile()) return 1.0;
        return 1.0 - (dashCooldownRemaining / PlayerSettings.COOLDOWN_SCATTO_SEC);
    }

    @Override
    public void onDeath() { /* Implement logic */ }

    @Override
    public double getMaxVelocity() {
        return PlayerSettings.VELOCITA_MAX;
    }
}