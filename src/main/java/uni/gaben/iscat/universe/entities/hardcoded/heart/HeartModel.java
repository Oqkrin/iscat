package uni.gaben.iscat.universe.entities.hardcoded.heart;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.interfaces.HasSprite;

public class HeartModel extends AbstractLivingEntityModel implements HasSprite {
    public HeartModel(double x, double y) {
        super(x, y, new EntityRecordBuilder().build());

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(UU.pxToM(HeartSettings.RAGGIO_COLLISIONE_PX)));
        // Applica il filtro per distinguere questa entità come BOOST nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.BOOST_FILTER);
        // Rende l'oggetto un sensore: viene rilevato il tocco ma ci passi attraverso
        fixture.setSensor(true);
        // Blocca la rotazione fisica ma permette lo spostamento lineare
        this.setMass(MassType.FIXED_ANGULAR_VELOCITY);
    }

    @Override
    public double getTerminalVelocity() {
        return 10;
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
    public int getSpriteFrameHeight() {
        return HeartSettings.DIM_SPRITE;
    }

    @Override
    public double getVisualAngularOffsetDeg() {
        return 90;
    }

    @Override
    public boolean isInalterable() {
        return false;
    }
}