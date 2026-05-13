package uni.gaben.iscat.gamenex.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;

import java.util.Random;

import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Alive;
import java.util.ArrayList;
import java.util.List;

/**
 * Modello fisico del mondo di gioco.
 * Rappresenta lo spazio in cui esistono le entità, basato sul mondo fisico Dyn4j.
 * Gestisce le dimensioni della visuale e la lista dei corpi attivi.
 */
public class UniverseModel extends World<Body> {

    private Random rand = new Random();
    private PlayerModel player;
    private final List<AbstractEntityModel> entities = new ArrayList<>();
    private final StarfieldModel starfieldModel = new StarfieldModel();

    private DoubleProperty width = new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
    private DoubleProperty height = new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public UniverseModel() {
        setGravity(World.ZERO_GRAVITY);
    }

    public void setPlayer(PlayerModel player) {
        this.player = player;
        addEntity(player);
    }

    public void addEntity(AbstractEntityModel entity) {
        this.entities.add(entity);
        this.addBody(entity);
    }

    public void removeEntity(AbstractEntityModel entity) {
        this.entities.remove(entity);
        this.removeBody(entity);
    }

    public List<AbstractEntityModel> getEntities() {
        return entities;
    }
    
    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width.set(w);
            this.height.set(h);
        }
    }

    public double getWidth() { return width.get(); }
    public double getHeight() { return height.get(); }

    public DoubleProperty widthProperty() { return width; }
    public DoubleProperty heightProperty() { return height; }

    @Override
    public boolean update(double dt) {
        player.update(dt);

        // Remove dead or consumed entities
        entities.removeIf(e -> {
            if (e instanceof Alive a && !a.isAlive()) {
                removeBody(e);
                return true;
            }
            return false;
        });

        bodies.forEach(b -> {
            if(b.getLinearVelocity().getMagnitude() > ((AbstractEntityModel) b).getMaxVelocity()) {
                b.setLinearVelocity(b.getLinearVelocity().getNormalized().setMagnitude(((AbstractEntityModel) b).getMaxVelocity()));
            }
        });

        return super.update(dt);
    }
    
    public PlayerModel getPlayer() { return player; }
    public StarfieldModel getStarfieldModel() { return starfieldModel; }
}
