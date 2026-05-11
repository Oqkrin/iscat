package uni.gaben.iscat.gamenex.universe.asteroid;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeColors;
import org.dyn4j.geometry.Vector2;

public class AsteroidView implements Drawable<AsteroidModel> {
    @Override
    public void draw(AsteroidModel entity, GraphicsContext gc) {
        Vector2[] vertices = entity.getDisplayVertices();
        double[] xPoints = new double[vertices.length];
        double[] yPoints = new double[vertices.length];
        
        for(int i=0; i<vertices.length; i++) {
            Vector2 worldPoint = entity.getTransform().getTransformed(vertices[i]);
            xPoints[i] = worldPoint.x * UniverseSettings.SCALE;
            yPoints[i] = worldPoint.y * UniverseSettings.SCALE;
        }
        
        gc.setStroke(ThemeColors.getTextSecondary());
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, vertices.length);
    }
}
