package uni.gaben.iscat.iscat_game.universe.iscats.eater;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;

import static uni.gaben.iscat.iscat_game.universe.iscats.eater.IscatEaterSettings.ISCATEATER;

public class IscatEaterModel extends LivingEntityModel {

    public IscatEaterModel(double x, double y) {
        super(x, y, ISCATEATER.initLife, ISCATEATER.initLife);
        setXpReward(ISCATEATER.xpReward);

        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATEATER.dimSprite * ISCATEATER.scale / 2.0 * 0.9)));

        // Applica il filtro per distinguere questa entità come NEMICO nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);

        // Imposta il tipo di massa normale per permettere risposte fisiche agli urti
        setMass(MassType.NORMAL);

        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        setLinearDamping(ISCATEATER.dampingLineare);

    }

    @Override
    public void onDeath() {
        super.onDeath();
    }

    @Override
    public double getTerminalVelocity() {
        return ISCATEATER.maxVelocity;
    }
}
