package uni.gaben.iscat.universe.enemies.bomber;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.bomber.IscatBomberSettings.ISCATBOMBER;

/**
 * Modello fisico dell'entità IscatBomber.
 * Differisce dall'IscatMob normale per:
 * - elevato damping lineare (più "pesante" nella sua inerzia)
 * - meccanica di stordimento quando colpito dal giocatore
 * Il callback di collisione che innesca lo stun è registrato
 * esternamente (da UniverseSpawner), non qui dentro.
 * Il modello espone solo i predicati di stato: {@link #isStunned()}.
 */
public class IscatBomberModel extends LivingEntityModel implements Updatable {

    private final Cooldown stunCooldown = new Cooldown();

    public IscatBomberModel(double x, double y) {
        super(x, y, ISCATBOMBER.initLife, ISCATBOMBER.initLife);
        setXpReward(ISCATBOMBER.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATBOMBER.dimSprite * ISCATBOMBER.scale / 2.0 * 0.9)));
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);

        setMass(MassType.NORMAL);
        setLinearDamping(ISCATBOMBER.dampingLineare);

    }

    // ─── LifeDeath ──────────────────────────────────────────────────────────

    /**
     * Aggiorna i cooldown interni. Chiamato dall'UniverseController ogni tick.
     */
    public void update(double dt) {
        stunCooldown.update(dt);
        updateStateTime(dt);
    }

    // ─── Stun state ─────────────────────────────────────────────────────────

    /** Avvia il timer di stordimento (chiamato dal callback collisione esterno). */
    public void applyStun() {
        stunCooldown.start(IscatBomberSettings.DURATA_STORDIMENTO_SEC);
    }

    /** @return true se il bomber è attualmente stordito e non può muoversi. */
    public boolean isStunned() {
        return stunCooldown.isCoolingDown();
    }

    // ─── HasTerminalVelocity ─────────────────────────────────────────────────

    @Override
    public double getTerminalVelocity() {
        return ISCATBOMBER.maxVelocity;
    }
}
