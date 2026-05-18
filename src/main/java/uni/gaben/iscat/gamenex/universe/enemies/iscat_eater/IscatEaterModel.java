package uni.gaben.iscat.gamenex.universe.enemies.iscat_eater;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public class IscatEaterModel extends LivingEntityModel {

    // Eater attacca una sola volta
    private boolean consumed = false;

    public IscatEaterModel(double x, double y) {
        super(x, y, IscatEaterSettings.HP_INIZIALI, IscatEaterSettings.HP_INIZIALI);

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(IscatEaterSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE));

        // Applica il filtro per distinguere questa entità come NEMICO nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);

        // Imposta il tipo di massa normale per permettere risposte fisiche agli urti
        setMass(MassType.NORMAL);

        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        setLinearDamping(IscatEaterSettings.DAMPING_LINEARE);

    }

    @Override
    public void onDeath() {
        super.onDeath();
    }

    @Override
    public double getTerminalVelocity() {
        return IscatEaterSettings.MAX_VELOCITY_MS;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }
}
