package uni.gaben.iscat.universe.consumables.heart;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

public class HeartModel extends LivingEntityModel {
    public HeartModel(double x, double y) {
        super(x, y, 1, 1);

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(HeartSettings.RAGGIO_COLLISIONE_PX)));
        // Applica il filtro per distinguere questa entità come BOOST nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.BOOST_FILTER);
        // Rende l'oggetto un sensore: viene rilevato il tocco ma ci passi attraverso
        fixture.setSensor(true);
        // Hearth si muove se c'è il player nel suo raggio
        this.setMass(MassType.NORMAL);
        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        //setLinearDamping(ISCATMOB.dampingLineare);
    }

    @Override
    public double getTerminalVelocity() {
        return 10; /*ISCATMOB.maxVelocity*/
    }
}

