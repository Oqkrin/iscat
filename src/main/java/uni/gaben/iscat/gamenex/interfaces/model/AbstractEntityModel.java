package uni.gaben.iscat.gamenex.interfaces.model;

import org.dyn4j.dynamics.Body;

public abstract class AbstractEntityModel extends Body {
    private String entityId;

    protected AbstractEntityModel() {
        super();
    }

    public String getEntityId() { return entityId; }
    public void setEntityId(String id) { this.entityId = id; }
}
