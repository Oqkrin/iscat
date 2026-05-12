package uni.gaben.iscat.gamenex.universe.hearth;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.universe.GamenexCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobSettings;

public class HearthModel extends AbstractEntityModel {

    public HearthModel(double x, double y) {
        super(x, y);

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(HearthSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE));

        // Applica il filtro per distinguere questa entità come BOOST nelle collisioni
        fixture.setFilter(GamenexCollisionLayers.BOOST_FILTER);

        // Rende l'oggetto un sensore: viene rilevato il tocco ma ci passi attraverso
        fixture.setSensor(true);

        // Imposta il tipo di massa infinite cosi non si muove se colpito
        setMass(MassType.INFINITE);

        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        setLinearDamping(IscatMobSettings.DAMPING_LINEARE);

    }

    @Override
    public double getMaxVelocity() {
        return IscatMobSettings.MAX_VELOCITY_MS;
    }
}
