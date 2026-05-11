package uni.gaben.iscat.gamenex.world.enviroment.asteroid;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.world.PhysicsSettings;
import uni.gaben.iscat.utils.ThemeColors;
import org.dyn4j.geometry.Vector2;

public class AsteroidView implements Drawable<AsteroidModel> {
    @Override
    public void render(AsteroidModel entity, GraphicsContext gc) {
        Vector2[] vertices = entity.getDisplayVertices();
        double[] xPoints = new double[vertices.length];
        double[] yPoints = new double[vertices.length];
        
        for(int i=0; i<vertices.length; i++) {
            Vector2 worldPoint = entity.getTransform().getTransformed(vertices[i]);
            xPoints[i] = worldPoint.x * PhysicsSettings.SCALE;
            yPoints[i] = worldPoint.y * PhysicsSettings.SCALE;
        }
        
        gc.setStroke(ThemeColors.getTextSecondary());
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, vertices.length);
    }
}
