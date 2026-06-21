package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;

/**
 * Modello fisico di base per tutti i proiettili all'interno dell'universo.
 * Estende {@link AbstractLivingEntityModel} configurando il corpo come proiettile ad alta velocità (CCD)
 * e azzerando gli effetti collaterali visivi/sonori alla sua distruzione.
 */
public abstract class AbstractPhysicalProjectileModel extends AbstractLivingEntityModel {

    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;

    /**
     * Costruttore rapido che inizializza il proiettile con una vita massima definita.
     */
    protected AbstractPhysicalProjectileModel(double maxLife) {
        this(0, 0, new EntityRecordBuilder().initLife(maxLife).build());
    }

    /**
     * Costruttore completo per definire posizione iniziale e record di dati dell'entità.
     */
    protected AbstractPhysicalProjectileModel(double x, double y, EntityRecord projectileRecord) {
        super(x, y, projectileRecord);
        setBullet(true); // Attiva il Continuous Collision Detection (CCD) di dyn4j per evitare il tunneling
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() {
        return terminalVelocity;
    }

    public void setTerminalVelocity(double v) {
        this.terminalVelocity = v;
    }

    /**
     * Interrompe il proiettile istantaneamente rimuovendolo dal mondo di gioco senza generare drop o audio.
     */
    @Override
    public void extinguish() {
        extinguish(true);
    }

    /**
     * Variante parametrizzata per estinguere il proiettile, impostando il flag di rimozione.
     */
    public void extinguish(boolean silent) {
        if (shouldRemove()) return;
        setShouldRemove(true);
        onDeath();
    }
}