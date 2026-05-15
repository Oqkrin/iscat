package uni.gaben.iscat.gamenex.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.PhysicsWorld;
import org.dyn4j.world.World;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasTerminalVelocity;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldModel;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Modello fisico del mondo di gioco.
 */
public class UniverseModel extends World<Body> {

    private final Random rand = new Random();
    private PlayerModel player;

    private final List<AbstractEntityModel> entities = new ArrayList<>();
    private final StarfieldModel starfieldModel = new StarfieldModel();

    private final DoubleProperty width = new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
    private final DoubleProperty height = new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public UniverseModel() {
        setGravity(PhysicsWorld.ZERO_GRAVITY);
    }

    public static double getUniverseScaled(double value) {
        return value / UniverseSettings.SCALE;
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

    /**
     * Helper to get specific types. Usage: getEntitiesOfType(AsteroidModel.class)
     */
    public <T extends AbstractEntityModel> List<T> getEntitiesOfType(Class<T> type) {
        return entities.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .collect(Collectors.toList());
    }

    @Override
    public boolean update(double dt) {
        // 1. Logic Update
        if (player != null) {
            player.update(dt);
        }

        // entity removal
        entities.removeIf(e -> {
            if (e.shouldRemove()) {
                removeEntity(e);
                return true;
            }
            return false;
        });

        // 3. Physics Constraints (Terminal Velocity)
        // Fixed: Added instanceof check to prevent ClassCastException if non-entity bodies exist
        for (Body b : getBodies()) {
            if (b instanceof HasTerminalVelocity entity) {
                double terminal = entity.getTerminalVelocity();
                if (b.getLinearVelocity().getMagnitude() > terminal) {
                    b.setLinearVelocity(b.getLinearVelocity().getNormalized().setMagnitude(terminal));
                }
            }
        }

        // 4. Dyn4j Physics Step
        return super.update(dt);
    }

    // Standard getters/setters...
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
    public PlayerModel getPlayer() { return player; }
    public StarfieldModel getStarfieldModel() { return starfieldModel; }
}