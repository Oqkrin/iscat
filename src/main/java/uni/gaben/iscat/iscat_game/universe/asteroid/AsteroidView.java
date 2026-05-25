package uni.gaben.iscat.iscat_game.universe.asteroid;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.utils.UU;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.utils.ThemeManager;

public class AsteroidView implements Drawable<AsteroidModel> {
    @Override
    public void draw(AsteroidModel entity, GraphicsContext gc) {
        Vector2[] vertices = entity.getDisplayVertices();
        double[] xPoints = new double[vertices.length];
        double[] yPoints = new double[vertices.length];
        
        for(int i=0; i<vertices.length; i++) {
            Vector2 worldPoint = entity.getTransform().getTransformed(vertices[i]);
            xPoints[i] = UU.mToPx(worldPoint.x);
            yPoints[i] = UU.mToPx(worldPoint.y);
        }

        gc.setFill(Color.BLACK);
        gc.fillPolygon(xPoints, yPoints, vertices.length);
        
        gc.setStroke(ThemeManager.getInstance().getTextSecondary());
        gc.setLineWidth(2);
        gc.strokePolygon(xPoints, yPoints, vertices.length);
    }
}
