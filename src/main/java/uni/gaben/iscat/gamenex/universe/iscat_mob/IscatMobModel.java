package uni.gaben.iscat.gamenex.universe.iscat_mob;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

/**
 * Modello fisico dell'entità IscatMob.
 * Rappresenta il corpo rigido del nemico nel mondo fisico di Dyn4j.
 * Gestisce la salute, la collisione circolare e le proprietà di attrito.
 */
public class IscatMobModel extends LivingEntityModel {

    /**
     * Crea un nuovo modello di IscatMob.
     * Configura la forma di collisione (cerchio) e applica i filtri di categoria.
     * @param x Coordinata X iniziale in pixel.
     * @param y Coordinata Y iniziale in pixel.
     */
    public IscatMobModel(double x, double y) {
        super(x, y, IscatMobSettings.HP_INIZIALI, IscatMobSettings.HP_INIZIALI);
        
        // Creazione della forma di collisione circolare scalata in metri
        BodyFixture fixture = addFixture(Geometry.createCircle(IscatMobSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE));
        
        // Applica il filtro per distinguere questa entità come NEMICO nelle collisioni
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        
        // Imposta il tipo di massa normale per permettere risposte fisiche agli urti
        setMass(MassType.NORMAL);
        
        // Applica l'attrito lineare per simulare la resistenza al movimento nel vuoto
        setLinearDamping(IscatMobSettings.DAMPING_LINEARE);

    }

    @Override
    public double getTerminalVelocity() {
        return IscatMobSettings.MAX_VELOCITY_MS;
    }
}
