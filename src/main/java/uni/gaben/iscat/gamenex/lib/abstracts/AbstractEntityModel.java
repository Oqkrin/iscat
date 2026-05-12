package uni.gaben.iscat.gamenex.lib.abstracts;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.gamenex.lib.interfaces.model.CappedVelocity;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

/**
 * Rappresentazione astratta di un'entità nel mondo fisico.
 * Estende {@link Body} di Dyn4j per fornire capacità di simulazione fisica.
 * Gestisce la conversione tra il sistema di coordinate pixel (View) e metri (Fisica).
 */
public abstract class AbstractEntityModel extends Body implements CappedVelocity {
    private String entityId;

    /**
     * Crea un modello con un identificatore specifico.
     * @param entityId ID univoco per il sistema di spawning o logica.
     */
    protected AbstractEntityModel(String entityId) {
        super();
        this.entityId = entityId;
    }

    protected AbstractEntityModel() {
        super();
    }

    /**
     * Inizializza l'entità in una posizione specifica.
     * Converte automaticamente le coordinate pixel in metri per Dyn4j.
     * @param x Coordinata X (pixel).
     * @param y Coordinata Y (pixel).
     */
    protected AbstractEntityModel(double x, double y) {
        super();
        translate(x / UniverseSettings.SCALE, y / UniverseSettings.SCALE);
    }

    /** Restituisce l'ID dell'entità. */
    public String getEntityId() { return entityId; }
    /** Imposta l'ID dell'entità. */
    public void setEntityId(String id) { this.entityId = id; }

    @Override
    public double getMaxVelocity() {
        return Double.MAX_VALUE;
    }
}
