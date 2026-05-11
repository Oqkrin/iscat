package uni.gaben.iscat.gamenex.player;

import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.interfaces.model.AbstractEntityModel;
import uni.gaben.iscat.gamenex.interfaces.model.Alive;

import uni.gaben.iscat.gamenex.world.PhysicsSettings;

public class PlayerModel extends AbstractEntityModel implements Alive {

    private int hp;
    private int maxHp;

    private double dashCooldownRemaining = 0;
    private double dashPhaseRemaining = 0;
    private double fireCooldownRemaining = 0;

    public PlayerModel(double x, double y) {
        // Scala il raggio di collisione in metri
        addFixture(Geometry.createCircle(PlayerSettings.RAGGIO_COLLISIONE / PhysicsSettings.SCALE));
        setMass(MassType.NORMAL);
        translate(x / PhysicsSettings.SCALE, y / PhysicsSettings.SCALE);
        setLinearDamping(PlayerSettings.LINEAR_DAMPING);
        
        this.hp = PlayerSettings.HP_INIZIALE;
        this.maxHp = PlayerSettings.HP_MASSIMO;
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

    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }
    public int getMaxHp() { return maxHp; }
    
    @Override
    public void die() {
        System.out.println("Player Died!");
        // TODO: death logic
    }
}
