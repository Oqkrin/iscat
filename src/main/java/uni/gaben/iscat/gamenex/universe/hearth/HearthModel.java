package uni.gaben.iscat.gamenex.universe.hearth;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_mob.IscatMobSettings;

public class HearthModel extends LivingEntityModel {
    public HearthModel(double x, double y) {
        super(x, y, 1, 1);

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(HearthSettings.RAGGIO_COLLISIONE_PX)));
        // Applica il filtro per distinguere questa entità come BOOST nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.BOOST_FILTER);
        // Rende l'oggetto un sensore: viene rilevato il tocco ma ci passi attraverso
        fixture.setSensor(true);
        // Hearth si muove se c'è il player nel suo raggio
        this.setMass(MassType.NORMAL);
        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        setLinearDamping(IscatMobSettings.DAMPING_LINEARE);
    }

    @Override
    public double getTerminalVelocity() {
        return IscatMobSettings.MAX_VELOCITY_MS;
    }
}
