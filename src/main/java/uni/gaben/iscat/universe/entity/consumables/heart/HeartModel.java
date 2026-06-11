package uni.gaben.iscat.universe.entity.consumables.heart;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entity.EntityFactory;
import uni.gaben.iscat.universe.entity.interfaces.HasSprite;

public class HeartModel extends AbstractLivingEntityModel implements HasSprite {
    public HeartModel(double x, double y) {
        super(x, y, new EntityFactory.EntityRecordBuilder().build());

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

    @Override
    public String getSpritePath() {
        return HeartSettings.sprite;
    }

    @Override
    public int getSpriteFrameWidth() {
        return HeartSettings.DIM_SPRITE;
    }

    @Override
    public double getFrameDuration() {
        return UU.UNIVERSE_TICK*6;
    }

    @Override
    public double getFrameDuration(int state, int frame) {
        return getFrameDuration();
    }

    @Override
    public int getSpriteFrameHeight() {
        return HeartSettings.DIM_SPRITE;
    }

    @Override
    public double getVisualScale() {
        return HeartSettings.SCALE;
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 0;
    }
}

